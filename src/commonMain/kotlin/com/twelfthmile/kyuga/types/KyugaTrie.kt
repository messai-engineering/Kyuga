package com.twelfthmile.kyuga.types

import com.twelfthmile.kyuga.utils.FSA_TYPES

class KyugaTrie {

    val root: RootTrie

    init {
        val mutableRoot = MutableRootTrie()
        FSA_TYPES.forEach {
            mutableRoot.next[it.first] = MutableGenTrie()
        }
        FSA_TYPES.forEach {
            mutableRoot.next[it.first]?.let { node -> seed(it.second, node) }
        }
        root = mutableRoot.toRootTrie()
    }

    private fun seed(type: String, mutableTrie: MutableGenTrie) {
        var t: MutableGenTrie
        val tokens = type.split(",")
        tokens.forEach {
            var i = 0
            t = mutableTrie
            while (i < it.length) {
                val char = it[i]
                t.child = true
                if (t.next.containsKey(char).not())
                    t.next[char] = MutableGenTrie()
                t = t.next[char] ?: throw IllegalStateException()
                if (i == it.length - 1) {
                    t.leaf = true
                    t.token = it.replace(";", "")
                } else if (i < it.length - 1 && it[i + 1].toInt() == 59) {
                    t.leaf = true
                    t.token = it.replace(";", "")
                    i++
                }
                i++
            }
        }
    }
}