package com.twelfthmile.kyuga.states

import com.twelfthmile.kyuga.model.StateContext
import com.twelfthmile.kyuga.model.StateResult
import com.twelfthmile.kyuga.utils.*

internal fun state25(
   context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        val c = nextChar
        if (Util.isNumber(c)) {
            contextMap.setType(TY_DTE, DT_YYYY)
            contextMap.put(DT_MM, c)
            state = 26
        } else if (i > 0 && Util.checkTypes(root, "FSA_TIMES", str.substring(i))?.let {
                    contextMap.setType(TY_TME, null)
                    var s = str.substring(0, i)
                    if (it.second == "mins")
                        s = "00$s"
                    s.extractTime(contextMap.getValMap())
                    i += it.first
                    true
                } == true) {
            state = -1
        } else {
            //it wasn't year, it was just a number
            i -= 2
            state = -1
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state26(
   context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        val c = nextChar
        if (Util.isNumber(c)) {
            contextMap.append(c)
            state = 27
        } else {
            contextMap.setType(TY_STR, TY_STR)
            i -= 1
            state = -1
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state27(
    context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        val c = nextChar
        if (Util.isDateOperator(c)) {
            delimiterStack.push(c)
            state = 28
        } else if (Util.isNumber(c)) {//it was a number, most probably telephone number
            if (contextMap.type == TY_DTE) {
                contextMap.setType(TY_NUM, TY_NUM)
            }
            contextMap.append(c)
            if ((delimiterStack.pop().toInt() == CH_SLSH || delimiterStack.pop()
                            .toInt() == CH_HYPH) && i + 1 < str.length && Util.isNumber(
                            str[i + 1]
                    ) && (i + 2 == str.length || Util.isDelimiter(str[i + 2]))
            ) {//flight time 0820/0950
                contextMap.setType(TY_TMS, TY_TMS)
                contextMap.append(str[i + 1])
                i += 1
                state = -1
            } else if (delimiterStack.pop().toInt() == CH_SPACE) {
                state = 41
            } else
                state = 12
        } else if (c.toInt() == 42 || c.toInt() == 88 || c.toInt() == 120) {//*Xx
            contextMap.setType(TY_ACC, TY_ACC)
            contextMap.append('X')
            state = 11
        } else {
            contextMap.setType(TY_STR, TY_STR)
            i -= 1
            state = -1
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state28(
    context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        val c = nextChar
        if (Util.isNumber(c)) {
            contextMap.put(DT_D, c)
            state = 29
        } else {
            contextMap.setType(TY_STR, TY_STR)
            i -= 2
            state = -1
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state29(
    context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        val c = nextChar
        if (Util.isNumber(c)) {
            contextMap.append(c)
        } else
            i -= 1
        state = -1
    }
    return StateResult(state, i, context.counter)
}

internal fun state30(
    context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        val c = nextChar
        if (c.toInt() == CH_COMA || c.toInt() == CH_SPACE)
            state = 30
        else if (Util.isNumber(c)) {
            contextMap.put(DT_D, c)
            state = 31
        } else {
            contextMap.type = TY_DTE
            i -= 1
            state = -1
        }
    }
    return StateResult(state, i, context.counter)
}

