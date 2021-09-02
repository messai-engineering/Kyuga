package com.twelfthmile.kyuga.states

import com.twelfthmile.kyuga.Kyuga
import com.twelfthmile.kyuga.model.StateContext
import com.twelfthmile.kyuga.model.StateResult
import com.twelfthmile.kyuga.utils.*
import com.twelfthmile.kyuga.utils.accAmtNumPct

internal fun state0(context: StateContext): StateResult {
    return StateResult(0, context.index, context.counter)
}
internal fun state1(
   context: StateContext
): StateResult? {
    val state: Int
    var i = context.index
    val isMonth by lazy { Util.checkTypes(context.root, "FSA_MONTHS",
            context.str.substring(i)) }
    val isDay by lazy { Util.checkTypes(context.root, "FSA_DAYS",
            context.str.substring(i)) }
    with(context) {
        when {
            Util.isNumber(context.nextChar) -> {
                contextMap.setType(TY_NUM, null)
                contextMap.put(TY_NUM, nextChar)
                state = 2
            }
            isMonth != null -> {
                contextMap.setType(TY_DTE, null)
                isMonth?.let {
                    contextMap.put(DT_MMM, it.second)
                    i += it.first
                }
                state = 33
            }
            isDay != null -> {
                contextMap.setType(TY_DTE, null)
                isDay?.let {
                    contextMap.put(DT_DD, it.second)
                    i += it.first
                }
                state = 30
            }
            nextChar.toInt() == CH_HYPH -> state = 37
            nextChar.toInt() == CH_LSBT -> state = 1
            else -> {
                state = str.accAmtNumPct(root, i, contextMap, Kyuga.generateDefaultConfig())
                if (contextMap.type == null)
                    return null
                if (state == -1 && contextMap.type != TY_PCT) {
                    i -= 1
                }
            }
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state2(
    context: StateContext
): StateResult {
    var i = context.index
    val state: Int
    with(context) {
        if (Util.isNumber(nextChar)) {
            contextMap.append(nextChar)
            state = 3
        } else if (Util.isTimeOperator(nextChar)) {
            delimiterStack.push(nextChar)
            contextMap.setType(TY_DTE, DT_HH)
            state = 4
        } else if (Util.isDateOperator(nextChar) || nextChar.toInt() == CH_COMA) {
            delimiterStack.push(nextChar)
            contextMap.setType(TY_DTE, DT_D)
            state = 16
        } else if (Util.checkTypes(root, "FSA_MONTHS", str.substring(i))?.let {
                    contextMap.setType(TY_DTE, DT_D)
                    contextMap.put(DT_MMM, it.second)
                    i += it.first
                    true
                } == true) {
            state = 24
        } else {
            state = str.accAmtNumPct(root, i, contextMap, config)
            if (state == -1 && contextMap.type != TY_PCT)
                i -= 1
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state3(
    context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        if (Util.isNumber(nextChar)) {
            contextMap.append(nextChar)
            state = 8
        } else if (Util.isTimeOperator(nextChar)) {
            delimiterStack.push(nextChar)
            contextMap.setType(TY_DTE, DT_HH)
            state = 4
        } else if (Util.isDateOperator(nextChar) || nextChar.toInt() == CH_COMA) {
            delimiterStack.push(nextChar)
            contextMap.setType(TY_DTE, DT_D)
            state = 16
        } else if (Util.checkTypes(root, "FSA_MONTHS", str.substring(i))?.let {
                    contextMap.setType(TY_DTE, DT_D)
                    contextMap.put(DT_MMM, it.second)
                    i += it.first
                    true
                } == true) {
            state = 24
        } else if (Util.checkTypes(root, "FSA_DAYSFFX", str.substring(i))?.let {
                    contextMap.setType(TY_DTE, DT_D)
                    i += it.first
                    true
                } == true) {
            state = 32
        } else {
            state = str.accAmtNumPct(root, i, contextMap, config)
            if (state == -1 && contextMap.type != TY_PCT)
                i -= 1
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state4(
    context: StateContext
): StateResult {
    var i = context.index
    val state: Int
    with(context) {
        if (Util.isNumber(nextChar)) {
            contextMap.upgrade(nextChar)//hh to mm
            state = 5
        } else { //saw a colon randomly, switch back to num from hours
            if (!contextMap.contains(DT_MMM))
                contextMap.setType(TY_NUM, TY_NUM)
            i -= 2 //move back so that colon is omitted
            state = -1
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state5(
        context: StateContext
): StateResult {
    var i = context.index
    val state: Int
    with(context) {
        if (Util.isNumber(nextChar)) {
            contextMap.append(nextChar)
            state = 5
        } else if (nextChar.toInt() == CH_COLN)
            state = 6
        else if (nextChar == 'a' && i + 1 < str.length && str[i + 1] == 'm') {
            i += 1
            state = -1
        } else if (nextChar == 'p' && i + 1 < str.length && str[i + 1] == 'm') {
            contextMap.put(DT_HH, (contextMap[DT_HH]!!.toInt() + 12).toString())
            i += 1
            state = -1
        } else if (Util.checkTypes(root, "FSA_TIMES", str.substring(i))?.let {
                    i += it.first
                    true
                } == true) {
            state = -1
        } else
            state = 7
    }
    return StateResult(state, i, context.counter)
}

internal fun state6(
        context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        if (Util.isNumber(nextChar)) {
            contextMap.upgrade(nextChar)
            if (i + 1 < str.length && Util.isNumber(str[i + 1]))
                contextMap.append(str[i + 1])
            i += 1
            state = -1
        } else
            state = -1
    }
    return StateResult(state, i, context.counter)
}
