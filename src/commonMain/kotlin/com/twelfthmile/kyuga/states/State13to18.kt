package com.twelfthmile.kyuga.states

import com.twelfthmile.kyuga.Kyuga
import com.twelfthmile.kyuga.model.StateContext
import com.twelfthmile.kyuga.model.StateResult
import com.twelfthmile.kyuga.utils.*

internal fun state13(
   context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        val c = nextChar
        if (Util.isNumber(c)) {
            contextMap.append(c)
            state = 13
        } else if (c.toInt() == 42 || c.toInt() == 88 || c.toInt() == 120) {
            //*Xx
            contextMap.append('X')
            state = 13
        } else if (c.toInt() == CH_FSTP && config.containsKey(YUGA_SOURCE_CONTEXT) && config[YUGA_SOURCE_CONTEXT] == YUGA_SC_CURR) { //LIC **150.00 fix
            contextMap.setType(TY_AMT, TY_AMT)
            contextMap.put(TY_AMT, contextMap[TY_AMT]!!.replace("X".toRegex(), ""))
            contextMap.append(c)
            state = 10
        } else if (c.toInt() == CH_FSTP && str.lookAheadForInstr(i).let {
                    if (it > 0) {
                        i = it
                        true
                    } else {
                        false
                    }
                }) {
            // emptiness
            state = 13
        } else {
            i -= 1
            state = -1
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state14(
   context: StateContext
): StateResult {
    var state = 14
    var i = context.index
    with(context) {
        val c = nextChar
        if (Util.isNumber(c)) {
            contextMap.append(c)
        } else if (c.toInt() == CH_PCT) {
            contextMap.setType(TY_PCT, TY_PCT)
            state = -1
        } else if ((c == 'k' || c == 'c') && i + 1 < str.length && str[i + 1] == 'm') {
            contextMap.setType(TY_DST, TY_DST)
            i += 1
            state = -1
        } else if ((c == 'k' || c == 'm') && i + 1 < str.length && str[i + 1] == 'g') {
            contextMap.setType(TY_WGT, TY_WGT)
            i += 1
            state = -1
        } else {
            var tempBrk = true
            if (c.toInt() == CH_FSTP && i + 1 < str.length && Util.isNumber(str[i + 1])) {
                val samt = contextMap[contextMap.type!!]
                if (samt!!.contains(".")) {
                    val samtarr = samt.split("\\.".toRegex())
                    if (samtarr.size == 2) {
                        contextMap.type = TY_DTE
                        contextMap.put(DT_D, samtarr[0])
                        contextMap.put(DT_MM, samtarr[1])
                        state = 19
                        tempBrk = false
                    }
                }
            }
            if (tempBrk) {
                i -= 1
                state = -1
            }
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state15(
    context: StateContext
): StateResult {
    var state = 15
    var i = context.index
    var localCounter = context.counter
    with(context) {
        val c = nextChar
        if (Util.isNumber(c)) {
            localCounter++
            contextMap.append(c)
        } else if (c.toInt() == CH_COMA)
        //comma
            state = 12
        else if (c.toInt() == CH_FSTP) { //dot
            contextMap.append(c)
            state = 10
        } else if ((c.toInt() == 42 || c.toInt() == 88 || c.toInt() == 120) && i + 1 < str.length && (Util.isNumber(
                        str[i + 1]
                ) || str[i + 1].toInt() == CH_HYPH || str[i + 1].toInt() == 42 || str[i + 1].toInt() == 88 || str[i + 1].toInt() == 120)
        ) {//*Xx
            contextMap.setType(TY_ACC, TY_ACC)
            contextMap.append('X')
            state = 11
        } else if (c.toInt() == CH_SPACE && i + 2 < str.length && Util.isNumber(str[i + 1]) && Util.isNumber(
                        str[i + 2]
                )
        ) {
            state = 41
        } else {
            i -= 1
            state = -1
        }//                    else if (c == Constants.CH_ATRT) {
        //                        delimiterStack.push(c);
        //                        state = 43;
        //                    }
    }
    return StateResult(state, i, context.counter)
}

internal fun state16(
    context: StateContext
): StateResult {
    var state: Int
    var i = context.index
    with(context) {
        val c = nextChar
        if (Util.isNumber(c)) {
            contextMap.upgrade(c)
            state = 17
        } else if (c.toInt() == CH_SPACE || c.toInt() == CH_COMA)
            state = 16
        else if (Util.checkTypes(root, "FSA_MONTHS", str.substring(i))?.let {
                    contextMap.put(DT_MMM, it.second)
                    i += it.first
                    true
                } == true) {
            state = 24
        } else if (c.toInt() == CH_FSTP) { //dot
            contextMap.setType(TY_NUM, TY_NUM)
            contextMap.append(c)
            state = 10
        } else if (i > 0 && Util.checkTypes(root, "FSA_TIMES", str.substring(i))?.let {
                    contextMap.setType(TY_TME, null)
                    var s = str.substring(0, i)
                    if (it.second == "mins" || it.second == "minutes")
                        s = "00$s"
                    s.extractTime(contextMap.getValMap())
                    i += it.first
                    true
                } == true) {
            state = -1
        } else {//this is just a number, not a date
            //to cater to 16 -Nov -17
            if (delimiterStack.pop()
                            .toInt() == CH_SPACE && c.toInt() == CH_HYPH && i + 1 < str.length && (Util.isNumber(
                            str[i + 1]
                    ) || Util.checkTypes(root, "FSA_MONTHS", str.substring(i + 1)) != null)
            ) {
                state = 16
            } else {
                contextMap.setType(TY_NUM, TY_NUM)
                var j = i
                while (!Util.isNumber(str[j]))
                    j--
                i = j
                state = -1
            }
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state17(
    context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        val c = nextChar
        if (Util.isNumber(c)) {
            contextMap.append(c)
            state = 18
        } else if (Util.isDateOperator(c)) {
            delimiterStack.push(c)
            state = 19
        } else if (c.toInt() == CH_COMA && delimiterStack.pop().toInt() == CH_COMA) { //comma
            contextMap.setType(TY_NUM, TY_NUM)
            state = 12
        } else if (c.toInt() == CH_FSTP && delimiterStack.pop().toInt() == CH_COMA) { //dot
            contextMap.setType(TY_NUM, TY_NUM)
            contextMap.append(c)
            state = 10
        } else {
            contextMap.setType(TY_STR, TY_STR)
            i -= 1
            state = -1
        }
    }
    return StateResult(state, i, context.counter)
}

internal fun state18(
        context: StateContext
): StateResult {
    val state: Int
    var i = context.index
    with(context) {
        val c = nextChar
        if (Util.isDateOperator(c)) {
            delimiterStack.push(c)
            state = 19
        } else if (Util.isNumber(c) && delimiterStack.pop().toInt() == CH_COMA) {
            contextMap.setType(TY_NUM, TY_NUM)
            state = 12
            contextMap.append(c)
        } else if (Util.isNumber(c) && delimiterStack.pop().toInt() == CH_HYPH) {
            contextMap.setType(TY_NUM, TY_NUM)
            state = 42
            contextMap.append(c)
        } else if (c.toInt() == CH_COMA && delimiterStack.pop().toInt() == CH_COMA) { //comma
            contextMap.setType(TY_NUM, TY_NUM)
            state = 12
        } else if (c.toInt() == CH_FSTP && delimiterStack.pop().toInt() == CH_COMA) { //dot
            contextMap.setType(TY_NUM, TY_NUM)
            contextMap.append(c)
            state = 10
        } else if (c.toInt() == CH_FSTP && contextMap.contains(DT_D) && contextMap.contains(DT_MM)) { //dot
            state = -1
        } else {
            contextMap.setType(TY_STR, TY_STR)
            i -= 1
            state = -1
        }
    }
    return StateResult(state, i, context.counter)
}