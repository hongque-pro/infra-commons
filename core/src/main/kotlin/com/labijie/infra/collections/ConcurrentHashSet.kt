package com.labijie.infra.collections

import java.util.concurrent.ConcurrentHashMap


/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-24
 */


open class ConcurrentHashSet<E>() : AbstractSet<E>(), Set<E> {

    private val map = ConcurrentHashMap<E, Boolean>()

    private val keys
        get() = map.keys

    override val size: Int
        get() = map.size

    fun add(e: E): Boolean {
        return map.put(e, java.lang.Boolean.TRUE) == null
    }

    fun clear() {
        map.clear()
    }

    override operator fun contains(element: E): Boolean {
        return map.containsKey(element)
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        return keys.containsAll(elements)
    }

    override fun hashCode(): Int {
        return keys.hashCode()
    }

    override fun isEmpty(): Boolean {
        return map.isEmpty()
    }

    override fun iterator(): Iterator<E> {
        return keys.iterator()
    }

    fun remove(o: E): Boolean {
        return map.remove(o) != null
    }

    fun removeAll(c: Collection<E>): Boolean {
        return keys.removeAll(c)
    }

    fun retainAll(c: Collection<E>): Boolean {
        return keys.retainAll(c)
    }


    override fun toString(): String {
        return map.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ConcurrentHashSet<*>

        if (map != other.map) return false

        return true
    }
}