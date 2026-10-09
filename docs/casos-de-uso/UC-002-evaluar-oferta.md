# UC-002 — Evaluar oferta de empleo

- Estado: Implementado. La evaluación y su evidencia se recuperan después de reiniciar.
- Última actualización: 2026-10-09.
- Actor: Candidato en búsqueda de empleo.
- Hitos: Modelo de dominio y evaluación determinista en M1–M3. M4 persiste el agregado y las copias de perfil y oferta. El enriquecimiento con LLM sigue proyectado en M5.

## Objetivo y actor

**Actor:** Candidato en búsqueda activa o pasiva de empleo.

**Objetivo:** Analizar de forma determinista y explicable la compatibilidad entre una oferta laboral (`OfertaLaboral`) y el perfil profesional de un candidato (`PerfilProfesional`), generando un desglose estructurado de coincidencias (matches), brechas técnicas u operativas (gaps) y datos pendientes o ambiguos (questions / pending), clasificando el estado global de compatibilidad sin inferencias arbitrarias ni dependencias de IA.

## Alcance y exclusiones

### En alcance
- Contraste de tecnologías requeridas vs. dominadas (normalizadas, case-insensitive).
- Contraste de modalidad de trabajo (`REMOTE`, `HYBRID`, `ONSITE`) y ubicación.
- Contraste de expectativas salariales cuando ambos datos estén explícitamente informados con misma moneda y período.
- Detección de datos faltantes o no publicados como `DatoPendiente` (sin inventar salarios ni penalizar con cero).
- Generación del Aggregate Root `EvaluacionOferta` con trazabilidad temporal (`fechaEvaluacion`).
- Persistencia de solo inserción del agregado y de una copia inmutable del perfil y de la oferta usados en esa evaluación.
- Consulta de esa evaluación después de reiniciar el proceso, con salario ausente conservado como ausente.

### Exclusiones
- Inferencia mediante modelos LLM (se incorporará como puerto de enriquecimiento en Hito M5).
- Postulación automática a plataformas externas.
- Reescritura de una evaluación ya guardada. Una segunda evaluación del mismo par es otro agregado.
- Asunción arbitraria de porcentajes numéricos de compatibilidad.

## Precondiciones

1. Existe un `PerfilProfesional` válido con identificador, nombre y conjunto de tecnologías dominadas.
2. Existe una `OfertaLaboral` válida con identificador, cargo, tecnologías requeridas y modalidad.

## Entradas y salidas

### Entrada de comando de aplicación

```json
{
  "perfil": {
    "id": "perf-101",
    "nombre": "Gustavo Soto",
    "tituloPrincipal": "Senior Backend Developer",
    "tecnologias": ["Java", "Spring Boot", "Kafka", "PostgreSQL", "Docker"],
    "modalidadesAceptadas": ["REMOTE", "HYBRID"],
    "ubicacion": "Colombia",
    "expectativaSalarial": {
      "minimo": 4000,
      "maximo": 5500,
      "moneda": "USD",
      "periodo": "MENSUAL"
    }
  },
  "oferta": {
    "idFuente": "sim-001",
    "fuenteId": "SIMULADA",
    "titulo": "Senior Java Backend Engineer",
    "empresa": "Tech Solutions",
    "descripcion": "Java, Spring Boot, Kafka y AWS",
    "ubicacion": "Colombia",
    "modalidad": "REMOTE",
    "urlOriginal": "https://example.com/jobs/sim-001",
    "fechaPublicacion": "2026-09-30T10:00:00Z",
    "salario": {
      "minimo": 4500,
      "maximo": 6000,
      "moneda": "USD",
      "periodo": "MENSUAL"
    },
    "tecnologias": ["Java", "Spring Boot", "Kafka", "AWS"]
  }
}
```

### Salida estructurada de evaluación

```json
{
  "id": "eval-8f7a9d2c-3b1e-4c5a-9d8e-1f2a3b4c5d6e",
  "idOferta": "sim-001",
  "idPerfil": "perf-101",
  "estado": "COMPATIBLE_CON_BRECHAS",
  "coincidencias": [
    {
      "aspecto": "TECNOLOGIA",
      "detalle": "Tecnología coincidente: Java",
      "evidencia": "Requerida por la oferta y dominada por el candidato"
    },
    {
      "aspecto": "TECNOLOGIA",
      "detalle": "Tecnología coincidente: Spring Boot",
      "evidencia": "Requerida por la oferta y dominada por el candidato"
    },
    {
      "aspecto": "TECNOLOGIA",
      "detalle": "Tecnología coincidente: Kafka",
      "evidencia": "Requerida por la oferta y dominada por el candidato"
    },
    {
      "aspecto": "MODALIDAD",
      "detalle": "Modalidad compatible: REMOTE",
      "evidencia": "La oferta ofrece modalidad REMOTE, aceptada en el perfil del candidato"
    },
    {
      "aspecto": "SALARIO",
      "detalle": "Rango salarial compatible",
      "evidencia": "El salario de la oferta (USD 4500 - 6000 MENSUAL) cubre la expectativa (USD 4000 - 5500 MENSUAL)"
    }
  ],
  "brechas": [
    {
      "aspecto": "TECNOLOGIA",
      "detalle": "Tecnología faltante: AWS",
      "esBloqueante": false
    }
  ],
  "datosPendientes": [],
  "fechaEvaluacion": "2026-09-30T12:00:00Z"
}
```

## Flujo principal

