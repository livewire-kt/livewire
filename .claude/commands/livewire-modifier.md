# Generate a Livewire Modifier

Generate a new LivewireModifier for the Livewire remote UI system. The user will describe the Compose UI modifier they want to wrap (e.g. "border", "clip", "alpha", "rotate"). Use this description along with the argument: $ARGUMENTS

## What to generate

You must create exactly this file:

### Modifier definition: `ui/src/commonMain/kotlin/com/livewire/ui/modifier/<Name>Modifier.kt`

This file contains the internal modifier class and the public extension function(s).

**Modifier class pattern:**
- Annotated with `@LivewireSerializer` (from `com.livewire.annotations.LivewireSerializer`) — this auto-registers the modifier with the KSP-generated polymorphic serializer, no manual registration needed
- Annotated with `@Serializable`
- Marked `internal class`
- Implements `LivewireModifier.Element`
- All properties are `val` (immutable, unlike node properties)
- All properties must be serializable. For non-serializable types:
  - `Color` → use `@Serializable(with = ColorSerializer::class)` annotation (import from `com.livewire.ui.graphics.ColorSerializer`)
  - `Dp` → store as `Float` (the raw `.value`), convert back with `.dp` in `toComposeUi`
  - `Shape` → use `com.livewire.ui.graphics.Shape` (already `@Serializable`)
- Overrides `toComposeUi(then: Modifier): Modifier` annotated with `@Composable` and `@Suppress("ModifierFactoryExtensionFunction")`
- If the modifier is scope-aware (e.g. `weight` only works in RowScope/ColumnScope), also override the appropriate `RowScope.toComposeUi` or `ColumnScope.toComposeUi` extension

**Extension function pattern:**
- Public extension function(s) on `LivewireModifier`
- Accept user-friendly types (`Dp`, `Color`, `Shape`) and convert to serializable forms internally
- Call `then({Name}Modifier(...))` to chain
- Provide multiple overloads for common usage patterns (e.g. with/without shape, all-sides vs per-side)

Here is an example of a simple modifier (BackgroundModifier.kt):
```kotlin
package com.livewire.ui.modifier

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import com.livewire.annotations.LivewireSerializer
import com.livewire.ui.graphics.ColorSerializer
import com.livewire.ui.graphics.Shape
import kotlinx.serialization.Serializable

@LivewireSerializer
@Serializable
internal class BackgroundModifier(
  @Serializable(with = ColorSerializer::class) val color: Color,
  val shape: Shape? = null,
) : LivewireModifier.Element {

  @Suppress("ModifierFactoryExtensionFunction")
  @Composable
  override fun toComposeUi(then: Modifier): Modifier {
    return then.background(
      color = color,
      shape = shape?.toComposeUi() ?: RectangleShape,
    )
  }
}

fun LivewireModifier.background(color: Color): LivewireModifier =
  then(BackgroundModifier(color))

fun LivewireModifier.background(color: Color, shape: Shape): LivewireModifier =
  then(BackgroundModifier(color, shape))
```

Here is an example of a modifier with Dp values (PaddingModifier.kt):
```kotlin
package com.livewire.ui.modifier

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.livewire.annotations.LivewireSerializer
import kotlinx.serialization.Serializable

@LivewireSerializer
@Serializable
internal class PaddingModifier(
  val start: Float,
  val top: Float,
  val end: Float,
  val bottom: Float,
) : LivewireModifier.Element {
  init {
    require(
      (start >= 0f) and
        (top >= 0f) and
        (end >= 0f) and
        (bottom >= 0f)
    ) {
      "Padding must be non-negative"
    }
  }

  @Suppress("ModifierFactoryExtensionFunction")
  @Composable
  override fun toComposeUi(then: Modifier): Modifier {
    return then.padding(
      start = start.dp,
      end = end.dp,
      top = top.dp,
      bottom = bottom.dp,
    )
  }
}

fun LivewireModifier.padding(all: Dp): LivewireModifier =
  padding(left = all, top = all, right = all, bottom = all)

fun LivewireModifier.padding(
  left: Dp = 0.dp,
  top: Dp = 0.dp,
  right: Dp = 0.dp,
  bottom: Dp = 0.dp,
): LivewireModifier {
  return then(PaddingModifier(left.value, top.value, right.value, bottom.value))
}

fun LivewireModifier.padding(
  horizontal: Dp = 0.dp,
  vertical: Dp = 0.dp,
): LivewireModifier =
  padding(left = horizontal, top = vertical, right = horizontal, bottom = vertical)
```

