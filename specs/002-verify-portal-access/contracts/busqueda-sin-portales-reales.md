# Contract: Búsqueda sin portales reales

Canal: el `POST /api/ofertas/busquedas` que ya existe. El cuerpo de la petición y el de la respuesta no ganan campos.

## Qué se consulta

Solo fuentes habilitadas con adaptador. En este incremento eso sigue siendo `SIMULADA` cuando está `DISPONIBLE` y habilitada.

`LINKEDIN`, `INDEED` y `GLASSDOOR` no tienen adaptador. Una búsqueda no los incluye en `fuentesConsultadas` ni genera una advertencia por no tener vía comprobada. No se consultan.

## Respuesta

Sigue trayendo `ofertas`, `fuentesConsultadas` y `advertencias`. Una búsqueda con coincidencias en la fuente simulada responde 200 y registra `CON_RESULTADOS`, como en M4.

El registro de búsqueda, la distinción entre vacío, fallo y ausencia de fuentes, y la regla de no insertar en `oferta` ni en `evaluacion` no cambian.

## Prohibido

- llamar a un host de LinkedIn, Indeed o Glassdoor durante la búsqueda
- marcar uno de esos portales como `DISPONIBLE` o habilitado
- deshabilitar `SIMULADA` como efecto de guardar los dictámenes
- abrir otro modo de buscar
