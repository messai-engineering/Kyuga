package com.twelfthmile.kyuga

import com.example.app.BuildKonfig
import com.twelfthmile.kyuga.expectations.MultDate
import com.twelfthmile.kyuga.expectations.formatDateDefault
import com.twelfthmile.kyuga.expectations.log
import com.twelfthmile.kyuga.regex.EMAIL_ADDRESS
import com.twelfthmile.kyuga.states.*
import com.twelfthmile.kyuga.model.StateContext
import com.twelfthmile.kyuga.model.StateResult
import com.twelfthmile.kyuga.types.*
import com.twelfthmile.kyuga.utils.*

fun Char.isAlpha(): Boolean = this in 'a'..'z' || this in 'A'..'Z'
private val TOKENIZE_REGEX = "[. ]".toRegex()

object Kyuga {

    private val isDebug = BuildKonfig.isDebug.toBoolean()
    private val root: RootTrie
        get() = LazyHolder.root

    private object LazyHolder {
        var root = KyugaTrie().root
    }

    fun tokenize(message: String): String {
        val cleanMessage = message
            .replace(EMAIL_ADDRESS, " EMAILADDR ")
        val candidateTokens = cleanMessage
            .split(TOKENIZE_REGEX)
            .map { it.trim() }
        return try {
            val tokens = tokenize(candidateTokens).filter { it.isNotBlank() }
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

    fun tokenize(message: List<String>): List<String> = message.map {
        val parseResponse = parse(it)
        parseResponse?.type ?: when (Util.checkForId(it)) {
            true -> if (it != "EMAILADDR") "IDVAL" else it
            false -> it
        }
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

    private fun getIntegerDatePair(str: String, configMap: Map<String, String>): Pair<Int, MultDate>? {
        val (a, b) = parseInternal(str, configMap) ?: return null
        val d = b.getDate(configMap) ?: return null
        return Pair(a, d)
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
            val fsaLogicByState: ((context: StateContext) -> StateResult?) = when (state) {
                1 -> ::state1
                2 -> ::state2
                3 -> ::state3
                4 //hours to mins
                -> ::state4
                5 -> ::state5
                6 //for seconds
                -> ::state6
                7 -> ::state7
                8 -> ::state8
                9 -> ::state9//handle for num case
                10 -> ::state10
                11 -> ::state11
                12 -> ::state12
                13 -> ::state13
                14 -> ::state14
                15 -> ::state15
                16 -> ::state16//we should handle amt case, where comma led to 16 as opposed to 12
                17 -> ::state17//we should handle amt case, where comma led to 16,17 as opposed to 12
                18 -> ::state18//we should handle amt case, where comma led to 16,17 as opposed to 12
                19 //year
                -> ::state19
                20 //year++
                -> ::state20
                21 -> ::state21
                22 -> ::state22
                24 -> ::state24
                25//potential year start comes here
                -> ::state25
                26 -> ::state26
                27 -> ::state27
                28 -> ::state28
                29 -> ::state29
                30 -> ::state30
                31 -> ::state31
                32 -> ::state32
                33 -> ::state33
                34 -> ::state34
                35 -> ::state35
                36 -> ::state36
                37 -> ::state37
                38 -> ::state38
                39//instrno
                -> ::state39
                40 -> ::state40
                41//for phone numbers; same as 12 + space; coming from 27
                -> ::state41
                42 //18=12 case, where 7-2209 was becoming amt as part of phn support
                -> ::state42
                43 //1234567890@ybl
                -> ::state43
                44 -> ::state44
                45 -> ::state45
                else -> ::state0
            }
            fsaLogicByState(getStateContext())?.let {
                state = it.state
                i = it.index
                counter = it.counter
            } ?: return null
            i++
            if (isDebug) {
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
                v.substring(0, 4).extractTime(map.getValMap(), "dept")
                v.substring(4, 8).extractTime(map.getValMap(), "arrv")
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
                    state = if (s_.length == 4 && Util.isNumber(s_)) {//we captured a year after IST Mon Sep 04 13:47:13 IST 2017
                        map.put(DT_YYYY, s_)
                        -2
                    } else
                        -1
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