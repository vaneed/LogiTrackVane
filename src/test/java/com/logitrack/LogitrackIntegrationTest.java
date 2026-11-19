package com.logitrack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LogitrackIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;
    @Autowired JwtEncoder encoder;
    String admin;
    String empleado;
    long origen, destino, producto, otro;

    @BeforeEach
    void preparar() throws Exception {
        for (String tabla : new String[]{"detalle_movimiento", "movimiento", "existencia", "auditoria", "producto", "bodega", "usuario"}) {
            jdbc.update("DELETE FROM " + tabla);
        }
        empleado = enviar("POST", "/auth/register", null, "{\"nombre\":\"ana\",\"clave\":\"Clave1234\"}", 201).get("token").asText();
        enviar("POST", "/auth/register", null, "{\"nombre\":\"admin\",\"clave\":\"Clave1234\"}", 201);
        jdbc.update("UPDATE usuario SET rol='ADMIN' WHERE nombre='admin'");
        admin = enviar("POST", "/auth/login", null, "{\"nombre\":\"admin\",\"clave\":\"Clave1234\"}", 200).get("token").asText();
        origen = enviar("POST", "/bodegas", admin, "{\"nombre\":\"Norte\",\"ubicacion\":\"Bogotá\",\"capacidad\":100,\"encargado\":\"Ana\"}", 201).get("id").asLong();
        destino = enviar("POST", "/bodegas", admin, "{\"nombre\":\"Sur\",\"ubicacion\":\"Cali\",\"capacidad\":20,\"encargado\":\"Lina\"}", 201).get("id").asLong();
        producto = enviar("POST", "/productos", admin, "{\"nombre\":\"Cuaderno\",\"categoria\":\"Papel\",\"precio\":12000}", 201).get("id").asLong();
        otro = enviar("POST", "/productos", admin, "{\"nombre\":\"Lápiz\",\"categoria\":\"Papel\",\"precio\":1000}", 201).get("id").asLong();
    }
    JsonNode enviar(String metodo, String ruta, String token, String datos, int estado) throws Exception {
        var solicitud = switch (metodo) {
            case "POST" -> post(ruta); case "PUT" -> put(ruta); case "DELETE" -> delete(ruta); default -> get(ruta);
        };
        if (token != null) solicitud.header("Authorization", "Bearer " + token);
        if (datos != null) solicitud.contentType("application/json").content(datos);
        MvcResult respuesta = mvc.perform(solicitud).andExpect(status().is(estado)).andReturn();
        String cuerpo = respuesta.getResponse().getContentAsString();
        return cuerpo.isEmpty() ? null : mapper.readTree(cuerpo);
    }
    String movimiento(String tipo, Long desde, Long hasta, long id, int cantidad) {
        return "{\"tipo\":\"" + tipo + "\",\"bodegaOrigenId\":" + desde + ",\"bodegaDestinoId\":" + hasta + ",\"productos\":[{\"productoId\":" + id + ",\"cantidad\":" + cantidad + "}]}";
    }
    void entrada(int cantidad) throws Exception { enviar("POST", "/movimientos", empleado, movimiento("ENTRADA", null, origen, producto, cantidad), 201); }
    int stock() throws Exception { return enviar("GET", "/productos/" + producto, empleado, null, 200).get("stock").asInt(); }
    long contar(String tabla) { return jdbc.queryForObject("SELECT COUNT(*) FROM " + tabla, Long.class); }

    @Test void seguridadYRoles() throws Exception {
        assertThat(enviar("GET", "/bodegas", null, null, 401).get("estado").asInt()).isEqualTo(401);
        enviar("GET", "/bodegas", "invalido", null, 401);
        enviar("POST", "/productos", empleado, "{\"nombre\":\"X\",\"categoria\":\"Y\",\"precio\":10}", 403);
        enviar("GET", "/auditorias", empleado, null, 403);
        enviar("POST", "/auth/login", null, "{\"nombre\":\"ana\",\"clave\":\"incorrecta\"}", 401);
        enviar("POST", "/auth/register", null, "{\"nombre\":\"intruso\",\"clave\":\"Clave1234\",\"rol\":\"ADMIN\"}", 400);
        assertThat(jdbc.queryForObject("SELECT clave FROM usuario WHERE nombre='ana'", String.class)).startsWith("$2");
    }
    @Test void tokenVencido() throws Exception {
        JwtClaimsSet datos = JwtClaimsSet.builder().issuer("logitrack").subject("ana").claim("rol", "EMPLEADO")
            .issuedAt(Instant.now().minusSeconds(3600)).expiresAt(Instant.now().minusSeconds(120)).build();
        String vencido = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), datos)).getTokenValue();
        enviar("GET", "/productos", vencido, null, 401);
    }
    @Test void entradaSalidaYTransferencia() throws Exception {
        entrada(15);
        enviar("POST", "/movimientos", empleado, movimiento("TRANSFERENCIA", origen, destino, producto, 6), 201);
        assertThat(stock()).isEqualTo(15);
        assertThat(jdbc.queryForObject("SELECT cantidad FROM existencia WHERE bodega_id=? AND producto_id=?", Integer.class, destino, producto)).isEqualTo(6);
        JsonNode salida = enviar("POST", "/movimientos", empleado, movimiento("SALIDA", origen, null, producto, 4), 201);
        assertThat(salida.get("usuario").asText()).isEqualTo("ana");
        assertThat(stock()).isEqualTo(11);
        JsonNode resumen = enviar("GET", "/reportes/resumen", empleado, null, 200);
        assertThat(resumen.get("productosMasMovidos").get(0).get("cantidadMovida").asInt()).isEqualTo(25);
        assertThat(resumen.get("stockPorBodega").get(0).get("stockTotal").asInt()).isEqualTo(5);
    }
    @Test void stockInsuficienteNoDejaCambios() throws Exception {
        entrada(3); long auditorias = contar("auditoria"); long movimientos = contar("movimiento");
        enviar("POST", "/movimientos", empleado, movimiento("SALIDA", origen, null, producto, 4), 400);
        assertThat(stock()).isEqualTo(3);
        assertThat(contar("auditoria")).isEqualTo(auditorias);
        assertThat(contar("movimiento")).isEqualTo(movimientos);
    }
    @Test void falloEnSegundoProductoReviertePrimero() throws Exception {
        entrada(5); long auditorias = contar("auditoria");
        String datos = "{\"tipo\":\"SALIDA\",\"bodegaOrigenId\":" + origen + ",\"productos\":[{\"productoId\":" + producto + ",\"cantidad\":2},{\"productoId\":" + otro + ",\"cantidad\":1}]}";
        enviar("POST", "/movimientos", empleado, datos, 400);
        assertThat(stock()).isEqualTo(5);
        assertThat(contar("auditoria")).isEqualTo(auditorias);
        assertThat(contar("movimiento")).isEqualTo(1);
    }
    @Test void capacidadYBodegasInvalidas() throws Exception {
        enviar("POST", "/movimientos", empleado, movimiento("ENTRADA", null, destino, producto, 21), 400);
        enviar("POST", "/movimientos", empleado, movimiento("TRANSFERENCIA", origen, origen, producto, 1), 400);
        enviar("POST", "/movimientos", empleado, movimiento("ENTRADA", origen, destino, producto, 1), 400);
        enviar("POST", "/movimientos", empleado, movimiento("ENTRADA", null, 999999L, producto, 1), 404);
        enviar("POST", "/movimientos", empleado, movimiento("ENTRADA", null, destino, 999999L, 1), 404);
        assertThat(contar("movimiento")).isZero();
    }
    @Test void noReducirCapacidadOEliminarHistorial() throws Exception {
        entrada(15);
        enviar("PUT", "/bodegas/" + origen, admin, "{\"nombre\":\"Norte\",\"ubicacion\":\"Bogotá\",\"capacidad\":10,\"encargado\":\"Ana\"}", 400);
        enviar("DELETE", "/bodegas/" + origen, admin, null, 400);
        enviar("DELETE", "/productos/" + producto, admin, null, 400);
    }
    @Test void auditoriaTieneAntesDespuesYUsuario() throws Exception {
        enviar("PUT", "/productos/" + producto, admin, "{\"nombre\":\"Cuaderno rosa\",\"categoria\":\"Papel\",\"precio\":13000}", 200);
        JsonNode lista = enviar("GET", "/auditorias?usuario=admin&operacion=UPDATE", admin, null, 200);
        assertThat(lista.size()).isEqualTo(1);
        JsonNode a = lista.get(0);
        assertThat(a.get("entidad").asText()).isEqualTo("Producto");
        assertThat(mapper.readTree(a.get("valoresAnteriores").asText()).get("nombre").asText()).isEqualTo("Cuaderno");
        assertThat(mapper.readTree(a.get("valoresNuevos").asText()).get("nombre").asText()).isEqualTo("Cuaderno rosa");
        enviar("DELETE", "/productos/" + producto, admin, null, 204);
        JsonNode borrados = enviar("GET", "/auditorias?operacion=DELETE", admin, null, 200);
        assertThat(borrados.size()).isEqualTo(1);
        assertThat(borrados.get(0).get("valoresNuevos").isNull()).isTrue();
    }
    @Test void auditoriaAutomaticaDeStock() throws Exception {
        entrada(8);
        JsonNode lista = enviar("GET", "/auditorias?usuario=ana&operacion=UPDATE", admin, null, 200);
        JsonNode dato = lista.get(0);
        assertThat(dato.get("entidad").asText()).isEqualTo("Producto");
        assertThat(mapper.readTree(dato.get("valoresAnteriores").asText()).get("stock").asInt()).isZero();
        assertThat(mapper.readTree(dato.get("valoresNuevos").asText()).get("stock").asInt()).isEqualTo(8);
    }
    @Test void filtrosDeStockYFechas() throws Exception {
        entrada(10);
        assertThat(enviar("GET", "/productos/stock-bajo", empleado, null, 200).size()).isEqualTo(1);
        JsonNode todos = enviar("GET", "/movimientos", empleado, null, 200);
        String fecha = todos.get(0).get("fecha").asText();
        assertThat(enviar("GET", "/movimientos?desde=" + fecha + "&hasta=" + fecha, empleado, null, 200).size()).isEqualTo(1);
        assertThat(enviar("GET", "/movimientos?desde=2000-01-01T00:00:00&hasta=2000-01-02T00:00:00", empleado, null, 200).isEmpty()).isTrue();
        enviar("GET", "/movimientos?desde=2026-01-01T00:00:00", empleado, null, 400);
        enviar("GET", "/movimientos?desde=2026-02-01T00:00:00&hasta=2026-01-01T00:00:00", empleado, null, 400);
    }
    @Test void validacionesYErroresJson() throws Exception {
        enviar("POST", "/productos", admin, "{\"nombre\":\"\",\"categoria\":\"Papel\",\"precio\":-1}", 400);
        enviar("POST", "/movimientos", empleado, movimiento("ENTRADA", null, origen, producto, 0), 400);
        enviar("POST", "/movimientos", empleado, "{\"tipo\":\"ENTRADA\",\"bodegaDestinoId\":" + origen + ",\"productos\":[null]}", 400);
        enviar("POST", "/movimientos", empleado, "{\"tipo\":\"ENTRADA\",\"bodegaDestinoId\":" + origen + ",\"productos\":[]}", 400);
        JsonNode error = enviar("GET", "/productos/999999", empleado, null, 404);
        assertThat(error.has("fecha") && error.has("mensaje") && error.has("ruta")).isTrue();
        enviar("GET", "/productos/no-es-id", empleado, null, 400);
        enviar("GET", "/no-existe", empleado, null, 404);
    }
    @Test void noDuplicarProductoNiModificarStockDirectamente() throws Exception {
        String datos = "{\"tipo\":\"ENTRADA\",\"bodegaDestinoId\":" + origen + ",\"productos\":[{\"productoId\":" + producto + ",\"cantidad\":2},{\"productoId\":" + producto + ",\"cantidad\":3}]}";
        enviar("POST", "/movimientos", empleado, datos, 400);
        enviar("PUT", "/productos/" + producto, admin, "{\"nombre\":\"X\",\"categoria\":\"Y\",\"precio\":10,\"stock\":50}", 400);
    }
    @Test void crudBodegaYProductoSinHistorial() throws Exception {
        enviar("PUT", "/bodegas/" + origen, admin, "{\"nombre\":\"Centro\",\"ubicacion\":\"Cali\",\"capacidad\":50,\"encargado\":\"Ana\"}", 200);
        assertThat(enviar("GET", "/bodegas/" + origen, empleado, null, 200).get("nombre").asText()).isEqualTo("Centro");
        enviar("DELETE", "/bodegas/" + origen, admin, null, 204);
        enviar("GET", "/bodegas/" + origen, empleado, null, 404);
        enviar("DELETE", "/productos/" + otro, admin, null, 204);
        enviar("GET", "/productos/" + otro, empleado, null, 404);
    }
    @Test void openApiIncluyeJwtYTodasLasRutas() throws Exception {
        JsonNode doc = enviar("GET", "/v3/api-docs", null, null, 200);
        assertThat(doc.at("/components/securitySchemes/bearerAuth/scheme").asText()).isEqualTo("bearer");
        assertThat(doc.get("paths").has("/reportes/resumen")).isTrue();
        assertThat(doc.get("paths").get("/productos").get("get").get("responses").has("200")).isTrue();
        assertThat(doc.get("paths").get("/productos").get("post").get("responses").has("201")).isTrue();
        assertThat(doc.get("paths").get("/productos/{id}").get("delete").get("responses").has("204")).isTrue();
        assertThat(doc.get("paths").get("/auth/login").get("post").get("security")).isNullOrEmpty();
    }
}
