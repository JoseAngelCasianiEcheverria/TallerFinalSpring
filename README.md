# TallerAutos — API REST AutoDrive Motors

Taller final de **Análisis y Diseño de Software**. API REST para gestionar las
ventas de vehículos de una concesionaria ficticia, **AutoDrive Motors**, que hoy
administra su información en hojas de cálculo y documentos físicos.

---

## Qué resuelve

Ocho problemas del enunciado, y cómo los resuelve la API:

| Problema | Cómo lo resuelve |
|---|---|
| Pérdida de datos de clientes | `cliente` con correo único, en base relacional |
| Errores en los precios | `CHECK (precio >= 0)` en la base y `@PositiveOrZero` en el DTO |
| Ventas duplicadas | Bloqueo pesimista del vehículo dentro de la transacción de venta |
| No saber qué está disponible | `GET /api/vehiculos/disponibles`, y el estado lo deduce el sistema |
| Historial de mantenimiento | `GET /api/mantenimientos/vehiculo/{id}`, ordenado del más reciente al más antiguo |
| Retrasos en la atención | Índices en las cuatro tablas, latencia por debajo de 500 ms |
| Control de lo vendido | El estado pasa a `VENDIDO` al registrar la venta, no a mano |
| Reportes rápidos | Las tablas ya son el reporte: `GET /api/ventas` con el cliente y el vehículo resueltos |

---

## Stack

| Pieza | Versión |
|---|---|
| Java | 17 (Temurin 17.0.20.101-hotspot) |
| Spring Boot | 3.3.5 |
| Spring Data JPA / Hibernate | el del starter |
| PostgreSQL | 18 |
| Maven | 3.9.16 |
| Frontend | HTML y JS planos, sin framework — **añadido propio, el enunciado pide backend** |
| Pruebas | Postman + 7 pruebas unitarias (JUnit) |

**Sin Lombok**: getters, setters, constructores y el mapeo Entity ↔ DTO son
manuales. Ver la razón en el vault (`ERR-LOMBOK_Java25`).

---

## Cómo se levanta

### 1. Requisitos

```powershell
mvn -v
```

Debe decir **`Java version: 17.0.20.1`**. En esta máquina ya viene así.

PostgreSQL 18 debe estar corriendo:

```powershell
Get-Service postgresql-x64-18      # Status: Running
```

### 2. La base de datos

Crear la base:

```powershell
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -h localhost -c "CREATE DATABASE tallerautos;"
```

> El nombre va **en minúscula**. PostgreSQL pliega a minúscula los identificadores
> sin comillas, y la URL de JDBC compara literal: `tallerAutos` daría
> *"no existe la base de datos"*.

Crear el esquema, que está en [`schema.sql`](schema.sql):

```powershell
$env:PGPASSWORD = "<tu clave>"
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -h localhost -d tallerautos -f schema.sql
```

El esquema **no lo genera Hibernate**: se ejecuta a mano y la aplicación queda
con `ddl-auto=validate`, que hace que no arranque si el código y el esquema no
coinciden, en vez de producir datos raros en silencio.

### 3. Arrancar

```powershell
cd C:\Users\josea\Desktop\TallerAutos
$env:DB_PASSWORD = "<tu clave>"

mvn spring-boot:run
```

Sin la clave la app no arranca:

```
The server requested SCRAM-based authentication, but no password was provided.
```

Es deliberado. `application.properties` la lee como `${DB_PASSWORD:}`, así que
fallar al principio es preferible a fallar más tarde y más difícil.

Espera:

```
Tomcat started on port 8080 (http) with context path '/'
Started TallerAutosApplication in 8.1 seconds
```

---

## Cómo se usa

Es una **API REST**: no hay ventana que abrir. `GET /` devuelve 404 salvo por la
página estática.

### La página web

Abre **`http://localhost:8080/`** en el navegador. Cuatro pestañas:

| Pestaña | Qué hace |
|---|---|
| **Inicio** | Contadores de clientes, vehículos, disponibles, ventas y mantenimientos, y la tasa de cambio del día con su origen |
| **Clientes** | Alta, edición y borrado |
| **Vehículos** | Catálogo con filtro por marca y "solo disponibles", y alta, edición y borrado |
| **Ventas** | Elegir cliente y vehículo, **ver el total con descuento antes de vender**, y registrar la venta |

> Ábrela por `http://localhost`, **nunca con doble clic** en el archivo. Desde
> `file://` el navegador bloquea las peticiones a la API.

> Tras cambiar el HTML o el JS, recarga con **`Ctrl+F5`**, no con `F5`.

### Postman

Importa las dos unidades de la raíz del repositorio:

- `Postman_AutoDrive.postman_collection.json` — 51 peticiones en 8 carpetas, con sus assertions
- `Postman_AutoDrive.postman_environment.json` — dos variables: `base_url` y `run`

**Corre las carpetas en orden.** La `01 - Datos base` guarda sola los ids que el
resto usa, y la `05` tiene una dependencia invisible: la prueba de "vender un
vehículo en mantenimiento → 409" está **dentro de la 05**, antes de "Cerrar
mantenimiento". Si la mueves a la `06`, falla.

Sube `run` (`02`, `03`…) si ya creaste los datos de prueba, o las primeras
peticiones devolverán 409.

---

## Endpoints

Prefijo `/api`. **Son 23**; el enunciado exige 11 como mínimo.

### Clientes

```
GET    /api/clientes
POST   /api/clientes
GET    /api/clientes/{id}
PUT    /api/clientes/{id}
DELETE /api/clientes/{id}
```

