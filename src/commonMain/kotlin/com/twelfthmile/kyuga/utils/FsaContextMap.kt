package com.twelfthmile.kyuga.utils

import com.twelfthmile.kyuga.expectations.*
import com.twelfthmile.kyuga.utils.*

class FsaContextMap {

    //todo change to private
    private var map: MutableMap<String, String> = mutableMapOf()
    private val valMap = mutableMapOf<String, String>()
    private var prevKey: String? = null
    private var keys: MutableList<String?> = mutableListOf()

    var type: String?
        get() = map[TY_TYP]
        set(type) {
            type?.let {  map[TY_TYP] = it }
        }

    var index: Int?
        get() = map[INDEX]?.toInt()
        set(index) {
            map[INDEX] = index.toString()
        }

    operator fun contains(key: String): Boolean {
        return map.containsKey(key)
    }

    fun size(): Int {
        return map.keys.size
    }

    //normal put method
    fun put(key: String, value: Char) {
        if (!keys.contains(key))
            keys.add(key)
        map[key] = value.toString()
        prevKey = key
    }

    fun put(key: String, value: Int) {
        if (!keys.contains(key))
            keys.add(key)
        map[key] = value.toString()
        prevKey = key
    }

    fun put(key: String?, value: String) {
        if (!keys.contains(key))
            keys.add(key)
        key?.let { map[key] = value }
        prevKey = key
    }

    fun setType(type: String, convertType: String?) {
        map[TY_TYP] = type
        if (convertType != null)
            convert(convertType)
    }

    fun getVal(name: String): String? {
        return valMap[name]
    }

    fun setVal(name: String, `val`: String) {
        valMap[name] = `val`
    }

    fun getValMap(): MutableMap<String, String> {
        return valMap
    }

    //appending to prev value
    fun append(value: Char) {
        val preVal = map[prevKey]
        put(prevKey, preVal + value)
    }

    fun append(value: String) {
        val preVal = map[prevKey]
        put(prevKey, preVal + value)
    }

    //removing last appended value
    fun pop() {
        val preVal = map[prevKey]
        preVal?.let { put(prevKey, it.substring(0, it.length - 1)) }
    }

    fun convert(kOld: String, kNew: String) {
        if (map.containsKey(kOld)) {
            if (!map.containsKey(kNew))
                map.remove(kOld)?.let { put(kNew, it) }
            else
                put(kNew, map[kNew] + map.remove(kOld))
            prevKey = kNew
        }
    }

    private fun convert(k: String) {
        val sb = StringBuilder()
        for (key in keys) {
            sb.append(map.remove(key))
        }
        keys = ArrayList()
        put(k, sb.toString())
    }

    fun remove(key: String) {
        map.remove(key)
    }

    //upgrade for eg from yy to yyy
    fun upgrade(value: Char) {
        when (prevKey) {
            DT_HH -> {
                put(DT_mm, value)
                prevKey = DT_mm
            }
            DT_mm -> {
                put(DT_ss, value)
                prevKey = DT_ss
            }
            DT_D -> {
                put(DT_MM, value)
                prevKey = DT_MM
            }
            DT_MM, DT_MMM -> {
                put(DT_YY, value)
                prevKey = DT_YY
            }
            DT_YY -> {
                put(DT_YYYY, map.remove(DT_YY) + value)
                prevKey = DT_YYYY
            }
        }
    }

    operator fun get(key: String): String? {
        return map[key]
    }

    fun print(vararg str: String): String {
        return if (str.isNotEmpty())
            str[0] + " " + map.toString()
        else
            map.toString()
    }

    fun putAll(fsaContextMap: FsaContextMap) {
//        map.putAll(fsaContextMap.map)
        fsaContextMap.map.forEach {
           map[it.key] = it.value
        }
    }

