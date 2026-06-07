package dem.alena.countries.data.repository

object OfflineSyncPolicy {

    const val ALL_COUNTRIES_LIST_KEY = "__all__"

    fun listKeyForQuery(query: String): String {
        val trimmed = query.trim()
        return if (trimmed.isEmpty()) ALL_COUNTRIES_LIST_KEY else trimmed.lowercase()
    }

    fun isCacheFresh(cachedAt: Long?, ttlHours: Int, nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (cachedAt == null) return false
        val ttlMillis = ttlHours.coerceAtLeast(1) * 60L * 60L * 1000L
        return nowMillis - cachedAt < ttlMillis
    }
}
