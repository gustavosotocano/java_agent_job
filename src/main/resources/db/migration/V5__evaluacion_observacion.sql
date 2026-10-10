CREATE TABLE evaluacion_observacion (
    evaluacion_id VARCHAR(64) NOT NULL REFERENCES evaluacion (id),
    orden INTEGER NOT NULL,
    texto TEXT NOT NULL,
    fragmento TEXT NOT NULL,
    PRIMARY KEY (evaluacion_id, orden),
    CONSTRAINT evaluacion_observacion_texto_chk CHECK (length(btrim(texto)) > 0),
    CONSTRAINT evaluacion_observacion_fragmento_chk CHECK (length(btrim(fragmento)) > 0)
);
