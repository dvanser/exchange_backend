package com.exchange

import com.exchange.configuration.AppConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication


@SpringBootApplication
@EnableConfigurationProperties(AppConfig::class)
class ExchangeApplication

fun main(args: Array<String>) {
    runApplication<ExchangeApplication>(*args)
}