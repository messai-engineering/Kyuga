package com.twelfthmile.kyuga.utils

import com.twelfthmile.kyuga.types.RootTrie

internal fun String.accAmtNumPct(
        rootTrie: RootTrie,
        i: Int,
        map: FsaContextMap,
        config: Map<String, String>
): Int {
    //acc num amt pct
    val c = this[i]
    val subStr = this.substring(i)

    val pFSAAmt = Util.checkTypes(rootTrie, "FSA_AMT", subStr)
    val pFSATimes = Util.checkTypes(rootTrie, "FSA_TIMES", subStr)

    if (c.toInt() == CH_FSTP) { //dot
        if (i == 0 && config.containsKey(YUGA_SOURCE_CONTEXT) && config[YUGA_SOURCE_CONTEXT] == YUGA_SC_CURR)
            map.setType(TY_AMT, TY_AMT)
        map.append(c)
        return 10
    } else if (c.toInt() == 42 || c.toInt() == 88 || c.toInt() == 120) {//*Xx
        map.setType(TY_ACC, TY_ACC)
        map.append('X')
        return 11
    } else if (c.toInt() == CH_COMA) { //comma
        return 12
    } else if (c.toInt() == CH_PCT || c.toInt() == CH_SPACE && i + 1 < this.length && this[i + 1].toInt() == CH_PCT) { //pct
        map.setType(TY_PCT, TY_PCT)
        return -1
    } else if (c.toInt() == CH_PLUS) {
        if (config.containsKey(YUGA_SOURCE_CONTEXT) && config[YUGA_SOURCE_CONTEXT] == YUGA_SC_CURR) {
            return -1
        }
        map.setType(TY_STR, TY_STR)
        return 36
    } else if (i > 0 && pFSAAmt != null) {
        map.index = pFSAAmt.first
        map.setType(TY_AMT, TY_AMT)
        map.append(pFSAAmt.second.getAmt())
        return 38
    } else if (i > 0 && pFSATimes != null) {
        val ind = i + pFSATimes.first
        map.index = ind
        map.setType(TY_TME, null)
        var s = this.substring(0, i)
        if (pFSATimes.second == "mins")
            s = "00$s"
        s.extractTime(map.getValMap())
        return 38
    } else
        return -1
}

internal fun String.getAmt(): String {
    return when (this) {
        "lakh", "lac" -> "00000"
        "k" -> "000"
        else -> ""
    }
}

internal fun String.extractTime(valMap: MutableMap<String, String>, vararg prefix: String) {
    var pre = ""
    if (prefix.isNotEmpty())
        pre = prefix[0] + "_"
    val pattern = "([0-9]{2})([0-9]{2})?([0-9]{2})?".toRegex()
    val m = pattern.find(this)

    m?.let {
        val gps = it.groups
        valMap[pre + "time"] =
                gps[1]?.value.toString() + if (it.groups.size > 1 && gps[2] != null) ":" + gps[2]?.value.toString() else ":00"
    }
}

internal fun String.lookAheadForInstr(index: Int): Int {
    var c: Char
    for (i in index until this.length) {
        c = this[i]
        if (c.toInt() != CH_FSTP) {
            return if (c.toInt() == 42 || c.toInt() == 88 || c.toInt() == 120 || Util.isNumber(c))
                i
            else -1
        }
    }
    return -1
}