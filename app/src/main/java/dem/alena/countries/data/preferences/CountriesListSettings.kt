package dem.alena.countries.data.preferences

enum class CountriesSortOrder {
    NAME,
    POPULATION_DESC
}

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class CountriesListSettings(
    val sortOrder: CountriesSortOrder = CountriesSortOrder.NAME,
    val activeProfileId: Long = 0L,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val cacheTtlHours: Int = 24,
    val wifiOnlyBackgroundSync: Boolean = true
)
