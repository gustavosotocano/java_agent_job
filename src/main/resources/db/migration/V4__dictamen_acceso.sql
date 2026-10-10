CREATE TABLE dictamen_acceso (
    fuente_id VARCHAR(32) PRIMARY KEY,
    via_comprobada BOOLEAN NOT NULL,
    motivo TEXT,
    clase_acceso VARCHAR(40),
    cobertura TEXT,
    limites TEXT,
    actualidad TEXT,
    verificado_en TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT dictamen_fuente_chk CHECK (fuente_id IN ('LINKEDIN', 'INDEED', 'GLASSDOOR')),
    CONSTRAINT dictamen_clase_chk CHECK (
        clase_acceso IS NULL
        OR clase_acceso IN ('LISTADO_PUBLICO', 'FEED', 'PROVEEDOR_AUTORIZADO', 'BUSQUEDA_PUBLICA_INDEXADA')
    ),
    CONSTRAINT dictamen_no_comprobada_chk CHECK (
        via_comprobada
        OR (
            motivo IS NOT NULL
            AND btrim(motivo) <> ''
            AND clase_acceso IS NULL
            AND cobertura IS NULL
            AND limites IS NULL
            AND actualidad IS NULL
        )
    ),
    CONSTRAINT dictamen_comprobada_chk CHECK (
        NOT via_comprobada
        OR (
            clase_acceso IS NOT NULL
            AND cobertura IS NOT NULL
            AND btrim(cobertura) <> ''
            AND limites IS NOT NULL
            AND btrim(limites) <> ''
            AND actualidad IS NOT NULL
            AND btrim(actualidad) <> ''
        )
    )
);

INSERT INTO dictamen_acceso (fuente_id, via_comprobada, motivo, verificado_en) VALUES
    (
        'LINKEDIN',
        FALSE,
        'La API oficial publicada es de socios para publicar avisos y postulaciones, no para buscar. No hay acuerdo ni una API pública de búsqueda',
        TIMESTAMP WITH TIME ZONE '2026-10-10T00:00:00Z'
    ),
    (
        'INDEED',
        FALSE,
        'La búsqueda pública de publishers está retirada. Las APIs actuales de socios envían avisos y postulaciones hacia Indeed y exigen alta',
        TIMESTAMP WITH TIME ZONE '2026-10-10T00:00:00Z'
    ),
    (
        'GLASSDOOR',
        FALSE,
        'La API de socios que exponía empleos ya no atiende y no acepta altas nuevas. La vía empresarial reparte avisos desde un ATS, no permite buscar',
        TIMESTAMP WITH TIME ZONE '2026-10-10T00:00:00Z'
    );
