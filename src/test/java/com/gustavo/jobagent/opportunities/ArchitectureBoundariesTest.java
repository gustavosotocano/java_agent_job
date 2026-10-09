package com.gustavo.jobagent.opportunities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ArchitectureBoundariesTest {

    @Test
    @DisplayName("El dominio no importa Spring, JPA ni Hibernate")
    void dominioNoDependeDeInfraestructura() throws IOException {
        Path dominio = Path.of("src/main/java/com/gustavo/jobagent/opportunities/domain");
        try (Stream<Path> archivos = Files.walk(dominio)) {
            archivos.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                String fuente;
                try {
                    fuente = Files.readString(path);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
                assertThat(fuente).doesNotContain("org.springframework");
                assertThat(fuente).doesNotContain("jakarta.persistence");
                assertThat(fuente).doesNotContain("org.hibernate");
            });
        }
    }
}
