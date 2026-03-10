# M3 Status

## Scope

- `expressions/**` player/server/chat bundle only
- narrow tests for assigned expressions

## Assigned Targets

- `20` expressions:
  - `ExprAllCommands`
  - `ExprChatFormat`
  - `ExprChatRecipients`
  - `ExprClientViewDistance`
  - `ExprCmdCooldownInfo`
  - `ExprCommand`
  - `ExprCommandInfo`
  - `ExprCommandSender`
  - `ExprHostname`
  - `ExprIP`
  - `ExprLanguage`
  - `ExprLastLoginTime`
  - `ExprLastResourcePackResponse`
  - `ExprMaxPlayers`
  - `ExprMessage`
  - `ExprPermissions`
  - `ExprPing`
  - `ExprPlayerChatCompletions`
  - `ExprPlayerProtocolVersion`
  - `ExprProtocolVersion`

## Landed Classes

- `ExprAllCommands`
- `ExprClientViewDistance`
- `ExprCommand`
- `ExprCommandSender`
- `ExprIP`
- `ExprLanguage`
- `ExprLastLoginTime`
- `ExprMaxPlayers`
- `ExprPing`
- `ExprPlayerProtocolVersion`
- `ExprProtocolVersion`
- helper glue:
  - `ExpressionRuntimeSupport`

## Runtime-Eligible Classes

- `ExprAllCommands`
- `ExprClientViewDistance`
- `ExprCommand`
- `ExprCommandSender`
- `ExprIP`
- `ExprLanguage`
- `ExprLastLoginTime`
- `ExprMaxPlayers`
- `ExprPing`
- `ExprPlayerProtocolVersion`
- `ExprProtocolVersion`
- notes:
  - `ExprCommand` and `ExprCommandSender` require the existing import-only `FabricPlayerEventHandles.Command` event surface
  - `ExprLastLoginTime`, `ExprMaxPlayers`, and `ExprAllCommands` require a non-null runtime server context
  - no bootstrap activation was done in-lane per batch rules

## Bootstrap Registrations Needed

- add `forceInitialize(ch.njol.skript.expressions.ExprAllCommands.class)`
- add `forceInitialize(ch.njol.skript.expressions.ExprClientViewDistance.class)`
- add `forceInitialize(ch.njol.skript.expressions.ExprCommand.class)`
- add `forceInitialize(ch.njol.skript.expressions.ExprCommandSender.class)`
- add `forceInitialize(ch.njol.skript.expressions.ExprIP.class)`
- add `forceInitialize(ch.njol.skript.expressions.ExprLanguage.class)`
- add `forceInitialize(ch.njol.skript.expressions.ExprLastLoginTime.class)`
- add `forceInitialize(ch.njol.skript.expressions.ExprMaxPlayers.class)`
- add `forceInitialize(ch.njol.skript.expressions.ExprPing.class)`
- add `forceInitialize(ch.njol.skript.expressions.ExprPlayerProtocolVersion.class)`
- add `forceInitialize(ch.njol.skript.expressions.ExprProtocolVersion.class)`

## Targeted Tests

- `./gradlew test --tests ch.njol.skript.expressions.ExpressionPlayerServerCompatibilityTest --rerun-tasks`
  - passed

## Blockers

- `ExprChatFormat` and `ExprChatRecipients` remain blocked on a missing Fabric chat event surface; there is no local chat handle or runtime producer equivalent to upstream chat events in this batch
- `ExprCmdCooldownInfo`, `ExprCommandInfo`, and the script-command branch of `ExprAllCommands` remain blocked on the absent upstream `ch.njol.skript.command` package
- `ExprHostname` remains blocked on a missing connect/login compat handle carrying hostname data
- `ExprLastResourcePackResponse` remains blocked on missing persistent player-level resource-pack response state; only the event handle exists locally
- `ExprMessage` remains blocked on missing chat/join/quit/death/broadcast message event surfaces
- `ExprPermissions` remains blocked because the current local LuckPerms bridge only supports point permission checks, not enumeration
- `ExprPlayerChatCompletions` remains blocked on missing local state/packet glue for custom chat completions

## Merge Note

- likely conflicts:
  - `src/main/java/ch/njol/skript/expressions/*.java`
  - `src/test/java/ch/njol/skript/expressions/ExpressionPlayerServerCompatibilityTest.java`
  - `docs/porting/parallel/batch-mixed-runtime-20260311/M3_STATUS.md`
