# Lane B Status

Last updated: 2026-03-09

## Scope

- `src/main/java/ch/njol/skript/config/**`
- `src/main/java/ch/njol/skript/util/**`
- matching config/util compatibility tests

## Latest Slice

- primary landed: restored the upstream-backed config support bundle around `NodeNavigator`, `ConfigReader`, `VoidNode` / `InvalidNode`, and reflective config options via `Option`, `OptionSection`, and legacy `EnumParser`
- `Config`, `Node`, and `SectionNode` now carry enough upstream navigation/config state for path lookups, config-backed node attachment, and invalid-node error tracking without breaking the current lightweight runtime constructor path
- fallback landed: restored upstream-style `Utils.parseInt(...)` saturation and added `Version`

## Verification

- `./gradlew test --tests ch.njol.skript.config.NodeCompatibilityTest --tests ch.njol.skript.config.SectionNodeCompatibilityTest --tests ch.njol.skript.config.ConfigReaderCompatibilityTest --tests ch.njol.skript.config.ConfigSupportCompatibilityTest --tests ch.njol.skript.util.VersionCompatibilityTest --rerun-tasks` passed

## Next Lead

- continue inside config/localization with `validate/**` or the localization message stack if it can be imported without widening into non-lane addon/runtime surfaces

## Merge Notes

- likely conflict surface: `src/main/java/ch/njol/skript/config/Config.java`, `src/main/java/ch/njol/skript/config/Node.java`, `src/main/java/ch/njol/skript/config/SectionNode.java`, `src/main/java/ch/njol/skript/util/Utils.java`
- new files are isolated to config support helpers, `Version`, and focused config/util compatibility tests
