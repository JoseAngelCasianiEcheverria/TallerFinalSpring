
BEGIN;


TRUNCATE TABLE venta, mantenimiento, vehiculo, cliente RESTART IDENTITY CASCADE;

COMMIT;


SELECT 'cliente' AS tabla, COUNT(*) AS filas FROM cliente
UNION ALL SELECT 'vehiculo',       COUNT(*) FROM vehiculo
UNION ALL SELECT 'mantenimiento',  COUNT(*) FROM mantenimiento
UNION ALL SELECT 'venta',          COUNT(*) FROM venta
ORDER BY tabla;