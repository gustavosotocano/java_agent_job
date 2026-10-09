# Contract: Consultar oferta registrada

Canal: caso de uso de aplicación. No hay endpoint HTTP en este incremento.

## Entradas aceptadas

- por `id` interno
- por identidad (`fuenteId` + `urlOriginal` sin normalizar; el caso de uso normaliza antes de buscar)

## Salida

La misma forma `OfertaRegistrada` del contrato de registro, o ausencia si no existe.

## Garantías de lectura

- Un campo salarial, una fecha o una modalidad nulos se devuelven nulos. No se sustituyen por cero, por una modalidad por defecto ni por un valor de otra procedencia.
- La lista de procedencias incluye todas las capturas guardadas.
- Empresa y título iguales en dos identidades distintas devuelven dos ofertas.
