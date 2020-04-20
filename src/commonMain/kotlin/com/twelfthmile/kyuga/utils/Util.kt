package com.twelfthmile.kyuga.utils

import com.twelfthmile.kyuga.types.GenTrie
import com.twelfthmile.kyuga.types.Pair
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
        for (i in 0 until s.length)
            if (!isNumber(s[i]))
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
        var t: GenTrie? = root.next[type] ?: return null
        while (i < word.length) {
            val ch = word[i]
            if (t != null && t.leaf && !t.next.containsKey(ch) && isTypeEnd(ch))
                return t.token?.let { tkn -> Pair<Int, String>(i - 1, tkn) }
            if (t != null && t.child && t.next.containsKey(ch)) {
                t = t.next[ch]
            } else
                break
            i++
        }
        return if (t != null && t.leaf && i == word.length) t.token?.let { tkn -> Pair(i - 1, tkn) } else null
    }

    fun isTypeEnd(ch: Char): Boolean {
        return isNumber(ch) || ch.toInt() == CH_FSTP || ch.toInt() == CH_SPACE || ch.toInt() == CH_HYPH || ch.toInt() == CH_COMA || ch.toInt() == CH_SLSH || ch.toInt() == CH_RBKT || ch.toInt() == CH_PLUS || ch.toInt() == CH_STAR || ch == '\r' || ch == '\n' || ch == '\''
    }

    fun isAlpha(c: Char): Boolean {
        return c.toInt() in 65..90 || c.toInt() in 97..122
    }

    fun isUpperAlpha(c: Char): Boolean {
        return c.toInt() in 65..90
    }

    fun isLowerAlpha(c: Char): Boolean {
        return c.toInt() in 97..122
    }

    fun isUpperAlpha(str: String?): Boolean {
        if (str == null || str.isEmpty())
            return false
        for (i in 0 until str.length) {
            if (!isUpperAlpha(str[i]))
                return false
        }
        return true
    }

    fun isLowerAlpha(str: String?): Boolean {
        if (str == null || str.isEmpty())
            return false
        for (i in 0 until str.length) {
            if (!isLowerAlpha(str[i]))
                return false
        }
        return true
    }

    fun isAlpha(str: String?): Boolean {
        if (str == null || str.isEmpty())
            return false
        for (i in 0 until str.length) {
            if (!isAlpha(str[i]))
                return false
        }
        return true
    }

}
