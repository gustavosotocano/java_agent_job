---
name: speckit-cycle
description: >-
  Runs one Spec Kit increment from specify through converge and stops only on
  a human gate. Use when the user invokes /speckit-cycle with a feature
  description, or asks to resume an in-progress cycle from .specify/cycle-state.json.
disable-model-invocation: true
---

# Spec Kit cycle

Run one increment until `/speckit-converge` reports converged, or until a stop gate fires. Read and follow each phase skill under `.cursor/skills/`. Do not rewrite those skills. Do not commit, push, or open a pull request.

## Start

The text after `/speckit-cycle` is the feature description. If it is empty and `.specify/cycle-state.json` has `status: in_progress`, resume at `phase`. If it is empty and there is no in-progress cycle, stop and ask for the description. Do not take the next milestone from `PLAN.md` on your own.

Write `.specify/cycle-state.json` after every completed phase:

```json
{
  "feature_directory": "specs/NNN-short-name",
  "description": "<user text>",
  "phase": "specify",
  "status": "in_progress",
  "converge_passes": 0,
  "stopped_reason": null
}
```

`phase` is the last completed phase: `specify`, `plan`, `tasks`, `analyze`, `implement`, `converge`.

## Phases

1. `/speckit-specify` with the description.
2. `/speckit-plan`
3. `/speckit-tasks`
4. `/speckit-analyze` (read-only)
5. `/speckit-implement`
6. `/speckit-converge`

Skip `/speckit-clarify` when the spec has no open questions. Skip `/speckit-checklist` and `/speckit-taskstoissues`. Do not re-run `/speckit-constitution`.

## Stop gates

| Phase | Continue | Stop |
|---|---|---|
| specify | Requirements checklist is complete and there is no open question | Empty description, or a product decision still unanswered |
| plan | Research resolves unknowns without breaking the constitution | The design needs a new HTTP channel, automatic apply, portal login, or an unverified source |
| tasks | Every task has a checkbox, an id, and a file | The plan cannot be split into work |
| analyze | No CRITICAL finding and no HIGH finding that contradicts the plan | A finding changes the product. Leave LOW findings for implement and converge |
| implement | `./mvnw test` is green and tasks are `[X]` | The requirements checklist is incomplete, or tests fail |
| converge | The report says converged and `tasks.md` is unchanged | Actionable gaps remain |

## Inner loop

If converge appends tasks, run implement and then converge again. Two converge passes maximum. If gaps remain after the second pass, set `status` to `stopped`, write `stopped_reason`, and show the gaps. Do not start a third pass.

## Finish

On converged, set `status` to `complete` and `phase` to `converge`. Report the feature directory and that the increment is closed. The next increment starts only with a new `/speckit-cycle` plus a description.
