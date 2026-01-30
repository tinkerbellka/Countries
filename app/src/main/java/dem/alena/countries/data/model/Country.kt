package dem.alena.countries.data.model

import com.google.gson.annotations.SerializedName

data class Country(
    @SerializedName("cca3")
    val code: String,

    @SerializedName("name")
    val name: Name,

    @SerializedName("capital")
    val capital: List<String>?,

    @SerializedName("region")
    val region: String,

    @SerializedName("population")
    val population: Long,

    @SerializedName("flags")
    val flags: Flags
)

data class Name(
    @SerializedName("common")
    val common: String
)

data class Flags(
    @SerializedName("png")
    val png: String
)
