package com.twelfthmile.kyuga.states

import com.twelfthmile.kyuga.model.StateContext
import com.twelfthmile.kyuga.model.StateResult
import com.twelfthmile.kyuga.utils.*

internal fun state37(
    context: StateContext
): StateResult {
    val state: Int
    val i = context.index
    with(context) {
        val c = nextChar
        state = when {
            Util.isNumber(c) -> {
                contextMap.setType(TY_AMT, TY_AMT)
                contextMap.put(TY_AMT, '-')
                contextMap.append(c)
                12
            }
            c.toInt() == CH_FSTP -> {
                contextMap.put(TY_AMT, '-')
                contextMap.append(c)
                10
            }
            else -> -1
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state38(
    context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        i = contextMap.index!!
        state = -1
    }
    return StateResult(state, i, context.counter)
}

internal fun state39(
    context: StateContext
): StateResult {
    var state = 39
    val i = context.index
    with(context) {
        val c = nextChar
        if (Util.isNumber(c))
            contextMap.append(c)
        else {
            contextMap.setType(TY_ACC, TY_ACC)
            state = -1
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state40(
   context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        val c = nextChar
        if (Util.isNumber(c)) {
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

internal fun state41(
    context: StateContext
): StateResult {
    var state = 41
    var i = context.index
    with(context) {
        val c = nextChar
        when {
            Util.isNumber(c) -> {
                contextMap.append(c)
            }
            c.toInt() == CH_SPACE -> state = 41
            else -> {
                i = if (i - 1 > 0 && str[i - 1].toInt() == CH_SPACE)
                    i - 2
                else
                    i - 1
                state = -1
            }
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state42(
    context: StateContext
): StateResult {
    var state = 42
    var i = context.index
    with(context) {
        val c = nextChar
        if (Util.isNumber(c)) {
            contextMap.append(c)
        } else if (c.toInt() == CH_HYPH && i + 1 < str.length && Util.isNumber(str[i + 1])) {
            state = 39
        } else {
            i -= 1
            state = -1
        }
    }
    return StateResult(state, i, context.counter)
}