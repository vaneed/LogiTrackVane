-- Se ejecuta al iniciar Spring Boot. Crea la base logitrack primero en MySQL.
CREATE TABLE IF NOT EXISTS usuario (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 nombre VARCHAR(50) NOT NULL UNIQUE,
 clave VARCHAR(100) NOT NULL,
 rol VARCHAR(20) NOT NULL CHECK (rol IN ('ADMIN','EMPLEADO'))
);
CREATE TABLE IF NOT EXISTS bodega (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 nombre VARCHAR(80) NOT NULL,
 ubicacion VARCHAR(150) NOT NULL,
 capacidad INT NOT NULL CHECK (capacidad > 0),
 encargado VARCHAR(80) NOT NULL
);
CREATE TABLE IF NOT EXISTS producto (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 nombre VARCHAR(80) NOT NULL,
 categoria VARCHAR(60) NOT NULL,
 stock INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
 precio DECIMAL(12,2) NOT NULL CHECK (precio > 0)
);
CREATE TABLE IF NOT EXISTS existencia (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 bodega_id BIGINT NOT NULL,
 producto_id BIGINT NOT NULL,
 cantidad INT NOT NULL DEFAULT 0 CHECK (cantidad >= 0),
 CONSTRAINT uk_existencia UNIQUE (bodega_id, producto_id),
 FOREIGN KEY (bodega_id) REFERENCES bodega(id),
 FOREIGN KEY (producto_id) REFERENCES producto(id)
);
CREATE TABLE IF NOT EXISTS movimiento (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 fecha DATETIME(6) NOT NULL,
 tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('ENTRADA','SALIDA','TRANSFERENCIA')),
 usuario VARCHAR(50) NOT NULL,
 bodega_origen_id BIGINT,
 bodega_destino_id BIGINT,
 FOREIGN KEY (usuario) REFERENCES usuario(nombre),
 FOREIGN KEY (bodega_origen_id) REFERENCES bodega(id),
 FOREIGN KEY (bodega_destino_id) REFERENCES bodega(id),
 CHECK ((tipo = 'ENTRADA' AND bodega_origen_id IS NULL AND bodega_destino_id IS NOT NULL)
     OR (tipo = 'SALIDA' AND bodega_origen_id IS NOT NULL AND bodega_destino_id IS NULL)
     OR (tipo = 'TRANSFERENCIA' AND bodega_origen_id IS NOT NULL AND bodega_destino_id IS NOT NULL AND bodega_origen_id <> bodega_destino_id))
);
CREATE TABLE IF NOT EXISTS detalle_movimiento (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 movimiento_id BIGINT NOT NULL,
 producto_id BIGINT NOT NULL,
 cantidad INT NOT NULL CHECK (cantidad > 0),
 UNIQUE (movimiento_id, producto_id),
 FOREIGN KEY (movimiento_id) REFERENCES movimiento(id),
 FOREIGN KEY (producto_id) REFERENCES producto(id)
);
CREATE TABLE IF NOT EXISTS auditoria (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 operacion VARCHAR(10) NOT NULL CHECK (operacion IN ('INSERT','UPDATE','DELETE')),
 fecha DATETIME(6) NOT NULL,
 usuario VARCHAR(50) NOT NULL,
 entidad VARCHAR(80) NOT NULL,
 valores_anteriores LONGTEXT,
 valores_nuevos LONGTEXT
);
