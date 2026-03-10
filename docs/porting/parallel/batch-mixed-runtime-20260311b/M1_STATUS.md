# M1 Status

## Scope

- `events/**`
- `effects/**` only for listed activation targets
- minimal runtime bridge files for the listed active targets
- narrow tests for assigned events and effects

## Assigned Targets

- primary active-event closure `10`:
  - `EvtBlock` expansion beyond break-only backing
  - `EvtItem` expansion beyond spawn-only backing
  - `EvtEntity` expansion beyond spawn/death backing
  - `EvtEntityTransform` reason refinement
  - `EvtHealing` reason refinement
  - `EvtBeaconEffect` payload/runtime follow-up
  - `EvtBeaconToggle` payload/runtime follow-up
  - `EvtBookEdit` runtime payload follow-up
  - `EvtBookSign` runtime payload follow-up
  - `EvtClick` runtime payload follow-up
- fallback active-runtime effect closure `9`:
  - `EffColorItems`
  - `EffTeleport`
  - `EffWakeupSleep`
  - `EffFireworkLaunch`
  - `EffExplosion`
  - `EffTree`
  - `EffEntityVisibility`
  - `EffClearEntityStorage`
  - `EffReleaseEntityStorage`

## Landed Classes

- pending

## Runtime-Eligible Classes

- pending

## Bootstrap Registrations Needed

- coordinator to decide after merge

## Targeted Tests

- pending

## Blockers

- pending

## Merge Note

- pending