    fun getDate(config: Map<String, String>): MultDate? {
        val sbf = StringBuilder()
        val sbs = StringBuilder()
        var key: String
        var ifYear = false
        var ifMonth = false
        var ifDay = false
        val invalidDateContributors = ArrayList<String>()
        //when year is not provided then we assume message year; we check for that when we have both day and month
        try {
            for ((key1, value) in map) {
                key = key1
                if (allow(key)) {
                    sbf.append(key).append(" ")
                    sbs.append(value).append(" ")
                    when (key) {
                        DT_YY, DT_YYYY -> ifYear = true
                        DT_D -> ifDay = true
                        DT_MM, DT_MMM -> ifMonth = true
                    }
                }
            }
            //date year defaulting
            if (!ifYear && config.containsKey(YUGA_CONF_DATE)) {
                sbf.append("yyyy ")
                config[YUGA_CONF_DATE]?.let {
                    sbs.append(it.split("-".toRegex()).dropLastWhile { dp -> dp.isEmpty() }.toTypedArray()[0])
                        .append(" ")//assuming yyyy-MM-dd HH:mm:ss format
                }
            } else {
                val maxDate = getMaxDateYear()
                if (map.containsKey(DT_YY)) {
                    val y = map[DT_YY]?.toInt()
                    y?.let {
                        if (!(it > 0 && it < maxDate % 1000 + 3))
                            invalidDateContributors.add(DT_YY)
                    }
                } else {
                    val y = map[DT_YYYY]?.toInt()
                    y?.let {
                        if (!(it > 1971 && it < maxDate + 3))
                            invalidDateContributors.add(DT_YYYY)
                    }
                }

            }

            if (!ifMonth && config.containsKey(YUGA_CONF_DATE)) {
                sbf.append("MM ")
                config[YUGA_CONF_DATE]?.let {
                    sbs.append(it.split("-".toRegex()).dropLastWhile { dp -> dp.isEmpty() }.toTypedArray()[1])
                        .append(" ")//assuming yyyy-MM-dd HH:mm:ss format
                }
            } else {
                if (map.containsKey(DT_MM)) {
                    val m = map[DT_MM]?.toInt()
                    m?.let {
                        if (it !in 0..12)
                            invalidDateContributors.add(DT_MM)
                    }
                }
            }

            if (!ifDay && config.containsKey(YUGA_CONF_DATE)) {
                sbf.append("dd ")
                config[YUGA_CONF_DATE]?.let {
                    sbs.append(
                        it.split("-".toRegex()).dropLastWhile { dp -> dp.isEmpty() }.toTypedArray()[2].split(
                            " ".toRegex()
                        ).dropLastWhile { dp2 -> dp2.isEmpty() }.toTypedArray()[0]
                    )
                }?.append(" ")//assuming yyyy-MM-dd HH:mm:ss format
            } else {
                if (map.containsKey(DT_D)) {
                    map[DT_D]?.toInt()?.let {
                        if (it !in 0..31)
                            invalidDateContributors.add(DT_D)
                    }
                }
            }

            if (invalidDateContributors.size > 0) {
                return if (invalidDateContributors.size == 1 && invalidDateContributors[0] == DT_MM && ifDay && ifYear) {
                    val format = DT_D + "/" + DT_MM + "/" + if (map.containsKey(DT_YY)) DT_YY else DT_YYYY
                    val value = map[DT_MM] + "/" + map[DT_D] + "/" + if (map.containsKey(
                            DT_YY
                        )
                    ) map[DT_YY] else map[DT_YYYY]
                    formatDateByFormat(value, format)
                } else
                    null
            }
            return formatForEnglishLocale(sbs.toString(), sbf.toString())
        } catch (e: Exception) {
            //swallow
            return null
        }

    }

    private fun allow(key: String): Boolean {
        return key == DT_D || key == DT_MM || key == DT_MMM || key == DT_YY || key == DT_YYYY || key == DT_HH || key == DT_mm || key == DT_ss
    }

}