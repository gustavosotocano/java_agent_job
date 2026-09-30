# Job Agent — base Spring Boot 4

Proyecto educativo: un agente que busca ofertas públicas en fuentes configurables, las evalúa frente a un perfil y explica coincidencias, brechas y datos pendientes.

## Estado actual: M0

Solo están implementados el arranque, configuración, endpoint informativo y health check. La búsqueda en portales públicos está incluida en el plan; todavía no hay adaptadores reales, evaluación, LLM, base de datos ni herramientas del agente.

Stack: Java 17+, Spring Boot 4.0.8, Maven Wrapper 3.9.11, Spring MVC, Validation, Actuator y JUnit. Java 17 permite compilar esta base en el entorno disponible; podemos migrar a Java 21 antes de introducir características que lo requieran.

## Ejecutar

En macOS/Linux:

```bash
chmod +x mvnw
./mvnw clean verify
./mvnw spring-boot:run
```

En Windows: `mvnw.cmd clean verify` y `mvnw.cmd spring-boot:run`.
La primera ejecución requiere Internet para descargar Maven y dependencias.

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/v1/project
```

Importa `pom.xml` en IntelliJ o abre la carpeta en VS Code. No necesitas claves API para M0.

## Organización prevista

- `opportunities/domain`: entidades, value objects, aggregate y eventos; Java puro.
- `opportunities/application/port/in`: contratos de casos de uso.
- `opportunities/application/port/out`: contratos para persistencia y modelo de lenguaje.
- `opportunities/application/service`: orquestación de casos de uso.
- `opportunities/adapter/in/web`: API REST; hoy contiene el endpoint informativo.
- `opportunities/adapter/out`: implementaciones para memoria, persistencia y LLM.
- `configuration`: composición e inyección con Spring.

Los paquetes vacíos llevan `package-info.java` para documentar su propósito; no representan funcionalidades implementadas.

## Alcance de búsqueda

Fuentes candidatas: LinkedIn, Indeed y Glassdoor, sin cuentas personales. Comenzaremos con una fuente simulada; cada integración real depende de verificar su mecanismo de acceso. El plan incluye filtros, normalización, deduplicación, procedencia y fallos parciales.

Consulta [PLAN.md](PLAN.md) para los hitos y criterios de aceptación.
