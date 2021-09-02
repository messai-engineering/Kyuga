package com.twelfthmile.kyuga

import com.twelfthmile.kyuga.expectations.MultDate
import com.twelfthmile.kyuga.expectations.formatDateDefault
import com.twelfthmile.kyuga.expectations.log
import com.twelfthmile.kyuga.regex.EMAIL_ADDRESS
import com.twelfthmile.kyuga.states.*
import com.twelfthmile.kyuga.model.StateContext
import com.twelfthmile.kyuga.types.*
import com.twelfthmile.kyuga.utils.*

fun Char.isAlpha(): Boolean = this in 'a'..'z' || this in 'A'..'Z'
private val TOKENIZE_REGEX = "[. ]".toRegex()

object Kyuga {

    private val D_DEBUG = true

    private val root: RootTrie
        get() = LazyHolder.root

    private object LazyHolder {
        var root = KyugaTrie().root
    }

    fun tokenise(message: List<String>): List<String> = message.map {
        val parseResponse = parse(it)
        parseResponse?.type ?: when (Util.checkForId(it)) {
            true -> if (it != "EMAILADDR") "IDVAL" else it
            false -> it
        }
    }

    fun tokenize(message: String): String {
        val cleanMessage = message
            .replace(EMAIL_ADDRESS, " EMAILADDR ")
        val candidateTokens = cleanMessage
            .split(TOKENIZE_REGEX)
            .map { it.trim() }
        return try {
            val tokens = tokenise(candidateTokens).filter { it.isNotBlank() }
            tokens.filterIndexed { index, it ->
                if (it.isNotEmpty()) {
                    if (index > 0)
                        tokens[index - 1] != it
                    else
                        true
                } else
                    false
            }
        } catch (e: Exception) {
            candidateTokens
        }.joinToString(" ")
    }

    /**
     * Returns Pair of index upto which date was read and the date object
     *
     * @param str date string
     * @return A last index for date string, b date object
     * returns null if string is not of valid date format
     */
    fun parseDate(str: String): Pair<Int, MultDate>? {
        val configMap = generateDefaultConfig()
        return getIntegerDatePair(str, configMap)
    }

    private fun getIntegerDatePair(str: String, configMap: Map<String, String>): Pair<Int, MultDate>? {
        val (a, b) = parseInternal(str, configMap) ?: return null
        val d = b.getDate(configMap) ?: return null
        return Pair(a, d)
    }

    /**
     * Returns Pair of index upto which date was read and the date object
     *
     * @param str    date string
     * @param config pass the message date string for defaulting
     * @return A last index for date string, b date object
     * returns null if string is not of valid date format
     */

    fun parseDate(str: String, config: Map<String, String>): Pair<Int, MultDate>? {
        return getIntegerDatePair(str, config)
    }

    /**
     * Returns Response containing data-type, captured string and index upto which data was read
     *
     * @param str    string to be parsed
     * @param config config for parsing (Eg: date-defaulting)
     * @return Yuga Response type
     */

    fun parse(str: String, config: Map<String, String>): Response? {
        return getResponse(str, config)
    }

    private fun getResponse(str: String, config: Map<String, String>): Response? {
        val p = parseInternal(str, config) ?: return null
        val (a, b) = prepareResult(str, p, config)!!
        return when (b) {
            is MultDate -> Response(a, p.second.getValMap(), b, p.first)
            is String -> Response(a, p.second.getValMap(), b, p.first)
            else -> throw IllegalArgumentException("Error while creating response")
        }
    }

    /**
     * Returns Response containing data-type, captured string and index upto which data was read
     *
     * @param str string to be parsed
     * @return Yuga Response type
     */

    fun parse(str: String): Response? {
        val configMap = generateDefaultConfig()
        return getResponse(str, configMap)
    }

    // Pair <Type,String>
    private fun prepareResult(
        str: String,
        p: Pair<Int, FsaContextMap>,
        config: Map<String, String>
    ): Pair<String, Any>? {
        val index = p.first
        val map = p.second
        if (map.type == TY_DTE) {
            if (map.contains(DT_MMM) && map.size() < 3)
            //may fix
                return Pair(TY_STR, str.substring(0, index))
            if (map.contains(DT_HH) && map.contains(DT_mm) && !map.contains(DT_D) && !map.contains(
                    DT_DD
                ) && !map.contains(DT_MM) && !map.contains(DT_MMM) && !map.contains(DT_YY) && !map.contains(
                    DT_YYYY
                )
            ) {
                map.setType(TY_TME, null)
                map.setVal("time", map[DT_HH] + ":" + map[DT_mm])
                return Pair(TY_TME, str.substring(0, index))
            }
            val d = map.getDate(config)
            return if (d != null)
                p.second.type?.let { Pair<String, Any>(it, d) }
            else
                Pair(TY_STR, str.substring(0, index))
        } else {
            return if (map[map.type!!] != null) {
                if (map.type == TY_ACC && config.containsKey(YUGA_SOURCE_CONTEXT) && config[YUGA_SOURCE_CONTEXT] == YUGA_SC_CURR) {
                    Pair<String, Any>(TY_AMT, map[map.type!!]!!.replace("X".toRegex(), ""))
                } else {
                    p.second.type?.let { map[map.type!!]?.let { tg -> Pair<String, Any>(it, tg) } }
                }
            } else
                p.second.type?.let { Pair<String, Any>(it, str.substring(0, index)) }

        }
    }

