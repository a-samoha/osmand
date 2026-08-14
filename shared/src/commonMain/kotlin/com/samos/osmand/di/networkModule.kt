package com.samos.osmand.di

import com.samos.osmand.data.repository.MapRepositoryImpl
import com.samos.osmand.data.source.OsmandApi
import com.samos.osmand.data.source.createOsmandApi
import com.samos.osmand.domain.repository.MapRepository
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

private const val BASE_URL = "https://download.osmand.net/"

val networkModule = module {

    // HttpClient engine as a singleton inside the graph
    single<HttpClient> {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.BODY
            }
        }
    }

    // Ktorfit client builder, injecting the HttpClient from above using get()
    single<Ktorfit> {
        Ktorfit.Builder()
            .baseUrl(BASE_URL)
            .httpClient(get<HttpClient>())
            .build()
    }

    // Provide the API implementation using the injected Ktorfit instance
    single<OsmandApi> {
        get<Ktorfit>().createOsmandApi()
    }

    singleOf(::MapRepositoryImpl) bind MapRepository::class
}