Here is an example of a modifier with Color and Shape (BorderModifier.kt):
```kotlin
package com.livewire.ui.modifier

import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.livewire.annotations.LivewireSerializer
import com.livewire.ui.graphics.ColorSerializer
import com.livewire.ui.graphics.RectangleShape
import com.livewire.ui.graphics.Shape
import kotlinx.serialization.Serializable

@LivewireSerializer
@Serializable
internal class BorderModifier(
  val width: Float,
  @Serializable(with = ColorSerializer::class) val color: Color,
  val shape: Shape = RectangleShape,
) : LivewireModifier.Element {

  @Suppress("ModifierFactoryExtensionFunction")
  @Composable
  override fun toComposeUi(then: Modifier): Modifier {
    return then.border(
      width = width.dp,
      color = color,
      shape = shape.toComposeUi(),
    )
  }
}

fun LivewireModifier.border(width: Dp, color: Color): LivewireModifier =
  then(BorderModifier(width.value, color))

fun LivewireModifier.border(width: Dp, color: Color, shape: Shape): LivewireModifier =
  then(BorderModifier(width.value, color, shape))
```

Here is an example of a modifier with just a Shape (ClipModifier.kt):
```kotlin
package com.livewire.ui.modifier

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.livewire.annotations.LivewireSerializer
import com.livewire.ui.graphics.Shape
import kotlinx.serialization.Serializable

@LivewireSerializer
@Serializable
internal class ClipModifier(
  val shape: Shape,
) : LivewireModifier.Element {

  @Suppress("ModifierFactoryExtensionFunction")
  @Composable
  override fun toComposeUi(then: Modifier): Modifier {
    return then.clip(shape.toComposeUi())
  }
}

fun LivewireModifier.clip(shape: Shape): LivewireModifier =
  then(ClipModifier(shape))
```

## Important conventions

- Package: `com.livewire.ui.modifier` for all modifier files
- Class naming: `{Name}Modifier` (e.g. `BorderModifier`, `PaddingModifier`)
- Class visibility: `internal class` (not exposed directly, only via extension functions)
- Always annotate with `@LivewireSerializer` — this triggers KSP auto-registration into the polymorphic serializer. No manual serializer registration is needed.
- Extension functions: public, on `LivewireModifier`
- Dp values: store as `Float`, convert with `.dp` in `toComposeUi`
- Color values: annotate with `@Serializable(with = ColorSerializer::class)`
- Shape values: use `com.livewire.ui.graphics.Shape` (Livewire's serializable shape types: `RectangleShape`, `CircleShape`, `RoundedCornerShape`)
- The `toComposeUi` override must be annotated with both `@Composable` and `@Suppress("ModifierFactoryExtensionFunction")`
- Scope-aware modifiers (like `weight`) override the scope-specific `toComposeUi` extensions from `ComposeUiMapper`
- No `:host-ui` changes needed — modifier classes live in the shared `:ui` module and the host renderers call `toComposeUi` polymorphically
- Because modifier classes are shared, `toComposeUi` can only use what `:ui` provides. Host-side services must live in `:ui` too — e.g. `CopyClickableModifier` uses `LocalSnackDispatcher` from `com.livewire.ui.snackbar`, which stays in `:ui` for exactly this reason

## Existing serializable types available

These types from `com.livewire.ui.graphics` are already `@Serializable` and can be used as modifier properties:
- `Shape` (sealed interface: `RectangleShape`, `CircleShape`, `RoundedCornerShape`)
- `CornerSize` (with `Dp`, `Px`, and `Percent` types)
- `Color` (via `ColorSerializer`)

## After generating

1. Verify the code compiles: `./gradlew :ui:assemble`
2. Summarize what was created and what Compose UI modifier it wraps
