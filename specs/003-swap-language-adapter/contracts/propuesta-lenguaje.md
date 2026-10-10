# Contrato: puerto de propuesta

El caso de uso de evaluar pide una propuesta y sigue aunque el puerto no tenga nada que aportar. No es un endpoint.

## Entrada

Oferta y perfil ya validados, más un límite de espera. El límite de este incremento es 2 segundos. El texto de la oferta entra como dato, no como instrucción.

## Salida

- Disponible, con una lista de candidatas (`texto`, `fragmento`).
- No disponible, con motivo `FALLO`, `TIEMPO_AGOTADO` o `ILEGIBLE`.

El caso de uso filtra las candidatas contra el corpus y guarda solo las aceptadas. Una salida no disponible guarda la evaluación con cero observaciones.

## Adaptadores

| Nombre | Cuándo | Qué devuelve |
|---|---|---|
| vacia | Predeterminado. No llama a ningún servicio. | Disponible con lista vacía, al instante. |
| texto | `jobagent.lenguaje.adaptador=texto` | Interpreta `jobagent.lenguaje.texto`. Si no se puede leer, `ILEGIBLE`. |

El texto estructurado, cuando se usa, es una secuencia de bloques. Cada bloque tiene una línea `texto:` y una línea `fragmento:`. Un bloque incompleto o un texto vacío de configuración es ilegible y se descarta entero.

## Fuera de este contrato

No hay clave, URL ni cuenta de proveedor. Buscar ofertas no llama a este puerto. No se envía una postulación ni se inicia sesión.
