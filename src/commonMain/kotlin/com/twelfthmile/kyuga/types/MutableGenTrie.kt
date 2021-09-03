package com.twelfthmile.kyuga.types

data class GenTrie(
    val leaf: Boolean = false,
    val child: Boolean = false,
    val next: Map<Char, GenTrie> = mutableMapOf(),
    val token: String? = null
)

open class MutableGenTrie(
    var leaf: Boolean = false,
    var child: Boolean = false,
    val next: MutableMap<Char, MutableGenTrie> = mutableMapOf(),
    var token: String? = null
)

class RootTrie(val next: Map<String, GenTrie>)

class MutableRootTrie {
    val next: MutableMap<String, MutableGenTrie> = mutableMapOf()
}

internal fun MutableGenTrie.toGenTrie(): GenTrie {
    return GenTrie(this.leaf, this.child, this.next.toUnMutableGenTrie(), this.token)
}

internal fun MutableMap<Char, MutableGenTrie>.toUnMutableGenTrie(): Map<Char, GenTrie>{
    return this.map { it.key to it.value.toGenTrie() }.toMap()
}

internal fun MutableMap<String, MutableGenTrie>.toUnMutableRootTrie(): Map<String, GenTrie>{
    return this.map { it.key to it.value.toGenTrie() }.toMap()
}

internal fun MutableRootTrie.toRootTrie() = RootTrie(this.next.toUnMutableRootTrie())