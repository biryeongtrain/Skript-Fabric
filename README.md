# SkFabric : Skript Fabric port 

---
SkFabric made for Server-Side Mod development. you can make and modify logics on runtime. <br>

## Syntax Implementations 

you can see all implemented syntaxes on [here](implemented_syntax.md)

## For mod developers

first, you have to add dependency to your project.

```groovy
repositories {
    maven {
        url("https://repo.biryeong.kim/releases/")
    }
}
  ```

```
dependencies {
    modImplementation "com.biryeong.skfabric:skfabric:2.1.3+1.21.8"
}
```

now you can extend Skript with custom events, conditions, effects, and expressions.

### Creating a custom event

### Step 1: Declare the entrypoint

Add the `"skript"` entrypoint to your `fabric.mod.json`:

```json
{
  "entrypoints": {
    "skript": [
      "com.example.myaddon.MySkriptAddon"
    ]
  },
  "depends": {
    "skfabric": "*"
  }
}
```

### Step 2: Implement the entrypoint


```java
package com.example.myaddon;

import ch.njol.skript.Skript;
import ch.njol.skript.registrations.EventValues;
import net.minecraft.server.level.ServerPlayer;
import org.skriptlang.skript.fabric.api.SkriptAddonEntrypoint;

public class MySkriptAddon implements SkriptAddonEntrypoint {

    @Override
    public void onSkriptInitialize() {
        // Register event syntax
        Skript.registerEvent(EvtAnvilUse.class, "on anvil use");

        // Register event values (makes event-player available in scripts)
        EventValues.registerEventValue(
                AnvilUseHandle.class,
                ServerPlayer.class,
                AnvilUseHandle::player
        );

        // Hook into Fabric events to dispatch (see Step 5)
    }
}
```

### Step 3: Create an event handle

The handle is a simple data class that holds the event context. Scripts access event data through this object.

```java
package com.example.myaddon;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public record AnvilUseHandle(ServerLevel level, ServerPlayer player, BlockPos pos) {}
```

### Step 4: Create an event handler

Extend `SimpleEvent` and match your handle type via `instanceof`:

```java
package com.example.myaddon;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class EvtAnvilUse extends SimpleEvent {

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return event.handle() instanceof AnvilUseHandle;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "on anvil use";
    }
}
```

### Step 5: Dispatch events

Use `SkriptEventDispatcher` to fire events from Fabric callbacks or Mixins:

```java
import org.skriptlang.skript.fabric.api.SkriptEventDispatcher;

// Inside a Fabric event callback:
var handle = new AnvilUseHandle(level, player, pos);
boolean allowed = SkriptEventDispatcher.dispatch(handle, level.getServer(), level, player);
if (!allowed) {
    // Event was cancelled by a script via "cancel event"
    return InteractionResult.FAIL;
}
```

For fire-and-forget events that cannot be cancelled:

```java
SkriptEventDispatcher.dispatchUncancellable(handle, server, level, player);
```

### Result

Scripts can now use your custom event:

```
on anvil use:
    send "You used an anvil!" to event-player
    cancel event
```

### Optional: Implement core handle interfaces

If your handle implements Skript's built-in interfaces, expressions like `event-entity`, `event-block`, or `event-item` work automatically:

| Interface | Provides | Method |
|-----------|----------|--------|
| `FabricEntityEventHandle` | `event-entity` | `Entity entity()` |
| `FabricBlockEventHandle` | `event-block` | `ServerLevel level()`, `BlockPos position()` |
| `FabricItemEventHandle` | `event-item` | `ItemStack itemStack()` |

```java
public record MyHandle(ServerPlayer player, Entity target)
        implements FabricEntityEventHandle {
    @Override
    public Entity entity() {
        return target;
    }
}
```

---

### Creating a custom condition

Conditions return `true` or `false` and are used in `if` statements. Extend `Condition` and implement `check()`.

```java
package com.example.myaddon;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class CondIsGlowing extends Condition {

    private Expression<?> entities;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = expressions[0];
        setNegated(matchedPattern == 1); // pattern 1 = "is not glowing"
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return entities.check(event,
                value -> value instanceof Entity entity && entity.isCurrentlyGlowing(),
                isNegated());
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return entities.toString(event, debug) + (isNegated() ? " is not" : " is") + " glowing";
    }
}
```

Register in your entrypoint:

```java
Skript.registerCondition(CondIsGlowing.class,
        "%entities% (is|are) glowing",      // pattern 0
        "%entities% (isn't|aren't) glowing"  // pattern 1 (negated)
);
```

