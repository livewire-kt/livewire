package com.livewire.plugin.preferences.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PreferenceValueParsingTest {

  @Test
  fun parsesPrimitives() {
    assertEquals(
      PreferenceValue.StringValue("hello"),
      parsePreferenceValue(PreferenceValueType.String, "hello").getOrThrow(),
    )
    assertEquals(
      PreferenceValue.IntValue(42),
      parsePreferenceValue(PreferenceValueType.Int, " 42 ").getOrThrow(),
    )
    assertEquals(
      PreferenceValue.LongValue(9_000_000_000L),
      parsePreferenceValue(PreferenceValueType.Long, "9000000000").getOrThrow(),
    )
    assertEquals(
      PreferenceValue.FloatValue(1.5f),
      parsePreferenceValue(PreferenceValueType.Float, "1.5").getOrThrow(),
    )
    assertEquals(
      PreferenceValue.DoubleValue(2.25),
      parsePreferenceValue(PreferenceValueType.Double, "2.25").getOrThrow(),
    )
  }

  @Test
  fun rejectsInvalidNumbers() {
    assertTrue(parsePreferenceValue(PreferenceValueType.Int, "abc").isFailure)
    assertTrue(parsePreferenceValue(PreferenceValueType.Long, "1.5").isFailure)
    assertTrue(parsePreferenceValue(PreferenceValueType.Float, "").isFailure)
    assertTrue(parsePreferenceValue(PreferenceValueType.Double, "two").isFailure)
  }

  @Test
  fun parsesBooleansIgnoringCase() {
    assertEquals(
      PreferenceValue.BooleanValue(true),
      parsePreferenceValue(PreferenceValueType.Boolean, "TRUE").getOrThrow(),
    )
    assertEquals(
      PreferenceValue.BooleanValue(false),
      parsePreferenceValue(PreferenceValueType.Boolean, "false").getOrThrow(),
    )
    assertTrue(parsePreferenceValue(PreferenceValueType.Boolean, "yes").isFailure)
  }

  @Test
  fun parsesStringSetsFromLines() {
    val parsed = parsePreferenceValue(
      PreferenceValueType.StringSet,
      "alpha\n  beta  \n\ngamma\n",
    ).getOrThrow()

    assertEquals(
      PreferenceValue.StringSetValue(setOf("alpha", "beta", "gamma")),
      parsed,
    )
  }

  @Test
  fun bytesRoundTripThroughBase64() {
    val original = PreferenceValue.BytesValue(byteArrayOf(0, 1, 2, 127, -128))
    val parsed = parsePreferenceValue(PreferenceValueType.Bytes, original.render()).getOrThrow()
    assertEquals(original, parsed)
  }

  @Test
  fun rejectsInvalidBase64() {
    assertTrue(parsePreferenceValue(PreferenceValueType.Bytes, "not base64!!!").isFailure)
  }

  @Test
  fun renderParseRoundTripsPreserveValues() {
    val values = listOf(
      PreferenceValue.StringValue("some text"),
      PreferenceValue.IntValue(-7),
      PreferenceValue.LongValue(Long.MAX_VALUE),
      PreferenceValue.FloatValue(3.14f),
      PreferenceValue.DoubleValue(-0.001),
      PreferenceValue.BooleanValue(true),
      PreferenceValue.StringSetValue(setOf("one", "two")),
    )

    values.forEach { value ->
      val type = value.type!!
      assertEquals(value, parsePreferenceValue(type, value.render()).getOrThrow())
    }
  }

  @Test
  fun opaqueValuesAreNotEditable() {
    val opaque = PreferenceValue.OpaqueValue("whatever")
    assertNull(opaque.type)
    assertTrue(!opaque.editable)
  }
}
