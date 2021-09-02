package com.twelfthmile.kyuga.states

import com.twelfthmile.kyuga.model.StateContext
import com.twelfthmile.kyuga.model.StateResult
import com.twelfthmile.kyuga.utils.*

internal fun state19(
    context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        val c = nextChar
        if (Util.isNumber(c)) {
            contextMap.upgrade(c)
            state = 20
        } else {
            i -= 2
            state = -1
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state20(
    context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        val c = nextChar
        when {
            Util.isNumber(c) -> {
                contextMap.append(c)
                state = 21
            }
            c == ':' -> {
                if (contextMap.contains(DT_YY))
                    contextMap.convert(DT_YY, DT_HH)
                else if (contextMap.contains(DT_YYYY))
                    contextMap.convert(DT_YYYY, DT_HH)
                state = 4
            }
            else -> {
                contextMap.remove(DT_YY)//since there is no one number year
                i -= 1
                state = -1
            }
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state21(
    context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        val c = nextChar
        when {
            Util.isNumber(c) -> {
                contextMap.upgrade(c)
                state = 22
            }
            c == ':' -> {
                if (contextMap.contains(DT_YY))
                    contextMap.convert(DT_YY, DT_HH)
                else if (contextMap.contains(DT_YYYY))
                    contextMap.convert(DT_YYYY, DT_HH)
                state = 4
            }
            else -> {
                i -= 1
                state = -1
            }
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state22(
   context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        val c = nextChar
        if (Util.isNumber(c)) {
            contextMap.append(c)
            state = -1
        } else {
            contextMap.remove(DT_YYYY)//since there is no three number year
            i -= 1
            state = -1
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state24(
        context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        val c = nextChar
        if (Util.isDateOperator(c) || c.toInt() == CH_COMA) {
            delimiterStack.push(c)
            state = 24
        } else if (Util.isNumber(c)) {
            contextMap.upgrade(c)
            state = 20
        } else if (c.toInt() == CH_SQOT && i + 1 < str.length && Util.isNumber(str[i + 1])) {
            state = 24
        } else if (c == '|') {
            state = 24
        } else {
            i -= 1
            state = -1
        }
    }
    return StateResult(state, i, context.counter)
}