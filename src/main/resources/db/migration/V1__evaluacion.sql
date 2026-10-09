CREATE TABLE evaluacion (
    id VARCHAR(64) PRIMARY KEY,
    id_oferta VARCHAR(255) NOT NULL,
    id_perfil VARCHAR(255) NOT NULL,
    estado VARCHAR(32) NOT NULL,
    fecha_evaluacion TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT evaluacion_estado_chk CHECK (estado IN ('COMPATIBLE', 'COMPATIBLE_CON_BRECHAS', 'NO_COMPATIBLE'))
);

CREATE TABLE evaluacion_coincidencia (
    evaluacion_id VARCHAR(64) NOT NULL REFERENCES evaluacion (id),
    orden INTEGER NOT NULL,
    aspecto VARCHAR(32) NOT NULL,
    detalle TEXT NOT NULL,
    evidencia TEXT NOT NULL,
    PRIMARY KEY (evaluacion_id, orden),
    CONSTRAINT evaluacion_coincidencia_aspecto_chk CHECK (aspecto IN ('TECNOLOGIA', 'MODALIDAD', 'UBICACION', 'SALARIO', 'EXPERIENCIA'))
);

CREATE TABLE evaluacion_brecha (
    evaluacion_id VARCHAR(64) NOT NULL REFERENCES evaluacion (id),
    orden INTEGER NOT NULL,
    aspecto VARCHAR(32) NOT NULL,
    detalle TEXT NOT NULL,
    es_bloqueante BOOLEAN NOT NULL,
    PRIMARY KEY (evaluacion_id, orden),
    CONSTRAINT evaluacion_brecha_aspecto_chk CHECK (aspecto IN ('TECNOLOGIA', 'MODALIDAD', 'UBICACION', 'SALARIO', 'EXPERIENCIA'))
);

CREATE TABLE evaluacion_dato_pendiente (
    evaluacion_id VARCHAR(64) NOT NULL REFERENCES evaluacion (id),
    orden INTEGER NOT NULL,
    aspecto VARCHAR(32) NOT NULL,
    pregunta TEXT NOT NULL,
    PRIMARY KEY (evaluacion_id, orden),
    CONSTRAINT evaluacion_dato_pendiente_aspecto_chk CHECK (aspecto IN ('TECNOLOGIA', 'MODALIDAD', 'UBICACION', 'SALARIO', 'EXPERIENCIA'))
);

CREATE TABLE evaluacion_oferta_evidencia (
    evaluacion_id VARCHAR(64) PRIMARY KEY REFERENCES evaluacion (id),
    id_fuente VARCHAR(255) NOT NULL,
    fuente_id VARCHAR(255) NOT NULL,
    titulo TEXT NOT NULL,
    empresa TEXT,
    descripcion TEXT,
    ubicacion TEXT,
    modalidad VARCHAR(16),
    url_original TEXT,
    url_normalizada TEXT,
    fecha_publicacion TIMESTAMP(6) WITH TIME ZONE,
    salario_minimo NUMERIC,
    salario_maximo NUMERIC,
    salario_moneda VARCHAR(16),
    salario_periodo VARCHAR(16),
    CONSTRAINT evaluacion_oferta_modalidad_chk CHECK (modalidad IS NULL OR modalidad IN ('REMOTE', 'HYBRID', 'ONSITE')),
    CONSTRAINT evaluacion_oferta_periodo_chk CHECK (salario_periodo IS NULL OR salario_periodo IN ('HORA', 'MENSUAL', 'ANUAL'))
);

CREATE TABLE evaluacion_oferta_tecnologia (
    evaluacion_id VARCHAR(64) NOT NULL REFERENCES evaluacion_oferta_evidencia (evaluacion_id),
    orden INTEGER NOT NULL,
    tecnologia VARCHAR(255) NOT NULL,
    PRIMARY KEY (evaluacion_id, orden)
);

CREATE TABLE evaluacion_perfil_evidencia (
    evaluacion_id VARCHAR(64) PRIMARY KEY REFERENCES evaluacion (id),
    perfil_id VARCHAR(255) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    titulo_principal VARCHAR(255) NOT NULL,
    ubicacion TEXT,
    anos_experiencia INTEGER,
    salario_minimo NUMERIC,
    salario_maximo NUMERIC,
    salario_moneda VARCHAR(16),
    salario_periodo VARCHAR(16),
    CONSTRAINT evaluacion_perfil_periodo_chk CHECK (salario_periodo IS NULL OR salario_periodo IN ('HORA', 'MENSUAL', 'ANUAL'))
);

CREATE TABLE evaluacion_perfil_tecnologia (
    evaluacion_id VARCHAR(64) NOT NULL REFERENCES evaluacion_perfil_evidencia (evaluacion_id),
    orden INTEGER NOT NULL,
    tecnologia VARCHAR(255) NOT NULL,
    PRIMARY KEY (evaluacion_id, orden)
);

CREATE TABLE evaluacion_perfil_modalidad (
    evaluacion_id VARCHAR(64) NOT NULL REFERENCES evaluacion_perfil_evidencia (evaluacion_id),
    orden INTEGER NOT NULL,
    modalidad VARCHAR(16) NOT NULL,
    PRIMARY KEY (evaluacion_id, orden),
    CONSTRAINT evaluacion_perfil_modalidad_chk CHECK (modalidad IN ('REMOTE', 'HYBRID', 'ONSITE'))
);
