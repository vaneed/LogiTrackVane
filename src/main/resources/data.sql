-- Datos iniciales únicamente cuando la tabla está vacía.
-- No hay usuarios ni contraseñas predeterminadas: registra tu propia cuenta.
INSERT INTO bodega (nombre, ubicacion, capacidad, encargado)
SELECT nombre, ubicacion, capacidad, encargado FROM (
    SELECT 'Bodega Norte' AS nombre, 'Bogotá' AS ubicacion, 1000 AS capacidad, 'Laura' AS encargado
    UNION ALL
    SELECT 'Bodega Sur', 'Medellín', 800, 'Sofía'
) AS datos
WHERE NOT EXISTS (SELECT 1 FROM bodega);

INSERT INTO producto (nombre, categoria, stock, precio)
SELECT nombre, categoria, stock, precio FROM (
    SELECT 'Cuaderno pastel' AS nombre, 'Papelería' AS categoria, 0 AS stock, 12500.00 AS precio
    UNION ALL
    SELECT 'Marcador lila', 'Papelería', 0, 4500.00
) AS datos
WHERE NOT EXISTS (SELECT 1 FROM producto);
-- Las semillas SQL son de instalación. Los listeners auditan cambios hechos por JPA.
