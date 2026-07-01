@file:OptIn(ComposeToolingApi::class)

package com.r0adkll.livewire.plugin.recomposition

import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.ParameterSourceInformation
import androidx.compose.ui.util.fastForEachReversed
import java.lang.reflect.Field
import kotlin.math.min

// adapted from androidx.compose.ui.tooling.data.extractParameterInfo

@ComposeToolingApi
internal actual fun extractParametersFromLambda(
  scope: RecomposeScope,
  metadata: List<ParameterSourceInformation>,
): List<ParameterInfo>? {
  val block = try {
    scope.javaClass.accessibleField("block")?.get(scope) ?: return null
  } catch (_: Exception) {
    return null
  }

  val blockClass = block.javaClass

  // field naming differs by compose compiler version. try invokedynamic (`f$N`/`arg$N`) capture fields first,
  // then fall back to legacy `$name` fields.
  return try {
    val indyFields = blockClass.declaredFields.filter { it.name.matches(IndyLambdaRegex) }
    if (indyFields.isNotEmpty()) {
      extractFromIndyLambdaFields(indyFields, block, metadata)
    } else {
      val legacyFields = blockClass.declaredFields.filter { it.name.matches(LegacyLambdaRegex) }
      if (legacyFields.isNotEmpty()) {
        extractFromLegacyFields(legacyFields, block, metadata)
      } else {
        null
      }
    }
  } catch (_: Exception) {
    null
  }
}

private fun extractFromIndyLambdaFields(
  fields: List<Field>,
  block: Any,
  metadata: List<ParameterSourceInformation>,
): List<ParameterInfo> {
  val sortedFields = fields.sortedBy { it.name.substringAfterLast("$").toIntOrNull() ?: Int.MAX_VALUE }

  val changedCount = (metadata.size + SlotsPerChangedInt - 1) / SlotsPerChangedInt
  var trailingInts = 0
  sortedFields.fastForEachReversed { if (it.type == Int::class.java) trailingInts++ else return@fastForEachReversed }

  val extraParameters = sortedFields.size - metadata.size - changedCount
  val maxDefaultCount = min(trailingInts - changedCount, (metadata.size + SlotsPerDefaultInt - 1) / SlotsPerDefaultInt)

  // skip leading capture fields that aren't declared params (extension receiver, $default ints, etc) so values line up with metadata.
  val leadingSkip = when {
    extraParameters <= 0 -> 0
    extraParameters > maxDefaultCount -> minOf(extraParameters - maxDefaultCount, 1)
    extraParameters == 1 -> if (isFirstFieldProbablyAReceiver(sortedFields[0], block)) 1 else 0
    else -> 0
  }

  val end = minOf(leadingSkip + metadata.size, sortedFields.size)
  val paramFields = sortedFields.subList(leadingSkip, end)

  return paramFields.mapIndexedNotNull { index, field ->
    field.isAccessible = true
    val value = try { field.get(block) } catch (_: Exception) { return@mapIndexedNotNull null }
    val meta = metadata.firstOrNull { it.sortedIndex == index }

    ParameterInfo(
      name = meta?.name ?: "param$index",
      value = ParameterValue.fromValue(value, meta?.inlineClass),
    )
  }
}

private fun isFirstFieldProbablyAReceiver(field: Field, block: Any): Boolean {
  field.isAccessible = true
  val value = try { field.get(block) } catch (_: Exception) { return false } ?: return false
  val cls = value::class.java
  return cls.interfaces.any { it.simpleName.endsWith("Scope") } ||
    cls.simpleName.endsWith("ScopeInstance")
}

private fun extractFromLegacyFields(
  fields: List<Field>,
  block: Any,
  metadata: List<ParameterSourceInformation>,
): List<ParameterInfo> {
  val sortedFields = fields.sortedBy { it.extractedName() }
  val sortedMeta = if (metadata.any { it.name != null }) {
    metadata.sortedBy { it.name }
  } else {
    metadata
  }

  return sortedFields.mapIndexedNotNull { index, field ->
    val meta = sortedMeta.getOrNull(index)
    val sortedIndex = meta?.sortedIndex ?: index
    if (sortedIndex >= fields.size) return@mapIndexedNotNull null

    val targetField = sortedFields[sortedIndex]
    targetField.isAccessible = true
    val value = try {
      targetField.get(block)
    } catch (_: Exception) {
      return@mapIndexedNotNull null
    }
    val name = meta?.name ?: targetField.extractedName() ?: "param$index"

    ParameterInfo(
      name = name,
      value = ParameterValue.fromValue(value, meta?.inlineClass),
    )
  }
}

private fun Field.extractedName(): String? {
  val groups = LegacyLambdaRegex.find(name)?.groups
  return (groups?.get(1) ?: groups?.get(2))?.value
}

private fun Class<*>.accessibleField(name: String): Field? =
  declaredFields.firstOrNull { it.name == name }?.apply { isAccessible = true }

private val IndyLambdaRegex = Regex("^(f\\$|arg\\$)\\d+$")
private val LegacyLambdaRegex = Regex("^\\$([^$]+)$|\\$\\$.*?\\$-([^$]+)\\$\\d+$")
private const val SlotsPerChangedInt = 10
private const val SlotsPerDefaultInt = 32
