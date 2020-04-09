@file:Suppress("unused")

package coil.request

import coil.decode.Decoder
import coil.fetch.Fetcher
import coil.request.Parameters.Entry
import coil.util.mapNotNullValues

/** A map of generic values that can be used to pass custom data to [Fetcher]s and [Decoder]s. */
class Parameters private constructor(
    private val map: Map<String, Entry>
) : Iterable<Pair<String, Entry>> {

    constructor() : this(emptyMap())

    companion object {
        @JvmField val EMPTY = Parameters()

        /** Create a new [Parameters] instance. */
        @Deprecated(
            message = "Use Parameters.Builder to create new instances.",
            replaceWith = ReplaceWith("Parameters.Builder().apply(builder).build()")
        )
        inline operator fun invoke(
            builder: Builder.() -> Unit = {}
        ): Parameters = Builder().apply(builder).build()
    }

    /** Returns the value associated with [key] or null if [key] has no mapping. */
    fun value(key: String): Any? = map[key]?.value

    /** Returns the cache key associated with [key] or null if [key] has no mapping. */
    fun cacheKey(key: String): String? = map[key]?.cacheKey

    /** Returns the entry associated with [key] or null if [key] has no mapping. */
    fun entry(key: String): Entry? = map[key]

    /** Returns the number of parameters in this object. */
    fun count(): Int = map.count()

    /** Returns true if this object has no parameters. */
    fun isEmpty(): Boolean = map.isEmpty()

    /** Returns a map of keys to values. */
    fun values(): Map<String, Any?> {
        return if (isEmpty()) {
            emptyMap()
        } else {
            map.mapValues { it.value.value }
        }
    }

    /** Returns a map of keys to non null cache keys. Parameters with a null cache key are filtered out. */
    fun cacheKeys(): Map<String, String> {
        return if (isEmpty()) {
            emptyMap()
        } else {
            map.mapNotNullValues { it.value.cacheKey }
        }
    }

    /** Returns an [Iterator] over the entries in the [Parameters]. */
    override operator fun iterator(): Iterator<Pair<String, Entry>> {
        return map.map { (key, value) -> key to value }.iterator()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Parameters) return false
        if (map != other.map) return false
        return true
    }

    override fun hashCode(): Int = map.hashCode()

    fun newBuilder() = Builder(this)

    data class Entry(
        val value: Any?,
        val cacheKey: String?
    )

    class Builder {

        private val map: MutableMap<String, Entry>

        constructor() {
            map = mutableMapOf()
        }

        constructor(parameters: Parameters) {
            map = parameters.map.toMutableMap()
        }

        /**
         * Set a parameter.
         *
         * @param key The parameter's key.
         * @param value The parameter's value.
         * @param cacheKey The parameter's cache key. If not null, this value will be added to a request's cache key.
         */
        @JvmOverloads
        fun set(key: String, value: Any?, cacheKey: String? = value?.toString()) = apply {
            this.map[key] = Entry(value, cacheKey)
        }

        /**
         * Remove a parameter.
         *
         * @param key The parameter's key.
         */
        fun remove(key: String) = apply {
            this.map.remove(key)
        }

        /** Create a new [Parameters] instance. */
        fun build() = Parameters(map.toMap())
    }
}