    fun generateDefaultConfig(): Map<String, String> {
        val config = mutableMapOf<String, String>()
        config[YUGA_CONF_DATE] = formatDateDefault(MultDate())
        return config
    }

    fun checkTypes(type: String, word: String): Pair<Int, String>? {
        return Util.checkTypes(root, type, word)
    }


    private fun parseInternal(inputStr: String, config: Map<String, String>): Pair<Int, FsaContextMap>? {
        var str = inputStr
        var state = 1
        var i = 0
        var c: Char
        val map = FsaContextMap()
        val delimiterStack = DelimiterStack()
        str = str.toLowerCase()
        var counter = 0
        while (state > 0 && i < str.length) {
            c = str[i]
            fun getStateContext() = StateContext(root, str, c, map, i, delimiterStack, config, counter)
            when (state) {
                1 -> state1(getStateContext())?.let {
                    state = it.state
                    i = it.index
                } ?: return null

                2 -> state2(getStateContext()).let {
                    state = it.state
                    i = it.index
                }
                3 -> state3(getStateContext()).let {
                    state = it.state
                    i = it.index
                }
                4 //hours to mins
                -> state4(getStateContext()).let {
                    state = it.state
                    i = it.index
                }
                5 -> state5(getStateContext()).let {
                    state = it.state
                    i = it.index
                }
                6 //for seconds
                -> state6(getStateContext()).let {
                    state = it.state
                    i = it.index
                }
                7 -> state7(getStateContext()).let {
                    state = it.second
                    i = it.first
                }
                8 -> state8(getStateContext()).let {
                    state = it.second
                    i = it.first
                }
                9 -> state9(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }//handle for num case
                10 -> state10(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                11 -> state11(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                12 -> state12(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                13 -> state13(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                14 -> state14(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                15 -> state15(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                16 -> state16(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }//we should handle amt case, where comma led to 16 as opposed to 12
                17 -> state17(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }//we should handle amt case, where comma led to 16,17 as opposed to 12
                18 -> state18(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }//we should handle amt case, where comma led to 16,17 as opposed to 12
                19 //year
                -> state19(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                20 //year++
                -> state20(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                21 -> state21(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                22 -> state22(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                24 -> state24(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                25//potential year start comes here
                -> state25(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                26 -> state26(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                27 -> state27(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                28 -> state28(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                29 -> state29(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                30 -> state30(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                31 -> state31(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                32 -> state32(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                33 -> state33(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                34 -> state34(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                35 -> state35(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                36 -> state36(getStateContext())?.let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                } ?: return null
                37 -> state37(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                38 -> state38(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                39//instrno
                -> state39(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                40 -> state40(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                41//for phone numbers; same as 12 + space; coming from 27
                -> state41(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                42 //18=12 case, where 7-2209 was becoming amt as part of phn support
                -> state42(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                43 //1234567890@ybl
                -> state43(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                44 -> state44(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
                45 -> state45(getStateContext()).let {
                    state = it.state
                    i = it.index
                    counter = it.counter
                }
            }
            i++
            if (D_DEBUG) {
                log("ch:" + c + " state:" + state + " map:" + map.print())
            }
        }
        if (map.type == null)
            return null
        //sentence end cases
        if (state == 10) {
            map.pop()
            i -= 1
        } else if (state == 36) {
            if (counter == 12 || Util.isNumber(str.substring(1, i)))
                map.setType(TY_NUM, TY_NUM)
            else
                return null
        }

        if (map.type == TY_AMT) {
            if (!map.contains(map.type!!) || map[map.type!!]!!.contains(".") && map[map.type!!]!!.split("\\.".toRegex())
                    .dropLastWhile { it.isEmpty() }.toTypedArray()[0].length > 8 || !map[map.type!!]!!.contains(
                    "."
                ) && map[map.type!!]!!.length > 8
            )
                map.setType(TY_NUM, TY_NUM)
        }

        if (map.type == TY_NUM) {
            if (i < str.length && str[i].isAlpha() && !config.containsKey(YUGA_SOURCE_CONTEXT)) {
                var j = i
                while (j < str.length && str[j] != ' ')
                    j++
                map.setType(TY_STR, TY_STR)
                i = j
            } else if (map[TY_NUM] != null) {
                if (map[TY_NUM]!!.length == 10 && (map[TY_NUM]!![0] == '9' || map[TY_NUM]!![0] == '8' || map[TY_NUM]!![0] == '7'))
                    map.setVal("num_class", TY_PHN)
                else if (map[TY_NUM]!!.length == 12 && map[TY_NUM]!!.startsWith("91"))
                    map.setVal("num_class", TY_PHN)
                else if (map[TY_NUM]!!.length == 11 && map[TY_NUM]!!.startsWith("18"))
                    map.setVal("num_class", TY_PHN)
                else if (map[TY_NUM]!!.length == 11 && map[TY_NUM]!![0] == '0')
                    map.setVal("num_class", TY_PHN)
                else
                    map.setVal("num_class", TY_NUM)
            }
        } else if (map.type == TY_DTE && i + 1 < str.length) {
            val `in` = i + skip(str.substring(i))
            val sub = str.substring(`in`)
            if (`in` < str.length) {
                val pFSATimePrex = Util.checkTypes(root, "FSA_TIMEPRFX", sub)
                val pFSATz = Util.checkTypes(root, "FSA_TZ", sub)
                if (Util.isNumber(str[`in`]) || Util.checkTypes(
                        root,
                        "FSA_MONTHS",
                        sub
                    ) != null || Util.checkTypes(root, "FSA_DAYS", sub) != null
                ) {
                    val kl = parseInternal(sub, config)
                    if (kl != null && kl.second.type == TY_DTE) {
                        map.putAll(kl.second)
                        i = `in` + kl.first
                    }
                } else if (pFSATimePrex != null) {
                    val iTime = `in` + pFSATimePrex.first + 1 + skip(str.substring(`in` + pFSATimePrex.first + 1))
                    if (iTime < str.length && (Util.isNumber(str[iTime]) || Util.checkTypes(
                            root,
                            "FSA_DAYS",
                            str.substring(iTime)
                        ) != null)
                    ) {
                        val p_ = parseInternal(str.substring(iTime), config)
                        if (p_ != null && p_.second.type == TY_DTE) {
                            map.putAll(p_.second)
                            i = iTime + p_.first
                        }
                    }
                } else if (pFSATz != null) {
                    val j = skipForTZ(str.substring(`in` + pFSATz.first + 1), map)
                    i = `in` + pFSATz.first + 1 + j
                } else if (sub.toLowerCase().startsWith("pm") || sub.toLowerCase().startsWith("am")) {
                    //todo handle appropriately for pm
                    i = `in` + 2
                }
            }
        } else if (map.type == TY_TMS) {
            val v = map[map.type!!]
            if (v != null && v.length == 8 && Util.isHour(v[0], v[1]) && Util.isHour(v[4], v[5])) {
                extractTime(v.substring(0, 4), map.getValMap(), "dept")
                extractTime(v.substring(4, 8), map.getValMap(), "arrv")
            }
        }
        return Pair(i, map)
    }

    private fun skipForTZ(str: String, map: FsaContextMap): Int {
        var state = 1
        var i = 0
        var c: Char
        while (state > 0 && i < str.length) {
            c = str[i]
            when (state) {
                1 -> if (c.toInt() == CH_SPACE || c.toInt() == CH_PLUS || Util.isNumber(c))
                    state = 1
                else if (c.toInt() == CH_COLN)
                    state = 2
                else {
                    val s_ = str.substring(0, i).trim { it <= ' ' }
                    if (s_.length == 4 && Util.isNumber(s_)) {//we captured a year after IST Mon Sep 04 13:47:13 IST 2017
                        map.put(DT_YYYY, s_)
                        state = -2
                    } else
                        state = -1
                }
                2 ->
                    //todo re-adjust GMT time, current default +5:30 for IST
                    state = if (Util.isNumber(c))
                        3
                    else
                        -1
                3 -> state = if (Util.isNumber(c))
                    4
                else
                    -1
                4 -> state = if (c.toInt() == CH_SPACE)
                    5
                else
                    -2
                5 -> {
                    val sy = str.substring(i, i + 4)
                    if (i + 3 < str.length && Util.isNumber(sy)) {
                        map.put(DT_YYYY, sy)
                        i += 3
                    }
                    state = -2
                }
            }
            i++
        }
        val s_ = str.substring(0, i).trim { it <= ' ' }
        if (state == 1 && s_.length == 4 && Util.isNumber(s_))
        //we captured a year after IST Mon Sep 04 13:47:13 IST 2017
            map.put(DT_YYYY, s_)
        return if (state == -1) 0 else i
    }

    private fun skip(str: String): Int {
        var i = 0
        while (i < str.length) {
            if (str[i] == ' ' || str[i] == ',' || str[i] == '(' || str[i] == ':')
                i++
            else
                break
        }
        return i
    }

    private fun extractTime(str: String, valMap: MutableMap<String, String>, vararg prefix: String) {
        var pre = ""
        if (prefix.isNotEmpty())
            pre = prefix[0] + "_"
        val pattern = "([0-9]{2})([0-9]{2})?([0-9]{2})?".toRegex()
        val m = pattern.find(str)

        m?.let {
            val gps = it.groups
            valMap[pre + "time"] =
                gps[1]?.value.toString() + if (it.groups.size > 1 && gps[2] != null) ":" + gps[2]?.value.toString() else ":00"
        }
    }

    internal class DelimiterStack {
        private val stack: MutableList<Char> = mutableListOf()

        fun push(ch: Char) {
            stack.add(ch)
        }

        fun pop(): Char {
            return if (stack.isNotEmpty()) {
                stack[stack.size - 1]
            } else '~'
        }
    }

}