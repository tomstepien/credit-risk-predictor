package com.creditrisk.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Bean
    fun mlWebClient(
        webClientBuilder: WebClient.Builder,
        @Value("\${ML_SERVICE_URL:http://localhost:8000}") mlServiceUrl: String
    ): WebClient {
        return webClientBuilder.baseUrl(mlServiceUrl).build()
    }
}