Script usage:

```
if player is glowing:
    send "You're glowing!" to player
```

---

### Creating a custom effect

Effects perform actions and don't return values. Extend `Effect` and implement `execute()`.

```java
package com.example.myaddon;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class EffIgnite extends Effect {

    private Expression<Entity> entities;
    private Expression<Number> duration;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = (Expression<Entity>) expressions[0];
        duration = expressions.length > 1 ? (Expression<Number>) expressions[1] : null;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        int ticks = duration != null ? duration.getSingle(event).intValue() * 20 : 100;
        for (Entity entity : entities.getAll(event)) {
            entity.igniteForTicks(ticks);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "ignite " + entities.toString(event, debug);
    }
}
```

Register in your entrypoint:

```java
Skript.registerEffect(EffIgnite.class, "ignite %entities% [for %-number% second[s]]");
```

Script usage:

```
ignite player for 5 seconds
```

---

### Creating a custom expression

Expressions return values and can be used wherever a value is expected. Extend `SimpleExpression` for standalone values or `PropertyExpression` for `%type%'s property` patterns.

#### SimpleExpression example

```java
package com.example.myaddon;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprServerTPS extends SimpleExpression<Number> {

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    protected Number @Nullable [] get(SkriptEvent event) {
        if (event.server() == null) return null;
        return new Number[]{ event.server().getAverageTickTimeNanos() / 1_000_000.0 };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "server tick time";
    }
}
```

Register:

```java
Skript.registerExpression(ExprServerTPS.class, Number.class, "[the] server tick time");
```

#### PropertyExpression example

Use this when the expression gets a property from an entity/player/item.

```java
package com.example.myaddon;

import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprArmor extends PropertyExpression<LivingEntity, Number> {

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setExpr((Expression<? extends LivingEntity>) expressions[0]);
        return true;
    }

    @Override
    protected Number[] get(SkriptEvent event, LivingEntity[] source) {
        return get(source, entity -> (double) entity.getArmorValue());
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "armor value of " + getExpr().toString(event, debug);
    }
}
```

Register:

```java
Skript.registerExpression(ExprArmor.class, Number.class,
        "[the] armor value of %livingentities%",
        "%livingentities%'[s] armor value"
);
```

Script usage:

```
send "Your armor: %armor value of player%" to player
```

#### Making expressions changeable

Override `acceptChange()` and `change()` to allow `set`, `add`, `remove`, `reset`, `delete`:

```java
@Override
public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
    return switch (mode) {
        case SET, ADD, REMOVE -> new Class[]{Number.class};
        default -> null; // unsupported
    };
}

@Override
public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
    double value = delta != null ? ((Number) delta[0]).doubleValue() : 0;
    for (LivingEntity entity : getExpr().getArray(event)) {
        switch (mode) {
            case SET -> entity.setAbsorptionAmount((float) value);
            case ADD -> entity.setAbsorptionAmount(entity.getAbsorptionAmount() + (float) value);
            case REMOVE -> entity.setAbsorptionAmount(entity.getAbsorptionAmount() - (float) value);
        }
    }
}
```

Script usage:

```
set armor value of player to 10
add 5 to armor value of player
```

---

### Registration API summary

All registration calls should be made inside `onSkriptInitialize()`:

```java
// Events
Skript.registerEvent(MyEvent.class, "on my event");

// Conditions
Skript.registerCondition(MyCond.class, "%entities% is my condition", "%entities% isn't my condition");

// Effects
Skript.registerEffect(MyEffect.class, "do something to %entities% [with %-number%]");

// Expressions
Skript.registerExpression(MyExpr.class, Number.class, "[the] my value of %entities%");

// Event values (makes event-xxx expressions available)
EventValues.registerEventValue(MyHandle.class, ServerPlayer.class, MyHandle::player);
```

### Pattern syntax

Patterns use `%type%` for expression placeholders and `[optional]` for optional parts:

| Syntax | Meaning |
|--------|---------|
| `%entities%` | Required expression of type entities |
| `%-number%` | Optional expression of type number |
| `[optional text]` | Optional literal text |
| `(choice1\|choice2)` | One of the choices |
| `word:tag` | Parse tag (accessible via `parseResult.hasTag("tag")`) |

### Notes

- Use unique pattern names to avoid conflicts with core syntax (e.g., don't register `"on damage"`).
- If `onSkriptInitialize()` throws, the error is logged and other addons continue normally.
- All registration APIs are thread-safe.
- Do **not** call `Skript.setAcceptRegistrations(false)` — it will block other addons.
