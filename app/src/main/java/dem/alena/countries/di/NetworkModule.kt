package dem.alena.countries.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dem.alena.countries.data.local.FavouritesDao
import dem.alena.countries.data.network.CountriesApi
import dem.alena.countries.data.repository.CountriesRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://restcountries.com/v3.1/"

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideCountriesApi(
        retrofit: Retrofit
    ): CountriesApi = retrofit.create(CountriesApi::class.java)

    @Provides
    @Singleton
    fun provideCountriesRepository(
        api: CountriesApi,
        favouritesDao: FavouritesDao
    ): CountriesRepository = CountriesRepository(api, favouritesDao)
}

