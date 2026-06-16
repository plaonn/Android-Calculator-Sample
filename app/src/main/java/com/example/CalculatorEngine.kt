package com.example

import kotlin.math.*

data class Calculation(
    val formula: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)

object CalculatorEngine {

    fun preprocessExpression(expr: String): String {
        val s = expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")

        val sb = StringBuilder()
        var i = 0
        while (i < s.length) {
            val current = s[i]
            sb.append(current)

            if (i < s.length - 1) {
                val next = s[i + 1]
                val isCurrentTarget = current.isDigit() || current == ')' || current == '%' || current == 'π' || current == 'e'

                val nextString = s.substring(i + 1)
                val isNextTarget = next == '(' || next == 'π' || next == 'e' ||
                        nextString.startsWith("sin") ||
                        nextString.startsWith("cos") ||
                        nextString.startsWith("tan") ||
                        nextString.startsWith("log") ||
                        nextString.startsWith("ln") ||
                        nextString.startsWith("√")

                val isNextDigit = next.isDigit()

                if (isCurrentTarget && isNextTarget) {
                    sb.append('*')
                } else if (current == ')' && isNextDigit) {
                    sb.append('*')
                }
            }
            i++
        }
        return sb.toString()
    }

    fun evaluate(expression: String): Double {
        if (expression.isBlank()) return 0.0
        val prepared = preprocessExpression(expression)
            .replace("π", PI.toString())
            .replace("e", E.toString())
        return MathParser(prepared).parse()
    }

    fun evaluateRealTime(expression: String): String {
        if (expression.isBlank()) return ""
        try {
            var sanitized = expression.trim()
            
            // Temporarily trim trailing operators/functions for live sub-evaluation
            while (sanitized.isNotEmpty() && (
                sanitized.last() in "+-*/^(" || 
                sanitized.endsWith("sin") || 
                sanitized.endsWith("cos") || 
                sanitized.endsWith("tan") || 
                sanitized.endsWith("log") || 
                sanitized.endsWith("ln") || 
                sanitized.endsWith("√") ||
                sanitized.endsWith("−")
            )) {
                if (sanitized.last() in "+-*/^(") {
                    sanitized = sanitized.dropLast(1).trim()
                } else if (sanitized.endsWith("sin") || sanitized.endsWith("cos") || sanitized.endsWith("tan") || sanitized.endsWith("log")) {
                    sanitized = sanitized.dropLast(3).trim()
                } else if (sanitized.endsWith("ln")) {
                    sanitized = sanitized.dropLast(2).trim()
                } else if (sanitized.endsWith("√")) {
                    sanitized = sanitized.dropLast(1).trim()
                } else if (sanitized.endsWith("−")) {
                    sanitized = sanitized.dropLast(1).trim()
                }
            }

            // Auto-close open parentheses for real-time calculation preview
            val openCount = sanitized.count { it == '(' }
            val closeCount = sanitized.count { it == ')' }
            if (openCount > closeCount) {
                sanitized += ")".repeat(openCount - closeCount)
            }

            if (sanitized.isBlank()) return ""
            
            val result = evaluate(sanitized)
            if (result.isNaN() || result.isInfinite()) return ""
            return formatResult(result)
        } catch (e: Exception) {
            return ""
        }
    }

    fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        if (value == value.toLong().toDouble()) {
            return value.toLong().toString()
        }
        val df = java.text.DecimalFormat("#.##########")
        df.roundingMode = java.math.RoundingMode.HALF_UP
        return df.format(value)
    }

    private class MathParser(private val str: String) {
        private var pos = -1
        private var ch = 0

        private fun nextChar() {
            ch = if (++pos < str.length) str[pos].code else -1
        }

        private fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw RuntimeException("Unexpected character sequence: " + ch.toChar())
            return x
        }

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) x += parseTerm()
                else if (eat('-'.code)) x -= parseTerm()
                else break
            }
            return x
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.code)) x *= parseFactor()
                else if (eat('/'.code)) {
                    val divisor = parseFactor()
                    if (divisor == 0.0) throw ArithmeticException("Division by zero")
                    x /= divisor
                } else break
            }
            return x
        }

        private fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = this.pos
            if (eat('('.code)) {
                x = parseExpression()
                eat(')'.code)
            } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) {
                while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                x = str.substring(startPos, this.pos).toDouble()
            } else if (ch >= 'a'.code && ch <= 'z'.code || ch == '√'.code || ch == 's'.code || ch == 'c'.code || ch == 't'.code || ch == 'l'.code) {
                while (ch >= 'a'.code && ch <= 'z'.code || ch == '√'.code) nextChar()
                val func = str.substring(startPos, this.pos)
                if (func == "√") {
                    x = parseFactor()
                    if (x < 0) throw IllegalArgumentException("Square root of a negative value")
                    x = sqrt(x)
                } else {
                    x = parseFactor()
                    x = when (func) {
                        "sin" -> sin(Math.toRadians(x))
                        "cos" -> cos(Math.toRadians(x))
                        "tan" -> tan(Math.toRadians(x))
                        "log" -> log10(x)
                        "ln" -> ln(x)
                        else -> throw RuntimeException("Unknown function: $func")
                    }
                }
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }

            if (eat('^'.code)) x = x.pow(parseFactor())

            while (eat('%'.code)) {
                x /= 100.0
            }

            return x
        }
    }
}
