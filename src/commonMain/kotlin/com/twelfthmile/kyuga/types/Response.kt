package com.twelfthmile.kyuga.types

import com.twelfthmile.kyuga.expectations.MultDate
import com.twelfthmile.kyuga.expectations.formatDateDefault

data class Response(val type: String, val valMap: Map<String, String>, var str: String?, val index: Int) {

    private var multDate: MultDate? = null

    constructor(type: String, valMap: Map<String, String>, multDate: MultDate?, index: Int):  this(type, valMap, "", index) {
        this.multDate = multDate
        this.str = this.multDate?.let {  formatDateDefault(it) }
    }

    @Deprecated("")
    fun print(): String {
        return "{\"type\":\"" + type + "\", \"str\":\"" + str + "\", \"index\":\"" + index + "\", \"valMap\":" + printValMap() + "}"
    }

    private fun printValMap(): String =
        valMap.map { (key, value) -> "$key : $value" }.joinToString(separator = ",", prefix = "{", postfix = "}")

}
