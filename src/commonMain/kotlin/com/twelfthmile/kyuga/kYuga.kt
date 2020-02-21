package com.twelfthmile.kyuga

import com.twelfthmile.kyuga.expectations.MultDate
import com.twelfthmile.kyuga.expectations.formatDateDefault
import com.twelfthmile.kyuga.expectations.log
import com.twelfthmile.kyuga.types.GenTrie
import com.twelfthmile.kyuga.types.Pair
import com.twelfthmile.kyuga.types.Response
import com.twelfthmile.kyuga.types.RootTrie
import com.twelfthmile.kyuga.utils.FsaContextMap
import com.twelfthmile.kyuga.utils.KYugaConstants
import com.twelfthmile.kyuga.utils.Util

fun Char.isAlpha(): Boolean = this in 'a'..'z' || this in 'A'..'Z'

object Kyuga {

    private val D_DEBUG = true

    private val root: RootTrie
        get() = LazyHolder.root

    private object LazyHolder {
        internal var root = createRoot()
    }

//    fun init() {
//        val root = root
//    }

    private fun createRoot(): RootTrie {
        val root = RootTrie()
        root.next["FSA_MONTHS"] = GenTrie()
        root.next["FSA_DAYS"] = GenTrie()
        root.next["FSA_TIMEPRFX"] = GenTrie()
        root.next["FSA_AMT"] = GenTrie()
        root.next["FSA_TIMES"] = GenTrie()
        root.next["FSA_TZ"] = GenTrie()
        root.next["FSA_DAYSFFX"] = GenTrie()
        root.next["FSA_UPI"] = GenTrie()
        seeding(KYugaConstants.FSA_MONTHS, root.next["FSA_MONTHS"])
        seeding(KYugaConstants.FSA_DAYS, root.next["FSA_DAYS"])
        seeding(KYugaConstants.FSA_TIMEPRFX, root.next["FSA_TIMEPRFX"])
        seeding(KYugaConstants.FSA_AMT, root.next["FSA_AMT"])
        seeding(KYugaConstants.FSA_TIMES, root.next["FSA_TIMES"])
        seeding(KYugaConstants.FSA_TZ, root.next["FSA_TZ"])
        seeding(KYugaConstants.FSA_DAYSFFX, root.next["FSA_DAYSFFX"])
        seeding(KYugaConstants.FSA_UPI, root.next["FSA_UPI"])
        return root
    }

