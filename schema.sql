
















CREATE TABLE cliente (
    id             BIGSERIAL    PRIMARY KEY,
    nombre         VARCHAR(100) NOT NULL,
    email          VARCHAR(120) NOT NULL,
    telefono       VARCHAR(30),
    direccion      VARCHAR(200),
    fecha_registro TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT uq_cliente_email UNIQUE (email)
);


CREATE TABLE vehiculo (
    id            BIGSERIAL       PRIMARY KEY,
    placa         VARCHAR(10)     NOT NULL,
    marca         VARCHAR(50)     NOT NULL,
    modelo        VARCHAR(80)     NOT NULL,
    anio          INTEGER,
    color         VARCHAR(40),
    precio        NUMERIC(15, 2)  NOT NULL,
    estado        VARCHAR(20)     NOT NULL DEFAULT 'DISPONIBLE',
    fecha_ingreso TIMESTAMP       NOT NULL DEFAULT now(),
    CONSTRAINT uq_vehiculo_placa  UNIQUE (placa),
    CONSTRAINT ck_vehiculo_precio CHECK (precio >= 0),
    CONSTRAINT ck_vehiculo_estado CHECK (estado IN ('DISPONIBLE', 'EN_MANTENIMIENTO', 'VENDIDO'))
);


CREATE INDEX idx_vehiculo_marca ON vehiculo (lower(marca));
CREATE INDEX idx_vehiculo_estado ON vehiculo (estado);






CREATE TABLE venta (
    id                 BIGSERIAL      PRIMARY KEY,
    cliente_id         BIGINT         NOT NULL,
    vehiculo_id        BIGINT         NOT NULL,
    fecha_venta        TIMESTAMP      NOT NULL DEFAULT now(),
    valor_vehiculo     NUMERIC(15, 2) NOT NULL,
    descuento_aplicado NUMERIC(5, 2)  NOT NULL DEFAULT 0,
    total              NUMERIC(15, 2) NOT NULL,
    tasa_cambio        NUMERIC(12, 4),
    precio_usd         NUMERIC(15, 2),
    CONSTRAINT fk_venta_cliente  FOREIGN KEY (cliente_id)  REFERENCES cliente (id),
    CONSTRAINT fk_venta_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculo (id),
    CONSTRAINT ck_venta_valor    CHECK (valor_vehiculo >= 0),
    CONSTRAINT ck_venta_total    CHECK (total >= 0),
    CONSTRAINT ck_venta_desc     CHECK (descuento_aplicado IN (0, 5))
);

CREATE INDEX idx_venta_vehiculo ON venta (vehiculo_id);
CREATE INDEX idx_venta_cliente  ON venta (cliente_id);







CREATE TABLE mantenimiento (
    id           BIGSERIAL      PRIMARY KEY,
    vehiculo_id  BIGINT         NOT NULL,
    tipo         VARCHAR(50)    NOT NULL,
    descripcion  TEXT,
    costo        NUMERIC(12, 2) NOT NULL,
    fecha_inicio TIMESTAMP      NOT NULL DEFAULT now(),
    fecha_fin    TIMESTAMP,
    estado       VARCHAR(20)    NOT NULL DEFAULT 'EN_PROCESO',
    CONSTRAINT fk_mantenimiento_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculo (id),
    CONSTRAINT ck_mantenimiento_costo   CHECK (costo >= 0),
    CONSTRAINT ck_mantenimiento_estado  CHECK (estado IN ('EN_PROCESO', 'FINALIZADO'))
);


CREATE INDEX idx_mantenimiento_vehiculo ON mantenimiento (vehiculo_id, fecha_inicio DESC);
