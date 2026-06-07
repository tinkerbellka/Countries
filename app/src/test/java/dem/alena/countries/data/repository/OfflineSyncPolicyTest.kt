package dem.alena.countries.data.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineSyncPolicyTest {

    @Test
    fun listKey_blankQuery_returnsAllKey() {
        assertTrue(OfflineSyncPolicy.listKeyForQuery("") == OfflineSyncPolicy.ALL_COUNTRIES_LIST_KEY)
        assertTrue(OfflineSyncPolicy.listKeyForQuery("  ") == OfflineSyncPolicy.ALL_COUNTRIES_LIST_KEY)
    }

    @Test
    fun listKey_searchQuery_isNormalized() {
        assertTrue(OfflineSyncPolicy.listKeyForQuery("  Canada ") == "canada")
    }

    @Test
    fun isCacheFresh_withinTtl_returnsTrue() {
        val now = 1_000_000L
        val cachedAt = now - 60 * 60 * 1000
        assertTrue(OfflineSyncPolicy.isCacheFresh(cachedAt, ttlHours = 24, nowMillis = now))
    }

    @Test
    fun isCacheFresh_afterTtl_returnsFalse() {
        val now = 1_000_000L
        val cachedAt = now - 25 * 60 * 60 * 1000
        assertFalse(OfflineSyncPolicy.isCacheFresh(cachedAt, ttlHours = 24, nowMillis = now))
    }
}
