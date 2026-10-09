# Contract: Evaluación almacenada

Canales de aplicación. No hay endpoint HTTP en este incremento.

El puerto `EvaluacionOfertaRepository` se conserva. `guardar` sigue recibiendo el agregado que produce `EvaluacionOferta.evaluar` y solo inserta. `buscarPorId`, `buscarPorPerfil` y `buscarPorOferta` siguen devolviendo ese agregado.

## Escritura ampliada

`EvaluarOfertaService`, en el mismo guardado, persiste también la copia del `PerfilProfesional` y de la `OfertaLaboral` del comando, con la URL ya normalizada cuando exista. Si la oferta no tiene URL normalizable, la evaluación igual se guarda: la copia de identidad queda sin URL y el agregado no se rechaza. Registrar la oferta y evaluarla siguen siendo pasos distintos.

## Lectura completa

`ConsultarEvaluacionUseCase` devuelve `EvaluacionAlmacenada`:

- el agregado `EvaluacionOferta` con el mismo `estado`, las mismas listas y la misma `fechaEvaluacion`
- la copia del perfil
- la copia de la oferta, incluida la identidad normalizada si se pudo calcular

Después de reiniciar el proceso y apuntar al mismo PostgreSQL, la lectura completa devuelve esos tres elementos. Una evaluación posterior del mismo par aparece como otro `id` y no altera el anterior.

Reimportar la oferta vigente no cambia la copia guardada en la evaluación.