### Vehículos

```
GET  /api/vehiculos
POST /api/vehiculos
GET  /api/vehiculos/{id}
PUT  /api/vehiculos/{id}
DELETE /api/vehiculos/{id}
GET  /api/vehiculos/disponibles
GET  /api/vehiculos/marca/{marca}
```

### Ventas

```
POST /api/ventas
GET  /api/ventas
GET  /api/ventas/cliente/{clienteId}
GET  /api/ventas/vehiculo/{vehiculoId}
GET  /api/ventas/simular/{vehiculoId}     <- añadido propio, para la página
```

No hay `DELETE /api/ventas`: las ventas son el histórico del negocio.

### Mantenimientos

```
POST   /api/mantenimientos
GET    /api/mantenimientos
GET    /api/mantenimientos/vehiculo/{vehiculoId}
PUT    /api/mantenimientos/{id}
PATCH  /api/mantenimientos/{id}/cerrar
```

### Tasa de cambio

```
GET /api/tasa-cambio
```

---

## Las 6 reglas de negocio

| # | Regla | Dónde vive | Respuesta |
|---|---|---|---|
| RN-1 | No se puede vender un vehículo `VENDIDO` ni `EN_MANTENIMIENTO` | `VentaService` con bloqueo de fila | 409 |
| RN-2 | No se permiten precios negativos | Bean Validation + `CHECK` en la base | 400 |
| RN-3 | La placa es única | `existsByPlaca` + `UNIQUE` | 409 |
| RN-4 | El correo del cliente es único | `existsByEmail` + `UNIQUE` | 409 |
| RN-5 | Toda venta genera automáticamente fecha y total | El **DTO de entrada no tiene esos campos** | — |
| RN-6 | Si el valor supera $100.000.000 COP, 5 % de descuento | `VentaService.calcularTotal()` | — |

Dos detalles que conviene explicar:

**RN-1 no se resuelve con un `if`.** Sin bloqueo pesimista del vehículo, dos
vendedores leen el mismo `DISPONIBLE` antes de que ninguno escriba y los dos
pasan la validación. Un `if` no lo evita; hace falta
`@Lock(PESSIMISTIC_WRITE)`.

**RN-5 se cumple por ausencia de campo.** `VentaRequest` solo tiene `clienteId` y
`vehiculoId`. No es que el servicio ignore lo que venga: es que no existe dónde
mandarlo.

**RN-6 es estrictamente mayor.** Exactamente $100.000.000 **no** lleva descuento,
porque el enunciado dice "supera". Y la comparación es con `compareTo`, no con
`equals`: en `BigDecimal` `equals` también compara la escala, así que
`100000000` y `100000000.00` serían distintos.

---

## Cuando la API externa se cae

`open.er-api.com/v6/latest/USD` es la fuente de la tasa. Si no responde, **la
venta se guarda igual**, con `precio_usd` en `null` y un aviso en el log. Una
venta real no se pierde porque un servicio de terceros esté caído.

`GET /api/tasa-cambio` dice de dónde salió la cifra: `api-externa` o
`valor-por-defecto`.

---

## Estructura

```
src/main/java/com/tallerautos/
├── TallerAutosApplication.java
├── config/       DatosIniciales (apagable con datos.iniciales.activar=false)
├── controller/   5 clases, 20 endpoints
├── client/       TasaCambioClient (RestClient -> open.er-api.com)
├── dto/          request/ y response/ separados de las entidades
├── entity/       4 entidades + 2 enums
├── exception/    6 excepciones + GlobalExceptionHandler
├── mapper/       4 mappers Entity <-> DTO
├── repository/   4 repositorios
└── service/      5 servicios

src/main/resources/
├── application.properties
└── static/           <- la página web (añadido propio)
    ├── index.html
    ├── css/estilos.css
    └── js/app.js

src/test/java/com/tallerautos/
├── ContextoApplicationTest.java    arranca el contexto y valida los beans
└── service/VentaServiceTest.java   6 pruebas de RN-6

schema.sql                             el CREATE TABLE, se ejecuta a mano
Postman_AutoDrive.postman_collection.json
Postman_AutoDrive.postman_environment.json
```

---

## Pruebas

```powershell
mvn clean test
```

```
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Prueba | Qué cubre |
|---|---|
| `VentaServiceTest` (6) | RN-6: por debajo del umbral, en el umbral exacto, un peso por encima, el total con y sin descuento, y por qué `compareTo` y no `equals` |
| `ContextoApplicationTest` (1) | Que los 18 beans estén cableados, con H2 en memoria para no depender de PostgreSQL |

Las 11 verificaciones de extremo a extremo (V1–V11) se corren **en Postman**,
que es lo que pide el enunciado. La matriz está en el vault, en
`documentacion/requisitos/trazabilidad.md`.

---

## La documentación completa está en el vault

`C:\Users\josea\Desktop\Mi_Cerebro\Mi_Cerebro\Proyectos\10_TallerAutos\`

| Nota | Qué contiene |
|---|---|
| `_Index.md` | Ficha técnica, stack, decisiones, estado |
| `Estrategias.md` | Por qué cada decisión técnica |
| `Guia_Config_Entorno.md` | Levantar el entorno desde cero, y los problemas comunes |
| `documentacion/` | Plan documental, visión, ERS, historias de usuario, glosario, trazabilidad y modelo de datos |
| `diagramas/` | Diagramas UML y el DER en notación Chen, en `.drawio` y `.png` |
| `Errors/` | Cinco errores encontrados, con causa raíz y solución |
