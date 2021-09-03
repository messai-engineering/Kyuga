package com.twelfthmile.kyuga.states

import com.twelfthmile.kyuga.model.StateContext
import com.twelfthmile.kyuga.model.StateResult
import com.twelfthmile.kyuga.utils.*

internal fun state7(
    context: StateContext
): StateResult {
    var i = context.index
    val state: Int
    with(context) {
        if (nextChar == 'a' && i + 1 < str.length && str[i + 1] == 'm') {
            i += 1
            val hh = contextMap[DT_HH]!!.toInt()
            if (hh == 12)
                contextMap.put(DT_HH, 0.toString())
        } else if (nextChar == 'p' && i + 1 < str.length && str[i + 1] == 'm') {
            val hh = contextMap[DT_HH]!!.toInt()
            if (hh != 12)
                contextMap.put(DT_HH, (hh + 12).toString())
            i += 1
        } else if (Util.checkTypes(root, "FSA_TIMES", str.substring(i))?.let {
                    i += it.first
                    true
                } == true) {
            // emptiness
        } else
            i -= 2
        state = -1
    }
    return StateResult(state, i, context.counter)
}

internal fun state8(
    context: StateContext
):StateResult {
    var state: Int
    var i = context.index
    with(context) {
        if (Util.isNumber(nextChar)) {
            contextMap.append(nextChar)
            state = 9
        } else {
            state = str.accAmtNumPct(root, i, contextMap, config)
            if (nextChar.toInt() == CH_SPACE && state == -1 && i + 1 < str.length && Util.isNumber(str[i + 1]))
                state = 12
            else if (nextChar.toInt() == CH_HYPH && state == -1 && i + 1 < str.length && Util.isNumber(str[i + 1]))
                state = 45
            else if (state == -1 && contextMap.type != TY_PCT)
                i -= 1
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state9(
    context: StateContext
): StateResult {
    var localCounter = context.counter
    val state: Int
    var i = context.index
    with(context) {
        if (Util.isDateOperator(nextChar)) {
            delimiterStack.push(nextChar)
            state = 25
        } else if (Util.isNumber(nextChar)) {
            contextMap.append(nextChar)
            localCounter = 5
            state = 15
        } else {
            state = str.accAmtNumPct(root, i, contextMap, config)
            if (state == -1 && contextMap.type != TY_PCT) {//NUM
                i -= 1
            }
        }//handle for num case
    }
    return StateResult(state, i, localCounter)
}

internal fun state10(
    context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        if (Util.isNumber(nextChar)) {
            contextMap.append(nextChar)
            contextMap.setType(TY_AMT, TY_AMT)
            state = 14
        } else { //saw a fullstop randomly
            contextMap.pop()//remove the dot which was appended
            i -= 2
            state = -1
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state11(
    context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        if (nextChar.toInt() == 42 || nextChar.toInt() == 88 || nextChar.toInt() == 120) {
            //*Xx
            contextMap.append('X')
            state = 11
        } else if (nextChar.toInt() == CH_HYPH)
            state = 11
        else if (Util.isNumber(nextChar)) {
            contextMap.append(nextChar)
            state = 13
        } else if (nextChar == ' ' && i + 1 < str.length &&
                (str[i + 1].toInt() == 42 ||
                        str[i + 1].toInt() == 88 ||
                        str[i + 1].toInt() == 120 ||
                        Util.isNumber(str[i + 1])))
            state = 11
        else if (nextChar.toInt() == CH_FSTP && str.lookAheadForInstr(i).let {
                    if (it > 0) {
                        i = it
                        true
                    } else {
                        false
                    }
                }) {
            state = 11
            // emptiness
        } else {
            i -= 1
            state = -1
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state12(
    context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        val c = nextChar
        if (Util.isNumber(c)) {
            contextMap.setType(TY_AMT, TY_AMT)
            contextMap.append(c)
            state = 12
        } else if (c.toInt() == CH_COMA)
        //comma
            state = 12
        else if (c.toInt() == CH_FSTP) { //dot
            contextMap.append(c)
            state = 10
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