# External Library Gaps

Last updated: 2026-03-11

This document is the coordinator-owned ledger for upstream classes or syntax families
that could not stay landed because the local Fabric port still lacks a required external
library or external-facing API surface.

Use it when a batch had to:

- delete a previously attempted import
- revert a syntax family from active runtime back to import-only
- skip or park an upstream class because the needed external dependency is still absent

Do not use it for ordinary local runtime gaps that do not depend on an external library
or external API decision.

## Recording Rules

- workers record the blocker in their lane status file only
- coordinator decides the final action and updates this file
- silent deletion is forbidden
- every entry must include a suggested external library or a concrete alternative path
- batch final reports should mention any new rows added here

## Entry Format

| Field | Meaning |
| --- | --- |
| `date / batch` | When the decision was made and which batch it belonged to |
| `upstream class or syntax family` | The upstream class, family, or representative syntax affected |
| `local decision` | `deleted`, `reverted to import-only`, or `not imported` |
| `missing library or API` | The absent dependency or integration surface |
| `attempted fallback / adaptation` | What was tried locally before the decision |
| `suggested external library or alternative path` | Candidate dependency or non-library replacement strategy |
| `owner lane` | Which worker lane found the blocker |
| `commit / note` | Commit hash or short note for the final coordinator decision |
| `revisit trigger` | What needs to change before reopening the slice |

## Recorded Decisions

No confirmed external-library-driven deletions, rollbacks, or skipped imports are
recorded yet.
