# Widgets & Modifiers

The complete catalog of composables and modifiers available to plugin authors, all under `com.livewire.ui`. Names and parameters intentionally mirror Compose Material 3 — if you know Compose, you know these.

## Layout

| Composable | Signature highlights |
|---|---|
| `Column` | `verticalArrangement`, `horizontalAlignment`; scope adds `align()` and `weight()` |
| `Row` | `horizontalArrangement`, `verticalAlignment`; scope adds `align()` and `weight()` |
| `Box` | `contentAlignment`; scope adds `align()` |
| `Spacer` | modifier-only |
| `Surface` | `shape`, `color`, `contentColor`, `tonalElevation`, `shadowElevation`, optional `onClick` |
| `ResizableSurface` | a user-draggable panel: `anchor` (`Start`/`Top`/`End`/`Bottom`), `initialSize`, `minSize`, `maxSize` |
| `AnimatedVisibility` | `visible`, `enter` (`FadeIn`, `SlideIn*`, `Expand*`), `exit` (`FadeOut`, `SlideOut*`, `Shrink*`) |
| `FloatingToolbar` | `expanded`, row content |

## Buttons & actions

| Composable | Signature highlights |
|---|---|
| `Button` | `action: ClickAction`, `size` (`ExtraSmall`–`Large`), `style` (`Filled`, `Tonal`, `Outlined`, `Elevated`, `Text`), `shapes` |
| `IconButton` | `action`, `enabled`, `size`, `style` (`Default`, `Filled`, `Tonal`, `Outlined`), `shapes` |
| `ToggleButton` | `checked`, `onCheckedChange`, `style` (`Filled`, `Outlined`, `Elevated`) |
| `FloatingActionButton` | `action`, `size` (`Small`, `Default`, `Large`), `style` (`Primary`, `Surface`, `Secondary`, `Tertiary`), `expanded` |
| `Chip` | `label`, `action`, `style` (`Assist`, `Filter`, `Input`, `Suggestion`), `elevated`, `selected`, `leadingIcon` |

## Input

| Composable | Signature highlights |
|---|---|
| `TextField` | `initialValue`, `onValueChange: ValueChangeAction`, `label`, `placeholder`, `singleLine`, `maxLines`, `style` (`Filled`, `Outlined`) |
| `BasicTextField` | undecorated text input: `textStyle`, `textColor`, `cursorColor`, `placeholder`, `minLines`/`maxLines` |
| `Checkbox` | `checked`, `onCheckedChange`, `enabled` |
| `Switch` | `checked`, `onCheckedChange`, `enabled` |
| `RadioButton` | `selected`, `onClick`, `enabled` |
| `Slider` | `value`, `onValueChange: FloatValueChangeAction`, `valueRangeStart`/`End`, `steps` |
| `DropdownMenu` / `DropdownMenuItem` | `expanded`, `onDismissRequest`; items take `text`, `onClick`, leading/trailing icons |
| `TabRow` / `Tab` | `style` (`Primary`, `Secondary`); tabs take `selected`, `onClick`, `text`, `icon` |

## Display

| Composable | Signature highlights |
|---|---|
| `Text` | `text`, `color`, `style` (full M3 type scale: `DisplayLarge` … `LabelSmall`), `fontWeight` |
| `Icon` | any Compose `ImageVector` — vectors serialize across the wire |
| `Image` | raw encoded `ByteArray` + `contentDescription` |
| `CodeBlock` | `content`, `language` (`Json`, `Xml`, `Html`, `PlainText`), `searchable` |
| `Table` | `columns`, `rows`, built-in pagination (`pageSize = 10`) |
| `ProgressIndicator` | `progress` (`null` = indeterminate), `style` (`Linear`, `Circular`) |
| `HorizontalDivider` / `VerticalDivider` | `thickness` |

## Modifiers

`LivewireModifier` mirrors Compose's `Modifier` — start from the companion and chain:

| Modifier | Extensions |
|---|---|
| Size | `width()`, `height()`, `size()`, `fillMaxWidth()`, `fillMaxHeight()`, `fillMaxSize()`, `wrapContentSize()`, intrinsic sizes |
| Padding | `padding(all)`, `padding(horizontal, vertical)`, `padding(start, top, end, bottom)` |
| Drawing | `background(color, shape)`, `border(width, color, shape)`, `clip(shape)`, `alpha()`, `rotate()` |
| Interaction | `clickable(action)`, `copyClickable(value)` — copies to the host's clipboard with a snackbar |
| Scrolling | `verticalScroll()`, `horizontalScroll()` |
| Animation | `animateContentSize()` |
| Scoped | `align(...)` and `weight(...)` inside `Row`/`Column`/`Box` scopes |
| Conditional | `thenIf(condition) { ... }` |

Shapes are serializable too: `RectangleShape`, `CircleShape`, and `RoundedCornerShape(...)`.

## Actions

The five action types that carry interactions from the host back to your plugin, each with a composable factory:

| Action | Factory | Carried payload |
|---|---|---|
| `ClickAction` | `clickAction { }` | — |
| `CheckedChangeAction` | `checkedChangeAction { }` | `Boolean` |
| `ValueChangeAction` | `valueChangeAction { }` | `String` |
| `IntValueChangeAction` | `intValueChangeAction { }` | `Int` |
| `FloatValueChangeAction` | `floatValueChangeAction { }` | `Float` |

## Theming

`LivewireTheme(lightColorScheme, darkColorScheme)` carries two full Material 3 `ColorScheme`s from the client app to the host, so rendered plugin UI wears your app's brand. Inside a plugin, read colors from `LivewireTheme.colorScheme` — it resolves light vs. dark automatically, following the host's toggle.

```kotlin
LivewireClient {
  theme(LivewireTheme(
    lightColorScheme = lightColorScheme(primary = BrandOrange /* ... */),
    darkColorScheme = darkColorScheme(primary = BrandAmber /* ... */),
  ))
}
```
