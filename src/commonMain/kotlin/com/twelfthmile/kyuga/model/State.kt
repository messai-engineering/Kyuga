package com.twelfthmile.kyuga.model

import com.twelfthmile.kyuga.Kyuga
import com.twelfthmile.kyuga.types.RootTrie
import com.twelfthmile.kyuga.utils.FsaContextMap

internal data class StateContext(
    val root: RootTrie,
    val str: String,
    val nextChar: Char,
    val contextMap: FsaContextMap,
    val index: Int,
    val delimiterStack: Kyuga.DelimiterStack,
    val config: Map<String, String>,
    val counter: Int
)

internal data class StateResult(val state: Int, val index: Int, val counter: Int)