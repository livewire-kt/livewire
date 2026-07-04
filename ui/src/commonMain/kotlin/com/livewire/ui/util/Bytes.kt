package com.livewire.ui.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

fun Long.asReadableBytes(): String {
  val kb = this.toDouble() / 1024.0
  val mb = kb / 1024.0
  val gb = mb / 1024.0
  val tb = gb / 1024.0

  return if (tb >= 1.0) {
    tb.toFloat().toString(2) + " TB"
  } else if (gb >= 1.0) {
    gb.toFloat().toString(2) + " GB"
  } else if (mb >= 1.0) {
    mb.toFloat().toString(2) + " MB"
  } else if (kb >= 1.0) {
    kb.toFloat().toString(2) + " KB"
  } else {
    "$this B"
  }
}

/**
 * Return the float receiver as a string display with numOfDec after the decimal (rounded)
 * (e.g. 35.72 with numOfDec = 1 will be 35.7, 35.78 with numOfDec = 2 will be 35.80)
 *
 * @param numOfDec number of decimal places to show (receiver is rounded to that number)
 * @return the String representation of the receiver up to numOfDec decimal places
 */
fun Float.toString(numOfDec: Int): String {
  if (isNaN() || isInfinite()) return "--"
  val sign = if (this < 0f) "-" else ""
  val thisAbs = abs(this)
  val integerDigits = thisAbs.toInt()
  val floatDigits = ((thisAbs - integerDigits) * 10f.pow(numOfDec)).roundToInt()
  return "$sign$integerDigits.$floatDigits"
}
