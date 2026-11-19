# Ejemplo educativo de JWT

Este token es **ficticio, está caducado y no sirve para iniciar sesión**. No procede de una cuenta real.

```text
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJsb2dpdHJhY2siLCJzdWIiOiJhbHVtbmFfZGVtbyIsInJvbCI6IkVNUExFQURPIiwiaWF0IjoxNzA0MDY3MjAwLCJleHAiOjE3MDQwNzA4MDB9.gOsbqUbSMtrw7yBUON0-xuZWO4TGBB-B-Cweizu5gbI
```

Payload:
```json
{"iss":"logitrack","sub":"alumna_demo","rol":"EMPLEADO","iat":1704067200,"exp":1704070800}
```

Clave pública de demostración: `clave-ficticia-solo-documentacion-32-bytes`. No la uses como JWT_SECRET. Para probar el proyecto, usa el token vigente que devuelve `/auth/login`:

```bash
curl http://localhost:8080/bodegas -H "Authorization: Bearer TU_TOKEN_REAL"
```

En Swagger pega únicamente el token en **Authorize**, sin el prefijo Bearer.
