# LogiTrack 📦

Sistema de gestión de bodegas desarrollado con Spring Boot. Permite administrar productos, registrar movimientos de inventario y consultar auditorías.

## Tecnologías

- Java 17 y Spring Boot
- Spring Data JPA y MySQL
- Spring Security y JWT
- Swagger / OpenAPI
- HTML, CSS y JavaScript

## Funcionalidades

- CRUD de bodegas y productos.
- Entradas, salidas y transferencias entre bodegas.
- Validación de stock disponible y capacidad.
- Auditoría automática de cambios.
- Consultas de stock bajo y movimientos por fechas.
- Reporte de stock por bodega y productos más movidos.

## Roles

- **ADMIN:** administra bodegas y productos, consulta auditorías y realiza movimientos.
- **EMPLEADO:** consulta inventario y reportes, y registra movimientos.

## Instalación

1. Descarga el proyecto y ábrelo en IntelliJ como proyecto Maven.
2. Selecciona Java 17.
3. Crea la base de datos en MySQL:

```sql
CREATE DATABASE logitrack;
```

4. En la configuración de ejecución de IntelliJ, agrega estas variables de entorno:

```text
DB_USER=tu_usuario_mysql
DB_PASSWORD=tu_contraseña_mysql
JWT_SECRET=tu_clave_aleatoria_de_al_menos_32_caracteres
```

5. Ejecuta `LogitrackApplication`.

Los scripts `schema.sql` y `data.sql` crean las tablas y cargan datos iniciales automáticamente.

## Acceso

- **Interfaz:** http://localhost:8080
- **Swagger:** http://localhost:8080/swagger-ui.html

Para probar rutas protegidas en Swagger, inicia sesión en `/auth/login`, copia el token y pégalo en **Authorize**, sin escribir `Bearer`.

## Crear un administrador

Las cuentas nuevas tienen rol EMPLEADO. Para cambiar el rol, reemplaza `tu_usuario` por el nombre registrado:

```sql
USE logitrack;

UPDATE usuario
SET rol = 'ADMIN'
WHERE nombre = 'tu_usuario';
```

Cierra sesión y vuelve a entrar para obtener los nuevos permisos.

## Organización

```text
src/main/java/com/logitrack/
├── controller/
├── service/
├── repository/
├── model/
├── dto/
├── mapper/
├── config/
├── security/
└── exception/

frontend/    → Interfaz web
docs/        → Documentación y capturas
```

## Pruebas

```bash
./mvnw test
```

En Windows: `mvnw.cmd test`. Las pruebas usan una base H2 independiente de MySQL.

**Nota:** el stock se modifica mediante movimientos para conservar su trazabilidad. No subas contraseñas ni claves reales al repositorio.


## Autor 

Vanessa Diaz :)