package com.twelfthmile.kyuga.utils

import com.twelfthmile.kyuga.types.RootTrie

object Util {

    fun isHour(c1: Char, c2: Char): Boolean {
        return (c1 == '0' || c1 == '1') && isNumber(c2) || c1 == '2' && (c2 == '0' || c2 == '1' || c2 == '2' || c2 == '3' || c2 == '4')
    }

    fun isNumber(c: Char): Boolean {
        return c.toInt() in 48..57
    }

    fun isNumber(s: String?): Boolean {
        if (s == null || s.isEmpty())
            return false
        for (element in s)
            if (!isNumber(element))
                return false
        return true
    }

    fun isDateOperator(c: Char): Boolean {
        return c.toInt() == CH_SLSH || c.toInt() == CH_HYPH || c.toInt() == CH_SPACE
    }

    fun isDelimiter(c: Char): Boolean {
        return c.toInt() == CH_SPACE || c.toInt() == CH_FSTP || c.toInt() == CH_COMA || c.toInt() == CH_RBKT
    }

    fun isTimeOperator(c: Char): Boolean {
        return c.toInt() == CH_COLN //colon
    }

    fun checkTypes(root: RootTrie, type: String, word: String): Pair<Int, String>? {
        var i = 0
        var t = root.next[type] ?: return null
        while (i < word.length) {
            val ch = word[i]
            if (t.leaf && !t.next.containsKey(ch) && isTypeEnd(ch))
                return t.token?.let { tkn -> Pair(i - 1, tkn) }
            if (t.child && t.next.containsKey(ch)) {
                t = t.next[ch] ?: throw IllegalStateException("If check done, cannot be null")
            } else
                break
            i++
        }
        return if (t.leaf && i == word.length) t.token?.let { tkn -> Pair(i - 1, tkn) } else null
    }

    private fun isTypeEnd(ch: Char): Boolean {
        return isNumber(ch) || ch.toInt() == CH_FSTP || ch.toInt() == CH_SPACE || ch.toInt() == CH_HYPH || ch.toInt() == CH_COMA || ch.toInt() == CH_SLSH || ch.toInt() == CH_RBKT || ch.toInt() == CH_PLUS || ch.toInt() == CH_STAR || ch == '\r' || ch == '\n' || ch == '\''
    }

    private fun isAlpha(c: Char): Boolean {
        return c.toInt() in 65..90 || c.toInt() in 97..122
    }

    private fun isUpperAlpha(c: Char): Boolean {
        return c.toInt() in 65..90
    }

    fun isLowerAlpha(c: Char): Boolean {
        return c.toInt() in 97..122
    }

    fun checkForId(str: String): Boolean{
        var state = 1
        var i = 0
        var c: Char
        var haveSeenUpper = false
        var haveSeenNumber = false
        var haveSeenLower = false
        val sb = StringBuilder("")
        if (i < str.length && str[i].toInt() == CH_SQOT) i++
        while (state > 0 && i < str.length) {
            c = str[i]
            when (state) {
                1 -> if (isNumber(c) || isAlpha(c)) {
                    when {
                        isUpperAlpha(c) -> haveSeenUpper = true
                        isLowerAlpha(c) -> haveSeenLower = true
                        else -> haveSeenNumber = true
                    }
                    sb.append(c)
                    state = 2
                } else state = -1
                2 -> if (isNumber(c) || isAlpha(c) || c.toInt() == CH_UNSC) {
                    when {
                        isUpperAlpha(c) -> haveSeenUpper = true
                        isLowerAlpha(c) -> haveSeenLower =
                            true
                        isNumber(c) -> haveSeenNumber = true
                    }
                    sb.append(c)
                    state = 2
                } else if (c.toInt() == CH_SQOT) state = 2 else state = -1
            }
            i++
        }
        val validID = if (haveSeenUpper) {
            haveSeenNumber
        } else {
            if (!haveSeenNumber) false else {
                haveSeenLower
            }
        }
        return sb.isNotEmpty() && validID
    }
}