1. El cliente invoca el caso de uso `EvaluarOfertaUseCase.evaluar(comando)`.
2. Se validan las invariantes del `PerfilProfesional` y de la `OfertaLaboral`.
3. Se invoca el método de dominio `EvaluacionOferta.evaluar(oferta, perfil)`.
4. El dominio ejecuta las reglas deterministas de contraste:
   - Tecnologías comunes se registran como `Coincidencia`.
   - Tecnologías de la oferta no presentes en el perfil se registran como `Brecha` (no bloqueante por defecto en evaluación base).
   - Se valida la compatibilidad de modalidad (`REMOTE`, `HYBRID`, `ONSITE`):
     - Si la modalidad de la oferta está dentro de las aceptadas por el perfil -> `Coincidencia`.
     - Si la modalidad no es aceptada -> `Brecha` bloqueante.
   - Se valida la ubicación:
     - Si la oferta es remota o la ubicación coincide parcialmente -> `Coincidencia`.
     - Si no es remota y las ubicaciones no coinciden -> `Brecha` bloqueante.
   - Se valida el aspecto salarial:
     - Si ambos publican salario en la misma moneda y período:
       - Si el máximo de la oferta >= mínimo del candidato -> `Coincidencia`.
       - Si el máximo de la oferta < mínimo del candidato -> `Brecha` salarial.
     - Si monedas o períodos difieren o alguno no publica salario -> `DatoPendiente` explicativo.
5. Se determina el `EstadoEvaluacion`:
   - `NO_COMPATIBLE`: si existe al menos una brecha bloqueante o no hay ninguna coincidencia tecnológica.
   - `COMPATIBLE_CON_BRECHAS`: si hay al menos una coincidencia y existen brechas no bloqueantes.
   - `COMPATIBLE`: si hay coincidencias y cero brechas.
6. Se persiste la `EvaluacionOferta` generada, junto con la copia del perfil y de la oferta del comando, a través de `EvaluacionOfertaRepository`. `idOferta` guarda `OfertaLaboral.idFuente()`.
7. Se retorna la `EvaluacionOferta` resultante. La consulta posterior devuelve el agregado y esas copias. Una evaluación nueva no modifica la anterior.

## Alternativas y errores

- **Perfil nulo o inválido:** Lanza `IllegalArgumentException` o excepción de validación.
- **Oferta nula o inválida:** Lanza `IllegalArgumentException`.
- **Oferta sin tecnologías especificadas:** Registra un `DatoPendiente` ("La oferta no especifica tecnologías requeridas").
- **Fallo al guardar en repositorio:** Lanza `RuntimeException` de persistencia sin alterar el cálculo de dominio.

## Reglas de negocio

1. **Determinismo:** El cálculo se basa exclusivamente en datos presentes en la oferta y el perfil.
2. **Normalización:** Las cadenas de tecnologías se comparan sin distinguir mayúsculas, minúsculas ni espacios redundantes.
3. **Ausencia de datos:** Si falta información (ej. salario), jamás se asume cero ni se penaliza arbitrariamente; se emite un `DatoPendiente`.
4. **Explicabilidad:** Cada coincidencia, brecha o dato pendiente incluye descripción y hecho sustentador.
5. **Inmutabilidad:** Todas las entidades y Value Objects son inmutables.

## Puertos y adaptadores

| Responsabilidad | Elemento | Ubicación |
|---|---|---|
| Modelos de Dominio | `PerfilProfesional`, `EvaluacionOferta`, `Coincidencia`, `Brecha`, `DatoPendiente`, `TipoAspecto`, `EstadoEvaluacion` | `opportunities/domain` |
| Puerto de Entrada | `EvaluarOfertaUseCase`, `EvaluarOfertaCommand` | `opportunities/application/port/in` |
| Puerto de Salida | `EvaluacionOfertaRepository` | `opportunities/application/port/out` |
| Servicio de Aplicación | `EvaluarOfertaService`, `ConsultarEvaluacionService` | `opportunities/application/service` |
| Adaptador en Memoria | `InMemoryEvaluacionOfertaRepository` | `opportunities/adapter/out/persistence` |
| Adaptador PostgreSQL | `JpaEvaluacionOfertaRepository` | `opportunities/adapter/out/persistence` |

## Criterios de aceptación

- Evaluar un perfil con todas las tecnologías requeridas, modalidad compatible y salario compatible devuelve estado `COMPATIBLE` con lista de brechas vacía.
- Evaluar un perfil al que le falta una tecnología exigida por la oferta devuelve `COMPATIBLE_CON_BRECHAS` detallando la tecnología faltante.
- Evaluar un perfil que rechaza la modalidad presencial contra una oferta `ONSITE` devuelve brecha bloqueante y estado `NO_COMPATIBLE`.
- Evaluar una oferta sin salario registrado genera un `DatoPendiente` indicando verificar el rango salarial en la entrevista.
- Diferencias de mayúsculas ("Java" vs "java") se reconocen como coincidencia plena.
- La evaluación queda guardada y puede recuperarse por su identificador, con coincidencias, brechas, datos pendientes y las copias de perfil y oferta, también después de reiniciar.
- El salario ausente en la evidencia sigue ausente.
- Dos evaluaciones del mismo par conservan dos identificadores. La segunda no altera la primera.

## Decisiones y pendientes

- **Confirmado:** Dominio puro sin Spring ni dependencias externas; Aggregate Root `EvaluacionOferta` con método factory `evaluar(...)`.
- **Confirmado en M4:** la evaluación y su evidencia se guardan en PostgreSQL y se leen igual tras reiniciar. El cálculo sigue siendo `EvaluacionOferta.evaluar`.
- **Pendiente para M5:** Adaptador LLM para análisis semántico profundo de descripciones no estructuradas. No está implementado.

## Historial

- 2026-09-30: Creación y especificación formal de UC-002.
- 2026-10-09: la evaluación y su evidencia sobreviven al reinicio. Una evaluación nueva no reemplaza la anterior.