    private fun seeding(type: String, root: GenTrie?) {
        var t: GenTrie?
        var c = 0
        for (fsaCldr in type.split(",").toTypedArray()) {
            c++
            t = root
            val len = fsaCldr.length
            var i = 0
            while (i < len) {
                val ch = fsaCldr[i]
                t!!.child = true
                if (!t.next.containsKey(ch)) t.next[ch] = GenTrie()
                t = t.next[ch]
                if (i == len - 1) {
                    t!!.leaf = true
                    t.token = fsaCldr.replace(";", "")
                } else if (i < len - 1 && fsaCldr[i + 1].toInt() == 59) { //semicolon
                    t!!.leaf = true
                    t.token = fsaCldr.replace(";", "")
                    i++ //to skip semicolon
                }
                i++
            }
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
        return when(b) {
            is MultDate -> Response(a, p.b.getValMap(), b, p.a)
            is String -> Response(a, p.b.getValMap(), b, p.a)
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
        val index = p.a
        val map = p.b
        if (map.type == KYugaConstants.TY_DTE) {
            if (map.contains(KYugaConstants.DT_MMM) && map.size() < 3)
            //may fix
                return Pair(KYugaConstants.TY_STR, str.substring(0, index))
            if (map.contains(KYugaConstants.DT_HH) && map.contains(KYugaConstants.DT_mm) && !map.contains(KYugaConstants.DT_D) && !map.contains(
                    KYugaConstants.DT_DD
                ) && !map.contains(KYugaConstants.DT_MM) && !map.contains(KYugaConstants.DT_MMM) && !map.contains(KYugaConstants.DT_YY) && !map.contains(
                    KYugaConstants.DT_YYYY
                )
            ) {
                map.setType(KYugaConstants.TY_TME, null)
                map.setVal("time", map[KYugaConstants.DT_HH] + ":" + map[KYugaConstants.DT_mm])
                return Pair(KYugaConstants.TY_TME, str.substring(0, index))
            }
            val d = map.getDate(config)
            return if (d != null)
                p.b.type?.let { Pair<String, Any>(it, d) }
            else
                Pair(KYugaConstants.TY_STR, str.substring(0, index))
        } else {
            return if (map[map.type!!] != null) {
                if (map.type == KYugaConstants.TY_ACC && config.containsKey(KYugaConstants.YUGA_SOURCE_CONTEXT) && config[KYugaConstants.YUGA_SOURCE_CONTEXT] == KYugaConstants.YUGA_SC_CURR) {
                    Pair<String, Any>(KYugaConstants.TY_AMT, map[map.type!!]!!.replace("X".toRegex(), ""))
                } else {
                    p.b.type?.let { map[map.type!!]?.let { tg -> Pair<String, Any>(it, tg) } }
                }
            } else
                p.b.type?.let {  Pair<String, Any>(it, str.substring(0, index)) }

        }
    }

    private fun generateDefaultConfig(): Map<String, String> {
        val config = HashMap<String, String>()
        config[KYugaConstants.YUGA_CONF_DATE] = formatDateDefault(MultDate())
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
        loop@ while (state > 0 && i < str.length) {
            c = str[i]
            when (state) {
                1 -> if (Util.isNumber(c)) {
                    map.setType(KYugaConstants.TY_NUM, null)
                    map.put(KYugaConstants.TY_NUM, c)
                    state = 2
                } else if (Util.checkTypes(root, "FSA_MONTHS", str.substring(i))?.let {
                        map.setType(KYugaConstants.TY_DTE, null)
                        map.put(KYugaConstants.DT_MMM, it.b)
                        i += it.a
                    true} == true) {
                    state = 33
                } else if (Util.checkTypes(root, "FSA_DAYS", str.substring(i))?.let {
                        map.setType(KYugaConstants.TY_DTE, null)
                        map.put(KYugaConstants.DT_DD, it.b)
                        i += it.a
                        true
                    } == true) {
                    state = 30
                } else if (c.toInt() == KYugaConstants.CH_HYPH) {//it could be a negative number
                    state = 37
                } else if (c.toInt() == KYugaConstants.CH_LSBT) {//it could be an OTP
                    state = 1
                } else {
                    state = accAmtNumPct(str, i, map, config)
                    if (map.type == null)
                        return null
                    if (state == -1 && map.type != KYugaConstants.TY_PCT) {
                        i -= 1
                    }
                }

                2 -> if (Util.isNumber(c)) {
                    map.append(c)
                    state = 3
                } else if (Util.isTimeOperator(c)) {
                    delimiterStack.push(c)
                    map.setType(KYugaConstants.TY_DTE, KYugaConstants.DT_HH)
                    state = 4
                } else if (Util.isDateOperator(c) || c.toInt() == KYugaConstants.CH_COMA) {
                    delimiterStack.push(c)
                    map.setType(KYugaConstants.TY_DTE, KYugaConstants.DT_D)
                    state = 16
                } else if (checkMonthType(str, i)?.let {
                        map.setType(KYugaConstants.TY_DTE, KYugaConstants.DT_D)
                        map.put(KYugaConstants.DT_MMM, it.b)
                        i += it.a
                        true
                    } == true) {
                    state = 24
                } else {
                    state = accAmtNumPct(str, i, map, config)
                    if (state == -1 && map.type != KYugaConstants.TY_PCT)
                        i -= 1
                }
                3 -> if (Util.isNumber(c)) {
                    map.append(c)
                    state = 8
                } else if (Util.isTimeOperator(c)) {
                    delimiterStack.push(c)
                    map.setType(KYugaConstants.TY_DTE, KYugaConstants.DT_HH)
                    state = 4
                } else if (Util.isDateOperator(c) || c.toInt() == KYugaConstants.CH_COMA) {
                    delimiterStack.push(c)
                    map.setType(KYugaConstants.TY_DTE, KYugaConstants.DT_D)
                    state = 16
                } else if (checkMonthType(str, i)?.let {
                        map.setType(KYugaConstants.TY_DTE, KYugaConstants.DT_D)
                        map.put(KYugaConstants.DT_MMM, it.b)
                        i += it.a
                        true
                    } == true) {
                    state = 24
                } else if (Util.checkTypes(root, "FSA_DAYSFFX", str.substring(i))?.let {
                        map.setType(KYugaConstants.TY_DTE, KYugaConstants.DT_D)
                        i += it.a
                        true
                    } == true) {
                    state = 32
                } else {
                    state = accAmtNumPct(str, i, map, config)
                    if (state == -1 && map.type != KYugaConstants.TY_PCT)
                        i -= 1
                }
                4 //hours to mins
                -> if (Util.isNumber(c)) {
                    map.upgrade(c)//hh to mm
                    state = 5
                } else { //saw a colon randomly, switch back to num from hours
                    if (!map.contains(KYugaConstants.DT_MMM))
                        map.setType(KYugaConstants.TY_NUM, KYugaConstants.TY_NUM)
                    i -= 2 //move back so that colon is omitted
                    state = -1
                }
                5 -> if (Util.isNumber(c)) {
                    map.append(c)
                    state = 5
                } else if (c.toInt() == KYugaConstants.CH_COLN)
                    state = 6
                else if (c == 'a' && i + 1 < str.length && str[i + 1] == 'm') {
                    i += 1
                    state = -1
                } else if (c == 'p' && i + 1 < str.length && str[i + 1] == 'm') {
                    map.put(KYugaConstants.DT_HH, (map[KYugaConstants.DT_HH]!!.toInt() + 12).toString())
                    i += 1
                    state = -1
                } else if (Util.checkTypes(root, "FSA_TIMES", str.substring(i))?.let {
                        i += it.a
                        true
                    } == true) {
                    state = -1
                } else
                    state = 7
                6 //for seconds
                -> if (Util.isNumber(c)) {
                    map.upgrade(c)
                    if (i + 1 < str.length && Util.isNumber(str[i + 1]))
                        map.append(str[i + 1])
                    i = i + 1
                    state = -1
                } else
                    state = -1
                7 -> {
                    if (c == 'a' && i + 1 < str.length && str[i + 1] == 'm') {
                        i = i + 1
                        val hh = map[KYugaConstants.DT_HH]!!.toInt()
                        if (hh == 12)
                            map.put(KYugaConstants.DT_HH, 0.toString())
                    } else if (c == 'p' && i + 1 < str.length && str[i + 1] == 'm') {
                        val hh = map[KYugaConstants.DT_HH]!!.toInt()
                        if (hh != 12)
                            map.put(KYugaConstants.DT_HH, (hh + 12).toString())
                        i = i + 1
                    } else if (Util.checkTypes(root, "FSA_TIMES", str.substring(i))?.let {
                            i += it.a
                            true
                        } == true) {
                        // emptiness
                    } else
                        i -= 2
                    state = -1
                }
                8 -> if (Util.isNumber(c)) {
                    map.append(c)
                    state = 9
                } else {
                    state = accAmtNumPct(str, i, map, config)
                    if (c.toInt() == KYugaConstants.CH_SPACE && state == -1 && i + 1 < str.length && Util.isNumber(str[i + 1]))
                        state = 12
                    else if (c.toInt() == KYugaConstants.CH_HYPH && state == -1 && i + 1 < str.length && Util.isNumber(str[i + 1]))
                        state = 45
                    else if (state == -1 && map.type != KYugaConstants.TY_PCT)
                        i = i - 1
                }
                9 -> if (Util.isDateOperator(c)) {
                    delimiterStack.push(c)
                    state = 25
                } else if (Util.isNumber(c)) {
                    map.append(c)
                    counter = 5
                    state = 15
                } else {
                    state = accAmtNumPct(str, i, map, config)
                    if (state == -1 && map.type != KYugaConstants.TY_PCT) {//NUM
                        i = i - 1
                    }
                }//handle for num case
                10 -> if (Util.isNumber(c)) {
                    map.append(c)
                    map.setType(KYugaConstants.TY_AMT, KYugaConstants.TY_AMT)
                    state = 14
                } else { //saw a fullstop randomly
                    map.pop()//remove the dot which was appended
                    i = i - 2
                    state = -1
                }
                11 -> if (c.toInt() == 42 || c.toInt() == 88 || c.toInt() == 120)
                //*Xx
                    map.append('X')
                else if (c.toInt() == KYugaConstants.CH_HYPH)
                    state = 11
                else if (Util.isNumber(c)) {
                    map.append(c)
                    state = 13
                } else if (c == ' ' && i + 1 < str.length && (str[i + 1].toInt() == 42 || str[i + 1].toInt() == 88 || str[i + 1].toInt() == 120 || Util.isNumber(
                        str[i + 1]
                    ))
                )
                    state = 11
                else if (c.toInt() == KYugaConstants.CH_FSTP && lookAheadForInstr(str, i).let {
                        if (it > 0) {
                            i = it
                            true
                        } else {
                            false
                        }
                    }) {
                    // emptiness
                } else {
                    i -= 1
                    state = -1
                }
                12 -> if (Util.isNumber(c)) {
                    map.setType(KYugaConstants.TY_AMT, KYugaConstants.TY_AMT)
                    map.append(c)
                } else if (c.toInt() == KYugaConstants.CH_COMA)
                //comma
                    state = 12
                else if (c.toInt() == KYugaConstants.CH_FSTP) { //dot
                    map.append(c)
                    state = 10
                } else if (c.toInt() == KYugaConstants.CH_HYPH && i + 1 < str.length && Util.isNumber(str[i + 1])) {
                    state = 39
                } else {
                    if (i - 1 > 0 && str[i - 1].toInt() == KYugaConstants.CH_COMA)
                        i = i - 2
                    else
                        i = i - 1
                    state = -1
                }
                13 -> if (Util.isNumber(c))
                    map.append(c)
                else if (c.toInt() == 42 || c.toInt() == 88 || c.toInt() == 120)
                //*Xx
                    map.append('X')
                else if (c.toInt() == KYugaConstants.CH_FSTP && config.containsKey(KYugaConstants.YUGA_SOURCE_CONTEXT) && config[KYugaConstants.YUGA_SOURCE_CONTEXT] == KYugaConstants.YUGA_SC_CURR) { //LIC **150.00 fix
                    map.setType(KYugaConstants.TY_AMT, KYugaConstants.TY_AMT)
                    map.put(KYugaConstants.TY_AMT, map[KYugaConstants.TY_AMT]!!.replace("X".toRegex(), ""))
                    map.append(c)
                    state = 10
                } else if (c.toInt() == KYugaConstants.CH_FSTP&& lookAheadForInstr(str, i).let {
                        if (it > 0) {
                            i = it
                            true
                        } else {
                            false
                        }
                    }) {
                    // emptiness
                } else {
                    i = i - 1
                    state = -1
                }
                14 -> if (Util.isNumber(c)) {
                    map.append(c)
                } else if (c.toInt() == KYugaConstants.CH_PCT) {
                    map.setType(KYugaConstants.TY_PCT, KYugaConstants.TY_PCT)
                    state = -1
                } else if ((c == 'k' || c == 'c') && i + 1 < str.length && str[i + 1] == 'm') {
                    map.setType(KYugaConstants.TY_DST, KYugaConstants.TY_DST)
                    i += 1
                    state = -1
                } else if ((c == 'k' || c == 'm') && i + 1 < str.length && str[i + 1] == 'g') {
                    map.setType(KYugaConstants.TY_WGT, KYugaConstants.TY_WGT)
                    i += 1
                    state = -1
                } else {
                    if (c.toInt() == KYugaConstants.CH_FSTP && i + 1 < str.length && Util.isNumber(str[i + 1])) {
                        val samt = map[map.type!!]
                        if (samt!!.contains(".")) {
                            val samtarr = samt.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                            if (samtarr.size == 2) {
                                map.type = KYugaConstants.TY_DTE
                                map.put(KYugaConstants.DT_D, samtarr[0])
                                map.put(KYugaConstants.DT_MM, samtarr[1])
                                state = 19
                                break@loop
                            }
                        }
                    }
                    i = i - 1
                    state = -1
                }
                15 -> if (Util.isNumber(c)) {
                    counter++
                    map.append(c)
                } else if (c.toInt() == KYugaConstants.CH_COMA)
                //comma
                    state = 12
                else if (c.toInt() == KYugaConstants.CH_FSTP) { //dot
                    map.append(c)
                    state = 10
                } else if ((c.toInt() == 42 || c.toInt() == 88 || c.toInt() == 120) && i + 1 < str.length && (Util.isNumber(
                        str[i + 1]
                    ) || str[i + 1].toInt() == KYugaConstants.CH_HYPH || str[i + 1].toInt() == 42 || str[i + 1].toInt() == 88 || str[i + 1].toInt() == 120)
                ) {//*Xx
                    map.setType(KYugaConstants.TY_ACC, KYugaConstants.TY_ACC)
                    map.append('X')
                    state = 11
                } else if (c.toInt() == KYugaConstants.CH_SPACE && i + 2 < str.length && Util.isNumber(str[i + 1]) && Util.isNumber(
                        str[i + 2]
                    )
                ) {
                    state = 41
                } else {
                    i = i - 1
                    state = -1
                }//                    else if (c == Constants.CH_ATRT) {
                //                        delimiterStack.push(c);
                //                        state = 43;
                //                    }
                16 -> if (Util.isNumber(c)) {
                    map.upgrade(c)
                    state = 17
                } else if (c.toInt() == KYugaConstants.CH_SPACE || c.toInt() == KYugaConstants.CH_COMA)
                    state = 16
                else if (checkMonthType(str, i)?.let {
                        map.put(KYugaConstants.DT_MMM, it.b)
                        i += it.a
                        true
                    } == true) {
                    state = 24
                } else if (c.toInt() == KYugaConstants.CH_FSTP) { //dot
                    map.setType(KYugaConstants.TY_NUM, KYugaConstants.TY_NUM)
                    map.append(c)
                    state = 10
                } else if (i > 0 && Util.checkTypes(root, "FSA_TIMES", str.substring(i))?.let {
                        map.setType(KYugaConstants.TY_TME, null)
                        var s = str.substring(0, i)
                        if (it.b == "mins" || it.b == "minutes")
                            s = "00$s"
                        extractTime(s, map.getValMap())
                        i += it.a
                        true
                    } == true) {
                    state = -1
                } else {//this is just a number, not a date
                    //to cater to 16 -Nov -17
                    if (delimiterStack.pop().toInt() == KYugaConstants.CH_SPACE && c.toInt() == KYugaConstants.CH_HYPH && i + 1 < str.length && (Util.isNumber(
                            str[i + 1]
                        ) || checkMonthType(str, i + 1) != null)
                    ) {
                        state = 16
                    } else {
                        map.setType(KYugaConstants.TY_NUM, KYugaConstants.TY_NUM)
                        var j = i
                        while (!Util.isNumber(str[j]))
                            j--
                        i = j
                        state = -1
                    }
                }//we should handle amt case, where comma led to 16 as opposed to 12
                17 -> if (Util.isNumber(c)) {
                    map.append(c)
                    state = 18
                } else if (Util.isDateOperator(c)) {
                    delimiterStack.push(c)
                    state = 19
                } else if (c.toInt() == KYugaConstants.CH_COMA && delimiterStack.pop().toInt() == KYugaConstants.CH_COMA) { //comma
                    map.setType(KYugaConstants.TY_NUM, KYugaConstants.TY_NUM)
                    state = 12
                } else if (c.toInt() == KYugaConstants.CH_FSTP && delimiterStack.pop().toInt() == KYugaConstants.CH_COMA) { //dot
                    map.setType(KYugaConstants.TY_NUM, KYugaConstants.TY_NUM)
                    map.append(c)
                    state = 10
                } else {
                    map.setType(KYugaConstants.TY_STR, KYugaConstants.TY_STR)
                    i = i - 1
                    state = -1
                }//we should handle amt case, where comma led to 16,17 as opposed to 12
                18 -> if (Util.isDateOperator(c)) {
                    delimiterStack.push(c)
                    state = 19
                } else if (Util.isNumber(c) && delimiterStack.pop().toInt() == KYugaConstants.CH_COMA) {
                    map.setType(KYugaConstants.TY_NUM, KYugaConstants.TY_NUM)
                    state = 12
                    map.append(c)
                } else if (Util.isNumber(c) && delimiterStack.pop().toInt() == KYugaConstants.CH_HYPH) {
                    map.setType(KYugaConstants.TY_NUM, KYugaConstants.TY_NUM)
                    state = 42
                    map.append(c)
                } else if (c.toInt() == KYugaConstants.CH_COMA && delimiterStack.pop().toInt() == KYugaConstants.CH_COMA) { //comma
                    map.setType(KYugaConstants.TY_NUM, KYugaConstants.TY_NUM)
                    state = 12
                } else if (c.toInt() == KYugaConstants.CH_FSTP && delimiterStack.pop().toInt() == KYugaConstants.CH_COMA) { //dot
                    map.setType(KYugaConstants.TY_NUM, KYugaConstants.TY_NUM)
                    map.append(c)
                    state = 10
                } else if (c.toInt() == KYugaConstants.CH_FSTP && map.contains(KYugaConstants.DT_D) && map.contains(KYugaConstants.DT_MM)) { //dot
                    state = -1
                } else {
                    map.setType(KYugaConstants.TY_STR, KYugaConstants.TY_STR)
                    i = i - 1
                    state = -1
                }//we should handle amt case, where comma led to 16,17 as opposed to 12
                19 //year
                -> if (Util.isNumber(c)) {
                    map.upgrade(c)
                    state = 20
                } else {
                    i = i - 2
                    state = -1
                }
                20 //year++
                -> if (Util.isNumber(c)) {
                    map.append(c)
                    state = 21
                } else if (c == ':') {
                    if (map.contains(KYugaConstants.DT_YY))
                        map.convert(KYugaConstants.DT_YY, KYugaConstants.DT_HH)
                    else if (map.contains(KYugaConstants.DT_YYYY))
                        map.convert(KYugaConstants.DT_YYYY, KYugaConstants.DT_HH)
                    state = 4
                } else {
                    map.remove(KYugaConstants.DT_YY)//since there is no one number year
                    i = i - 1
                    state = -1
                }
                21 -> if (Util.isNumber(c)) {
                    map.upgrade(c)
                    state = 22
                } else if (c == ':') {
                    if (map.contains(KYugaConstants.DT_YY))
                        map.convert(KYugaConstants.DT_YY, KYugaConstants.DT_HH)
                    else if (map.contains(KYugaConstants.DT_YYYY))
                        map.convert(KYugaConstants.DT_YYYY, KYugaConstants.DT_HH)
                    state = 4
                } else {
                    i = i - 1
                    state = -1
                }
                22 -> if (Util.isNumber(c)) {
                    map.append(c)
                    state = -1
                } else {
                    map.remove(KYugaConstants.DT_YYYY)//since there is no three number year
                    i = i - 1
                    state = -1
                }
                24 -> if (Util.isDateOperator(c) || c.toInt() == KYugaConstants.CH_COMA) {
                    delimiterStack.push(c)
                    state = 24
                } else if (Util.isNumber(c)) {
                    map.upgrade(c)
                    state = 20
                } else if (c.toInt() == KYugaConstants.CH_SQOT && i + 1 < str.length && Util.isNumber(str[i + 1])) {
                    state = 24
                } else if (c == '|') {
                    state = 24
                } else {
                    i = i - 1
                    state = -1
                }
                25//potential year start comes here
                -> if (Util.isNumber(c)) {
                    map.setType(KYugaConstants.TY_DTE, KYugaConstants.DT_YYYY)
                    map.put(KYugaConstants.DT_MM, c)
                    state = 26
                } else if (i > 0 && Util.checkTypes(root, "FSA_TIMES", str.substring(i))?.let {
                        map.setType(KYugaConstants.TY_TME, null)
                        var s = str.substring(0, i)
                        if (it.b == "mins")
                            s = "00$s"
                        extractTime(s, map.getValMap())
                        i += it.a
                        true
                    } == true) {
                    state = -1
                } else {
                    //it wasn't year, it was just a number
                    i = i - 2
                    state = -1
                }
                26 -> if (Util.isNumber(c)) {
                    map.append(c)
                    state = 27
                } else {
                    map.setType(KYugaConstants.TY_STR, KYugaConstants.TY_STR)
                    i = i - 1
                    state = -1
                }
                27 -> if (Util.isDateOperator(c)) {
                    delimiterStack.push(c)
                    state = 28
                } else if (Util.isNumber(c)) {//it was a number, most probably telephone number
                    if (map.type == KYugaConstants.TY_DTE) {
                        map.setType(KYugaConstants.TY_NUM, KYugaConstants.TY_NUM)
                    }
                    map.append(c)
                    if ((delimiterStack.pop().toInt() == KYugaConstants.CH_SLSH || delimiterStack.pop().toInt() == KYugaConstants.CH_HYPH) && i + 1 < str.length && Util.isNumber(
                            str[i + 1]
                        ) && (i + 2 == str.length || Util.isDelimiter(str[i + 2]))
                    ) {//flight time 0820/0950
                        map.setType(KYugaConstants.TY_TMS, KYugaConstants.TY_TMS)
                        map.append(str[i + 1])
                        i = i + 1
                        state = -1
                    } else if (delimiterStack.pop().toInt() == KYugaConstants.CH_SPACE) {
                        state = 41
                    } else
                        state = 12
                } else if (c.toInt() == 42 || c.toInt() == 88 || c.toInt() == 120) {//*Xx
                    map.setType(KYugaConstants.TY_ACC, KYugaConstants.TY_ACC)
                    map.append('X')
                    state = 11
                } else {
                    map.setType(KYugaConstants.TY_STR, KYugaConstants.TY_STR)
                    i = i - 1
                    state = -1
                }
                28 -> if (Util.isNumber(c)) {
                    map.put(KYugaConstants.DT_D, c)
                    state = 29
                } else {
                    map.setType(KYugaConstants.TY_STR, KYugaConstants.TY_STR)
                    i = i - 2
                    state = -1
                }
                29 -> {
                    if (Util.isNumber(c)) {
                        map.append(c)
                    } else
                        i = i - 1
                    state = -1
                }
                30 -> if (c.toInt() == KYugaConstants.CH_COMA || c.toInt() == KYugaConstants.CH_SPACE)
                    state = 30
                else if (Util.isNumber(c)) {
                    map.put(KYugaConstants.DT_D, c)
                    state = 31
                } else {
                    map.type = KYugaConstants.TY_DTE
                    i = i - 1
                    state = -1
                }
                31 -> if (Util.isNumber(c)) {
                    map.append(c)
                    state = 32
                } else if (checkMonthType(str, i)?.let {
                        map.put(KYugaConstants.DT_MMM, it.b)
                        i += it.a
                        true
                    } == true) {
                    state = 24
                } else if (c.toInt() == KYugaConstants.CH_COMA || c.toInt() == KYugaConstants.CH_SPACE)
                    state = 32
                else {
                    i = i - 1
                    state = -1
                }
                32 -> if (checkMonthType(str, i)?.let {
                    map.put(KYugaConstants.DT_MMM, it.b)
                    i += it.a
                    true
                } == true) {
                    state = 24
                } else if (c.toInt() == KYugaConstants.CH_COMA || c.toInt() == KYugaConstants.CH_SPACE)
                    state = 32
                else if (Util.checkTypes(root, "FSA_DAYSFFX", str.substring(i))?.let {
                        i += it.a
                        true
                    } == true) {
                    state = 32
                } else {
                    var j = i
                    while (!Util.isNumber(str[j]))
                        j--
                    i = j
                    state = -1
                }
                33 -> if (Util.isNumber(c)) {
                    map.put(KYugaConstants.DT_D, c)
                    state = 34
                } else if (c.toInt() == KYugaConstants.CH_SPACE || c.toInt() == KYugaConstants.CH_COMA || c.toInt() == KYugaConstants.CH_HYPH)
                    state = 33
                else {
                    map.type = KYugaConstants.TY_DTE
                    i -= 1
                    state = -1
                }
                34 -> if (Util.isNumber(c)) {
                    map.append(c)
                    state = 35
                } else if (c.toInt() == KYugaConstants.CH_SPACE || c.toInt() == KYugaConstants.CH_COMA)
                    state = 35
                else {
                    map.type = KYugaConstants.TY_DTE
                    i -= 1
                    state = -1
                }
                35 -> if (Util.isNumber(c)) {
                    if (i > 1 && Util.isNumber(str[i - 1])) {
                        map.convert(KYugaConstants.DT_D, KYugaConstants.DT_YYYY)
                        map.append(c)
                    } else
                        map.put(KYugaConstants.DT_YY, c)
                    state = 20
                } else if (c.toInt() == KYugaConstants.CH_SPACE || c.toInt() == KYugaConstants.CH_COMA)
                    state = 40
                else {
                    map.type = KYugaConstants.TY_DTE
                    i -= 1
                    state = -1
                }
                36 -> if (Util.isNumber(c)) {
                    map.append(c)
                    counter++
                } else if (c.toInt() == KYugaConstants.CH_FSTP && i + 1 < str.length && Util.isNumber(str[i + 1])) {
                    map.append(c)
                    state = 10
                } else if (c.toInt() == KYugaConstants.CH_HYPH && i + 1 < str.length && Util.isNumber(str[i + 1])) {
                    delimiterStack.push(c)
                    map.append(c)
                    state = 16
                } else {
                    if (counter == 12 || Util.isNumber(str.substring(1, i)))
                        map.setType(KYugaConstants.TY_NUM, KYugaConstants.TY_NUM)
                    else
                        return null
                    state = -1
                }
                37 -> if (Util.isNumber(c)) {
                    map.setType(KYugaConstants.TY_AMT, KYugaConstants.TY_AMT)
                    map.put(KYugaConstants.TY_AMT, '-')
                    map.append(c)
                    state = 12
                } else if (c.toInt() == KYugaConstants.CH_FSTP) {
                    map.put(KYugaConstants.TY_AMT, '-')
                    map.append(c)
                    state = 10
                } else
                    state = -1
                38 -> {
                    i = map.index!!
                    state = -1
                }
                39//instrno
                -> if (Util.isNumber(c))
                    map.append(c)
                else {
                    map.setType(KYugaConstants.TY_ACC, KYugaConstants.TY_ACC)
                    state = -1
                }
                40 -> if (Util.isNumber(c)) {
                    map.put(KYugaConstants.DT_YY, c)
                    state = 20
                } else if (c.toInt() == KYugaConstants.CH_SPACE || c.toInt() == KYugaConstants.CH_COMA)
                    state = 40
                else {
                    map.type = KYugaConstants.TY_DTE
                    i -= 1
                    state = -1
                }
                41//for phone numbers; same as 12 + space; coming from 27
                -> when {
                    Util.isNumber(c) -> {
                        map.append(c)
                    }
                    c.toInt() == KYugaConstants.CH_SPACE -> state = 41
                    else -> {
                        i = if (i - 1 > 0 && str[i - 1].toInt() == KYugaConstants.CH_SPACE)
                            i - 2
                        else
                            i - 1
                        state = -1
                    }
                }
                42 //18=12 case, where 7-2209 was becoming amt as part of phn support
                -> if (Util.isNumber(c)) {
                    map.append(c)
                } else if (c.toInt() == KYugaConstants.CH_HYPH && i + 1 < str.length && Util.isNumber(str[i + 1])) {
                    state = 39
                } else {
                    i -= 1
                    state = -1
                }
                43 //1234567890@ybl
                -> if (Util.isLowerAlpha(c) || Util.isNumber(c)) {
                    map.setType(KYugaConstants.TY_VPD, KYugaConstants.TY_VPD)
                    map.append(delimiterStack.pop())
                    map.append(c)
                    state = 44
                } else {
                    state = -1
                }
                44 -> if (Util.isLowerAlpha(c) || Util.isNumber(c) || c.toInt() == KYugaConstants.CH_FSTP) {
                    map.append(c)
                    state = 44
                } else
                    state = -1
                45 -> if (Util.isNumber(c)) {
                    map.append(c)
                } else if (c.toInt() == KYugaConstants.CH_HYPH && i + 1 < str.length && Util.isNumber(str[i + 1])) {
                    state = 39
                } else {
                    i -= if (i - 1 > 0 && str[i - 1].toInt() == KYugaConstants.CH_COMA)
                        2
                    else
                        1
                    state = -1
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
                map.setType(KYugaConstants.TY_NUM, KYugaConstants.TY_NUM)
            else
                return null
        }

        if (map.type == KYugaConstants.TY_AMT) {
            if (!map.contains(map.type!!) || map[map.type!!]!!.contains(".") && map[map.type!!]!!.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].length > 8 || !map[map.type!!]!!.contains(
                    "."
                ) && map[map.type!!]!!.length > 8
            )
                map.setType(KYugaConstants.TY_NUM, KYugaConstants.TY_NUM)
        }

        if (map.type == KYugaConstants.TY_NUM) {
            if (i < str.length && str[i].isAlpha() && !config.containsKey(KYugaConstants.YUGA_SOURCE_CONTEXT)) {
                var j = i
                while (j < str.length && str[j] != ' ')
                    j++
                map.setType(KYugaConstants.TY_STR, KYugaConstants.TY_STR)
                i = j
            } else if (map[KYugaConstants.TY_NUM] != null) {
                if (map[KYugaConstants.TY_NUM]!!.length == 10 && (map[KYugaConstants.TY_NUM]!![0] == '9' || map[KYugaConstants.TY_NUM]!![0] == '8' || map[KYugaConstants.TY_NUM]!![0] == '7'))
                    map.setVal("num_class", KYugaConstants.TY_PHN)
                else if (map[KYugaConstants.TY_NUM]!!.length == 12 && map[KYugaConstants.TY_NUM]!!.startsWith("91"))
                    map.setVal("num_class", KYugaConstants.TY_PHN)
                else if (map[KYugaConstants.TY_NUM]!!.length == 11 && map[KYugaConstants.TY_NUM]!!.startsWith("18"))
                    map.setVal("num_class", KYugaConstants.TY_PHN)
                else if (map[KYugaConstants.TY_NUM]!!.length == 11 && map[KYugaConstants.TY_NUM]!![0] == '0')
                    map.setVal("num_class", KYugaConstants.TY_PHN)
                else
                    map.setVal("num_class", KYugaConstants.TY_NUM)
            }
        } else if (map.type == KYugaConstants.TY_DTE && i + 1 < str.length) {
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
                    if (kl != null && kl.b.type == KYugaConstants.TY_DTE) {
                        map.putAll(kl.b)
                        i = `in` + kl.a
                    }
                } else if (pFSATimePrex != null) {
                    val iTime = `in` + pFSATimePrex.a + 1 + skip(str.substring(`in` + pFSATimePrex.a + 1))
                    if (iTime < str.length && (Util.isNumber(str[iTime]) || Util.checkTypes(
                            root,
                            "FSA_DAYS",
                            str.substring(iTime)
                        ) != null)
                    ) {
                        val p_ = parseInternal(str.substring(iTime), config)
                        if (p_ != null && p_.b.type == KYugaConstants.TY_DTE) {
                            map.putAll(p_.b)
                            i = iTime + p_.a
                        }
                    }
                } else if (pFSATz != null) {
                    val j = skipForTZ(str.substring(`in` + pFSATz.a + 1), map)
                    i = `in` + pFSATz.a + 1 + j
                } else if (sub.toLowerCase().startsWith("pm") || sub.toLowerCase().startsWith("am")) {
                    //todo handle appropriately for pm
                    i = `in` + 2
                }
            }
        } else if (map.type == KYugaConstants.TY_TMS) {
            val v = map[map.type!!]
            if (v != null && v.length == 8 && Util.isHour(v[0], v[1]) && Util.isHour(v[4], v[5])) {
                extractTime(v.substring(0, 4), map.getValMap(), "dept")
                extractTime(v.substring(4, 8), map.getValMap(), "arrv")
            }
        }
        return Pair(i, map)
    }

    private fun checkMonthType(
        str: String,
        i: Int
    ) = Util.checkTypes(root, "FSA_MONTHS", str.substring(i))

    private fun skipForTZ(str: String, map: FsaContextMap): Int {
        var state = 1
        var i = 0
        var c: Char
        while (state > 0 && i < str.length) {
            c = str[i]
            when (state) {
                1 -> if (c.toInt() == KYugaConstants.CH_SPACE || c.toInt() == KYugaConstants.CH_PLUS || Util.isNumber(c))
                    state = 1
                else if (c.toInt() == KYugaConstants.CH_COLN)
                    state = 2
                else {
                    val s_ = str.substring(0, i).trim { it <= ' ' }
                    if (s_.length == 4 && Util.isNumber(s_)) {//we captured a year after IST Mon Sep 04 13:47:13 IST 2017
                        map.put(KYugaConstants.DT_YYYY, s_)
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
                4 -> state = if (c.toInt() == KYugaConstants.CH_SPACE)
                    5
                else
                    -2
                5 -> {
                    val sy = str.substring(i, i + 4)
                    if (i + 3 < str.length && Util.isNumber(sy)) {
                        map.put(KYugaConstants.DT_YYYY, sy)
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
            map.put(KYugaConstants.DT_YYYY, s_)
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

    private fun nextSpace(str: String): Int {
        var i = 0
        while (i < str.length) {
            if (str[i] == ' ')
                return i
            else
                i++
        }
        return i
    }

    private fun accAmtNumPct(str: String, i: Int, map: FsaContextMap, config: Map<String, String>): Int {
        //acc num amt pct
        val c = str[i]
        val subStr = str.substring(i)

        val pFSAAmt =  Util.checkTypes(root, "FSA_AMT", subStr)
        val pFSATimes = Util.checkTypes(root, "FSA_TIMES", subStr)

        if (c.toInt() == KYugaConstants.CH_FSTP) { //dot
            if (i == 0 && config.containsKey(KYugaConstants.YUGA_SOURCE_CONTEXT) && config[KYugaConstants.YUGA_SOURCE_CONTEXT] == KYugaConstants.YUGA_SC_CURR)
                map.setType(KYugaConstants.TY_AMT, KYugaConstants.TY_AMT)
            map.append(c)
            return 10
        } else if (c.toInt() == 42 || c.toInt() == 88 || c.toInt() == 120) {//*Xx
            map.setType(KYugaConstants.TY_ACC, KYugaConstants.TY_ACC)
            map.append('X')
            return 11
        } else if (c.toInt() == KYugaConstants.CH_COMA) { //comma
            return 12
        } else if (c.toInt() == KYugaConstants.CH_PCT || c.toInt() == KYugaConstants.CH_SPACE && i + 1 < str.length && str[i + 1].toInt() == KYugaConstants.CH_PCT) { //pct
            map.setType(KYugaConstants.TY_PCT, KYugaConstants.TY_PCT)
            return -1
        } else if (c.toInt() == KYugaConstants.CH_PLUS) {
            if (config.containsKey(KYugaConstants.YUGA_SOURCE_CONTEXT) && config[KYugaConstants.YUGA_SOURCE_CONTEXT] == KYugaConstants.YUGA_SC_CURR) {
                return -1
            }
            map.setType(KYugaConstants.TY_STR, KYugaConstants.TY_STR)
            return 36
        } else if (i > 0 && pFSAAmt != null) {
            map.index = pFSAAmt.a
            map.setType(KYugaConstants.TY_AMT, KYugaConstants.TY_AMT)
            map.append(getAmt(pFSAAmt.b))
            return 38
        } else if (i > 0 && pFSATimes != null) {
            val ind = i + pFSATimes.a
            map.index = ind
            map.setType(KYugaConstants.TY_TME, null)
            var s = str.substring(0, i)
            if (pFSATimes.b == "mins")
                s = "00$s"
            extractTime(s, map.getValMap())
            return 38
        } else
            return -1
    }

    private fun getAmt(type: String): String {
        when (type) {
            "lakh", "lac" -> return "00000"
            "k" -> return "000"
            else -> return ""
        }
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
                gps[1]?.value.toString() + if (it.groups.size > 1 && gps[2] != null) ":" + gps[2]?.value.toString()  else ":00"
        }
    }

    private fun lookAheadForInstr(str: String, index: Int): Int {
        var c: Char
        for (i in index until str.length) {
            c = str[i]
            if (c.toInt() == KYugaConstants.CH_FSTP) {
            } else return if (c.toInt() == 42 || c.toInt() == 88 || c.toInt() == 120 || Util.isNumber(c))
                i
            else
                -1
        }
        return -1
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