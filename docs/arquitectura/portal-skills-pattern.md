# Patrón de Portales como Skills / CLIs Desacoplados

- Estado: Confirmado para integraciones públicas (Hitos M4.1 y M6).
- Última actualización: 2026-09-30.

---

## 1. Contexto y Justificación

La búsqueda de ofertas laborales en portales externos (LinkedIn, Indeed, Glassdoor, etc.) involucra mecanismos de scraping público, endpoints no oficiales o selectores HTML propensos a cambios frecuentes y medidas anti-bot.

Integrar bibliotecas de scraping o parsers HTML directamente dentro de la aplicación Spring Boot introduce riesgos:
- Acoplamiento del ciclo de vida del backend a cambios volátiles de HTML/DOM de terceros.
- Carga de dependencias pesadas (ej. motores de navegador) dentro del runtime de backend.
- Dificultad para probar y actualizar la lógica de extracción de un portal de forma aislada.

Para maximizar la resiliencia del sistema y minimizar el costo de mantenimiento, se adopta el **patrón de Skills / CLIs independientes**.

---

## 2. Definición del Patrón

Cada portal de empleo se implementa como un módulo o Skill autónomo (por ejemplo, con Bun/TypeScript o Python) en el directorio `.agents/skills/<portal>-search/`:

```text
job-agent/
├── .agents/
│   └── skills/
│       ├── linkedin-search/
│       │   ├── skill.yaml (o SKILL.md)
│       │   └── cli/
│       │       └── src/cli.ts
│       ├── indeed-search/
│       └── glassdoor-search/
└── src/main/java/com/gustavo/jobagent/opportunities/
    ├── application/port/out/BuscarOfertasPort.java
    └── adapter/out/
        ├── FuenteOfertasSimuladaAdapter.java
        └── linkedin/
            └── LinkedInCliAdapter.java
```

---

## 3. Contrato de Comunicación entre Java y el Skill

1. **Invocación desde Java:**
   El adaptador de salida correspondiente (`LinkedInCliAdapter`) implementa `BuscarOfertasPort` y ejecuta el CLI como subproceso del sistema operativo:
   ```bash
   bun run .agents/skills/linkedin-search/cli/src/cli.ts search --location "<ubicacion>" --query "<cargo>" --format json
   ```

2. **Salida estándar (`stdout`):**
   El CLI devuelve exclusivamente un array JSON válido con la estructura normalizada:
   ```json
   [
     {
       "id": "4426311357",
       "titulo": "Senior Java Backend Engineer",
       "empresa": "Tech Corp",
       "descripcion": "Requisitos: Java, Spring Boot, Kafka...",
       "ubicacion": "Colombia",
       "modalidad": "REMOTE",
       "urlOriginal": "https://www.linkedin.com/jobs/view/4426311357",
       "fechaPublicacion": "2026-09-29T14:00:00Z",
       "salario": {
         "minimo": 4000,
         "maximo": 6000,
         "moneda": "USD",
         "periodo": "MENSUAL"
       },
       "tecnologias": ["Java", "Spring Boot", "Kafka"]
     }
   ]
   ```

3. **Manejo de errores y timeouts (`stderr` / exit code):**
   - Si el proceso termina con código distinto de 0 o tiempo de espera agotado, la salida de `stderr` se captura como una advertencia estructurada (`AdvertenciaFuente`).
   - El fallo de un portal nunca interrumpe la consulta a los demás portales ni corrompe el dominio.

---

## 4. Beneficios Operativos y Arquitectónicos

- **Hexagonal puro:** Para el dominio y la aplicación Java, el portal es simplemente una implementación de `BuscarOfertasPort`.
- **Doble propósito (Backend API & Agente LLM):** En el backend se consume mediante el adaptador de subproceso; en modo Agente autónomo (Hito M6), el modelo de lenguaje puede invocar el CLI directamente como tool del agente.
- **Evolución independiente:** Cualquier cambio en la estructura de un portal se corrige en el CLI respectivo sin recompilar el artefacto Java.
