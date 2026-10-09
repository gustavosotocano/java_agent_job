CREATE TABLE oferta (
    id VARCHAR(36) PRIMARY KEY,
    fuente_id VARCHAR(255) NOT NULL,
    url_normalizada TEXT NOT NULL,
    id_fuente VARCHAR(255) NOT NULL,
    titulo TEXT NOT NULL,
    empresa TEXT,
    descripcion TEXT,
    ubicacion TEXT,
    modalidad VARCHAR(16),
    url_original TEXT NOT NULL,
    fecha_publicacion TIMESTAMP(6) WITH TIME ZONE,
    salario_minimo NUMERIC,
    salario_maximo NUMERIC,
    salario_moneda VARCHAR(16),
    salario_periodo VARCHAR(16),
    CONSTRAINT oferta_identidad_uk UNIQUE (fuente_id, url_normalizada),
    CONSTRAINT oferta_modalidad_chk CHECK (modalidad IS NULL OR modalidad IN ('REMOTE', 'HYBRID', 'ONSITE')),
    CONSTRAINT oferta_periodo_chk CHECK (salario_periodo IS NULL OR salario_periodo IN ('HORA', 'MENSUAL', 'ANUAL'))
);

CREATE TABLE oferta_tecnologia (
    oferta_id VARCHAR(36) NOT NULL REFERENCES oferta (id),
    orden INTEGER NOT NULL,
    tecnologia VARCHAR(255) NOT NULL,
    PRIMARY KEY (oferta_id, orden)
);

CREATE TABLE procedencia (
    id VARCHAR(36) PRIMARY KEY,
    oferta_id VARCHAR(36) NOT NULL REFERENCES oferta (id),
    capturado_en TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    id_fuente VARCHAR(255) NOT NULL,
    url_original TEXT NOT NULL,
    titulo TEXT NOT NULL,
    empresa TEXT,
    descripcion TEXT,
    ubicacion TEXT,
    modalidad VARCHAR(16),
    fecha_publicacion TIMESTAMP(6) WITH TIME ZONE,
    salario_minimo NUMERIC,
    salario_maximo NUMERIC,
    salario_moneda VARCHAR(16),
    salario_periodo VARCHAR(16),
    CONSTRAINT procedencia_modalidad_chk CHECK (modalidad IS NULL OR modalidad IN ('REMOTE', 'HYBRID', 'ONSITE')),
    CONSTRAINT procedencia_periodo_chk CHECK (salario_periodo IS NULL OR salario_periodo IN ('HORA', 'MENSUAL', 'ANUAL'))
);

CREATE TABLE procedencia_tecnologia (
    procedencia_id VARCHAR(36) NOT NULL REFERENCES procedencia (id),
    orden INTEGER NOT NULL,
    tecnologia VARCHAR(255) NOT NULL,
    PRIMARY KEY (procedencia_id, orden)
);
