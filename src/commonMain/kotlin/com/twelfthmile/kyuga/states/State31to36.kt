package com.twelfthmile.kyuga.states

import com.twelfthmile.kyuga.model.StateContext
import com.twelfthmile.kyuga.model.StateResult
import com.twelfthmile.kyuga.utils.*

internal fun state31(
    context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        val c = nextChar
        if (Util.isNumber(c)) {
            contextMap.append(c)
            state = 32
        } else if (Util.checkTypes(root, "FSA_MONTHS", str.substring(i))?.let {
                    contextMap.put(DT_MMM, it.second)
                    i += it.first
                    true
                } == true) {
            state = 24
        } else if (c.toInt() == CH_COMA || c.toInt() == CH_SPACE)
            state = 32
        else {
            i -= 1
            state = -1
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state32(
    context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        val c = nextChar
        if (Util.checkTypes(root, "FSA_MONTHS", str.substring(i))?.let {
                    contextMap.put(DT_MMM, it.second)
                    i += it.first
                    true
                } == true) {
            state = 24
        } else if (c.toInt() == CH_COMA || c.toInt() == CH_SPACE)
            state = 32
        else if (Util.checkTypes(root, "FSA_DAYSFFX", str.substring(i))?.let {
                    i += it.first
                    true
                } == true) {
            state = 32
        } else {
            var j = i
            while (!Util.isNumber(str[j]))
                j--
            i = j
            state = -1
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state33(
   context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        val c = nextChar
        if (Util.isNumber(c)) {
            contextMap.put(DT_D, c)
            state = 34
        } else if (c.toInt() == CH_SPACE || c.toInt() == CH_COMA || c.toInt() == CH_HYPH)
            state = 33
        else {
            contextMap.type = TY_DTE
            i -= 1
            state = -1
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state34(
    context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        val c = nextChar
        if (Util.isNumber(c)) {
            contextMap.append(c)
            state = 35
        } else if (c.toInt() == CH_SPACE || c.toInt() == CH_COMA)
            state = 35
        else {
            contextMap.type = TY_DTE
            i -= 1
            state = -1
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state35(
   context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        val c = nextChar
        if (Util.isNumber(c)) {
            if (i > 1 && Util.isNumber(str[i - 1])) {
                contextMap.convert(DT_D, DT_YYYY)
                contextMap.append(c)
            } else
                contextMap.put(DT_YY, c)
            state = 20
        } else if (c.toInt() == CH_SPACE || c.toInt() == CH_COMA)
            state = 40
        else {
            contextMap.type = TY_DTE
            i -= 1
            state = -1
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state36(
   context: StateContext
): StateResult? {
    var state = 36
    val i = context.index
    var localCounter = context.counter
    with(context) {
        val c = nextChar
        if (Util.isNumber(c)) {
            contextMap.append(c)
            localCounter++
        } else if (c.toInt() == CH_FSTP && i + 1 < str.length && Util.isNumber(str[i + 1])) {
            contextMap.append(c)
            state = 10
        } else if (c.toInt() == CH_HYPH && i + 1 < str.length && Util.isNumber(str[i + 1])) {
            delimiterStack.push(c)
            contextMap.append(c)
            state = 16
        } else {
            if (counter == 12 || Util.isNumber(str.substring(1, i)))
                contextMap.setType(TY_NUM, TY_NUM)
            else
                return null
            state = -1
        }
    }
    return StateResult(state, i, localCounter)
}