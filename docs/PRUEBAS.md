# Evidencia de pruebas

Proyecto compilado con JDK 17, Maven 3.9.9 y Spring Boot 3.5.7.

## Pruebas automáticas incluidas
Archivo: `src/test/java/com/logitrack/LogitrackIntegrationTest.java`.
Resultado: **14 pruebas, 0 fallos, 0 errores, 0 omitidas**.

Usan Spring Boot completo, MockMvc, JWT firmado y H2 en modo MySQL. No modifican tu base de datos. Se ejecutan con `./mvnw test` o `.\mvnw.cmd test`.

| Caso | Resultado |
|---|---|
| Rutas protegidas: sin token, token inválido, rol insuficiente y login incorrecto | OK |
| Registro no permite seleccionar ADMIN; contraseña almacenada como hash | OK |
| Token vencido rechazado | OK |
| Entrada, salida, transferencia, existencias y reporte coherentes | OK |
| Stock insuficiente sin cambios ni auditorías parciales | OK |
| Fallo en segundo producto revierte también el primero | OK |
| Capacidad insuficiente, bodegas incorrectas y recursos inexistentes | OK |
| No reducir capacidad por debajo del stock ni borrar historial | OK |
| Auditoría INSERT/UPDATE/DELETE, usuario, antes y después | OK |
| Auditoría automática de cambios de stock | OK |
| Stock bajo, BETWEEN inclusivo y rangos inválidos | OK |
| Cantidades inválidas, lista vacía, detalle nulo, 400 y 404 JSON | OK |
| Productos duplicados y edición directa de stock rechazados | OK |
| CRUD sin historial y OpenAPI con esquema Bearer | OK |

Las filas resumen varios métodos/aserciones; el total real del runner es 14 tests. El manejador de 500 está implementado, pero no se provocó un fallo real de infraestructura para fotografiarlo.

## Ejecución con MySQL 8.4
Se utilizó una base temporal independiente de cualquier base de la alumna.

- Inicio correcto con `schema.sql`, `data.sql` y validación Hibernate.
- Registro, login, 401 sin token y 403 de EMPLEADO en auditorías.
- Entrada de 30, transferencia de 10 y salida de 3: stock total 27, Norte 20 y Sur 7.
- Salida de 21 desde Norte: 400; stock y número de auditorías sin cambios.
- **Concurrencia:** dos salidas simultáneas de 4 unidades con stock inicial 5. Una devolvió 201 y la otra 400. Stock final 1, nunca negativo.
- Auditoría de stock con usuario `ana`: valores anteriores 0 y nuevos 30.
- Filtros BETWEEN, stock bajo y reporte de unidades movidas.
- CRUD completo de bodega y producto sin historial.

## Interfaz y Swagger
Pruebas en Chromium:
- Login con usuario ADMIN.
- Resumen y filtro de stock bajo.
- Crear, editar y eliminar una bodega mediante formularios.
- Registrar una entrada y consultar auditorías.
- Vista móvil de 390 px sin desbordamiento horizontal de la página.
- Swagger: GET /productos devuelve 200 con JWT y 401 sin JWT.

Capturas:
1. [Login](capturas/01-login.png).
2. [Resumen con datos reales](capturas/02-resumen.png).
3. [Swagger/OpenAPI](capturas/03-swagger.png).
4. [Petición protegida con JWT: 200](capturas/04-swagger-jwt.png).
5. [Misma petición sin JWT: 401](capturas/05-swagger-sin-token.png).
6. [Vista móvil](capturas/06-movil.png).

La sección curl que contiene el token real se oculta en las capturas de Swagger. El cuerpo y código HTTP son las respuestas reales. Las cuentas y datos de estas capturas pertenecen a la base temporal de pruebas; **no** se crean como usuarios predeterminados en tu instalación. El resumen incluye además una entrada de 2 unidades realizada desde la interfaz.
