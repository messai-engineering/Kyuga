package com.twelfthmile.kyuga.states

import com.twelfthmile.kyuga.model.StateContext
import com.twelfthmile.kyuga.model.StateResult
import com.twelfthmile.kyuga.utils.*

internal fun state43(
    context: StateContext
): StateResult {
    val state: Int
    val i = context.index
    with(context) {
        val c = nextChar
        state = if (Util.isLowerAlpha(c) || Util.isNumber(c)) {
            contextMap.setType(TY_VPD, TY_VPD)
            contextMap.append(delimiterStack.pop())
            contextMap.append(c)
            44
        } else {
            -1
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state44(
    context: StateContext
): StateResult {
    val state: Int
    val i = context.index
    with(context) {
        val c = nextChar
        state = if (Util.isLowerAlpha(c) || Util.isNumber(c) || c.toInt() == CH_FSTP) {
            contextMap.append(c)
            44
        } else
            -1
    }
    return StateResult(state, i, context.counter)
}

internal fun state45(
    context: StateContext
): StateResult {
    var state = 45
    var i = context.index
    with(context) {
        val c = nextChar
        if (Util.isNumber(c)) {
            contextMap.append(c)
        } else if (c.toInt() == CH_HYPH && i + 1 < str.length && Util.isNumber(str[i + 1])) {
            state = 39
        } else {
            i -= if (i - 1 > 0 && str[i - 1].toInt() == CH_COMA)
                2
            else
                1
            state = -1
        }
    }
    return StateResult(state, i, context.counter)
}