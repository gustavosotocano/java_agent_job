CREATE TABLE registro_busqueda (
    id VARCHAR(36) PRIMARY KEY,
    ejecutado_en TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    cargo TEXT NOT NULL,
    ubicacion TEXT,
    modalidad VARCHAR(16),
    limite INTEGER NOT NULL,
    resultado VARCHAR(32) NOT NULL,
    cantidad_ofertas_devueltas INTEGER NOT NULL,
    CONSTRAINT registro_busqueda_resultado_chk CHECK (resultado IN (
        'CON_RESULTADOS', 'RESULTADO_VACIO', 'FALLO_PARCIAL', 'BUSQUEDA_FALLIDA', 'SIN_FUENTES_HABILITADAS'
    )),
    CONSTRAINT registro_busqueda_modalidad_chk CHECK (modalidad IS NULL OR modalidad IN ('REMOTE', 'HYBRID', 'ONSITE'))
);

CREATE TABLE registro_busqueda_tecnologia (
    registro_id VARCHAR(36) NOT NULL REFERENCES registro_busqueda (id),
    orden INTEGER NOT NULL,
    tecnologia VARCHAR(255) NOT NULL,
    PRIMARY KEY (registro_id, orden)
);

CREATE TABLE registro_busqueda_fuente (
    registro_id VARCHAR(36) NOT NULL REFERENCES registro_busqueda (id),
    orden INTEGER NOT NULL,
    fuente_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (registro_id, orden)
);

CREATE TABLE registro_busqueda_advertencia (
    registro_id VARCHAR(36) NOT NULL REFERENCES registro_busqueda (id),
    orden INTEGER NOT NULL,
    fuente_id VARCHAR(255) NOT NULL,
    mensaje TEXT NOT NULL,
    PRIMARY KEY (registro_id, orden)
);

CREATE TABLE fuente_configurada (
    fuente_id VARCHAR(255) PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    estado VARCHAR(32) NOT NULL,
    habilitada BOOLEAN NOT NULL,
    CONSTRAINT fuente_configurada_estado_chk CHECK (estado IN ('DISPONIBLE', 'PENDIENTE', 'DESHABILITADA'))
);

INSERT INTO fuente_configurada (fuente_id, nombre, estado, habilitada) VALUES
    ('SIMULADA', 'Fuente simulada', 'DISPONIBLE', TRUE),
    ('LINKEDIN', 'LinkedIn', 'PENDIENTE', FALSE),
    ('INDEED', 'Indeed', 'PENDIENTE', FALSE),
    ('GLASSDOOR', 'Glassdoor', 'PENDIENTE', FALSE);
