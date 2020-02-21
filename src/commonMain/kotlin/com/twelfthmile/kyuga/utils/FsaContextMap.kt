package com.twelfthmile.kyuga.utils

import com.twelfthmile.kyuga.expectations.MultDate
import com.twelfthmile.kyuga.expectations.formatDateByFormat
import com.twelfthmile.kyuga.expectations.formatForEnglishLocale
import com.twelfthmile.kyuga.expectations.getMaxDateYear

class FsaContextMap {

    //todo change to private
    private val map: MutableMap<String, String> = mutableMapOf()
    private val valMap = mutableMapOf<String, String>()
    private var prevKey: String? = null
    private var keys: MutableList<String?> = mutableListOf()

    var type: String?
        get() = map[KYugaConstants.TY_TYP]
        set(type) {
            type?.let {  map[KYugaConstants.TY_TYP] = it }
        }

    var index: Int?
        get() = map[KYugaConstants.INDEX]?.toInt()
        set(index) {
            map[KYugaConstants.INDEX] = index.toString()
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
        map[KYugaConstants.TY_TYP] = type
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
            KYugaConstants.DT_HH -> {
                put(KYugaConstants.DT_mm, value)
                prevKey = KYugaConstants.DT_mm
            }
            KYugaConstants.DT_mm -> {
                put(KYugaConstants.DT_ss, value)
                prevKey = KYugaConstants.DT_ss
            }
            KYugaConstants.DT_D -> {
                put(KYugaConstants.DT_MM, value)
                prevKey = KYugaConstants.DT_MM
            }
            KYugaConstants.DT_MM, KYugaConstants.DT_MMM -> {
                put(KYugaConstants.DT_YY, value)
                prevKey = KYugaConstants.DT_YY
            }
            KYugaConstants.DT_YY -> {
                put(KYugaConstants.DT_YYYY, map.remove(KYugaConstants.DT_YY) + value)
                prevKey = KYugaConstants.DT_YYYY
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
        map.putAll(fsaContextMap.map)
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
                        KYugaConstants.DT_YY, KYugaConstants.DT_YYYY -> ifYear = true
                        KYugaConstants.DT_D -> ifDay = true
                        KYugaConstants.DT_MM, KYugaConstants.DT_MMM -> ifMonth = true
                    }
                }
            }
            //date year defaulting
            if (!ifYear && config.containsKey(KYugaConstants.YUGA_CONF_DATE)) {
                sbf.append("yyyy ")
                config[KYugaConstants.YUGA_CONF_DATE]?.let {
                    sbs.append(it.split("-".toRegex()).dropLastWhile { dp -> dp.isEmpty() }.toTypedArray()[0])
                        .append(" ")//assuming yyyy-MM-dd HH:mm:ss format
                }
            } else {
                val maxDate = getMaxDateYear()
                if (map.containsKey(KYugaConstants.DT_YY)) {
                    val y = map[KYugaConstants.DT_YY]?.toInt()
                    y?.let {
                        if (!(it > 0 && it < maxDate % 1000 + 3))
                            invalidDateContributors.add(KYugaConstants.DT_YY)
                    }
                } else {
                    val y = map[KYugaConstants.DT_YYYY]?.toInt()
                    y?.let {
                        if (!(it > 1971 && it < maxDate + 3))
                            invalidDateContributors.add(KYugaConstants.DT_YYYY)
                    }
                }

            }

            if (!ifMonth && config.containsKey(KYugaConstants.YUGA_CONF_DATE)) {
                sbf.append("MM ")
                config[KYugaConstants.YUGA_CONF_DATE]?.let {
                    sbs.append(it.split("-".toRegex()).dropLastWhile { dp -> dp.isEmpty() }.toTypedArray()[1])
                        .append(" ")//assuming yyyy-MM-dd HH:mm:ss format
                }
            } else {
                if (map.containsKey(KYugaConstants.DT_MM)) {
                    val m = map[KYugaConstants.DT_MM]?.toInt()
                    m?.let {
                        if (it !in 0..12)
                            invalidDateContributors.add(KYugaConstants.DT_MM)
                    }
                }
            }

            if (!ifDay && config.containsKey(KYugaConstants.YUGA_CONF_DATE)) {
                sbf.append("dd ")
                config[KYugaConstants.YUGA_CONF_DATE]?.let {
                    sbs.append(
                        it.split("-".toRegex()).dropLastWhile { dp -> dp.isEmpty() }.toTypedArray()[2].split(
                            " ".toRegex()
                        ).dropLastWhile { dp2 -> dp2.isEmpty() }.toTypedArray()[0]
                    )
                }?.append(" ")//assuming yyyy-MM-dd HH:mm:ss format
            } else {
                if (map.containsKey(KYugaConstants.DT_D)) {
                    map[KYugaConstants.DT_D]?.toInt()?.let {
                        if (it !in 0..31)
                            invalidDateContributors.add(KYugaConstants.DT_D)
                    }
                }
            }

            if (invalidDateContributors.size > 0) {
                return if (invalidDateContributors.size == 1 && invalidDateContributors[0] == KYugaConstants.DT_MM && ifDay && ifYear) {
                    val format = KYugaConstants.DT_D + "/" + KYugaConstants.DT_MM + "/" + if (map.containsKey(KYugaConstants.DT_YY)) KYugaConstants.DT_YY else KYugaConstants.DT_YYYY
                    val value = map[KYugaConstants.DT_MM] + "/" + map[KYugaConstants.DT_D] + "/" + if (map.containsKey(
                            KYugaConstants.DT_YY
                        )
                    ) map[KYugaConstants.DT_YY] else map[KYugaConstants.DT_YYYY]
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
        return key == KYugaConstants.DT_D || key == KYugaConstants.DT_MM || key == KYugaConstants.DT_MMM || key == KYugaConstants.DT_YY || key == KYugaConstants.DT_YYYY || key == KYugaConstants.DT_HH || key == KYugaConstants.DT_mm || key == KYugaConstants.DT_ss
    }

}