package com.twelfthmile.kyuga.types

class GenTrie {

    var leaf = false
    var child = false
    val next: MutableMap<Char, GenTrie> = mutableMapOf()
    var token: String? = null

}

class RootTrie {
    val next: MutableMap<String, GenTrie> = mutableMapOf()
}


data class Pair<A, B>(val a: A, val b: B)