package no.nav.dagpenger.regel.sats

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import io.getunleash.DefaultUnleash
import io.getunleash.Unleash
import io.getunleash.util.UnleashConfig
import java.net.InetAddress

object Config {
    private val defaultProperties =
        ConfigurationMap(
            mapOf(
                "RAPID_APP_NAME" to "dagpenger-regel-sats",
                "KAFKA_CONSUMER_GROUP_ID" to "dagpenger-regel-sats-v1",
                "KAFKA_RAPID_TOPIC" to "teamdagpenger.regel.v1",
                "KAFKA_RESET_POLICY" to "latest",
                "UNLEASH_SERVER_API_URL" to "https://localhost:1234",
                "UNLEASH_SERVER_API_TOKEN" to "tøken",
            ),
        )
    private val properties =
        ConfigurationProperties.systemProperties() overriding EnvironmentVariables() overriding defaultProperties

    val config: Map<String, String> =
        properties.list().reversed().fold(emptyMap()) { map, pair ->
            map + pair.second
        }

    val unleash: Unleash by lazy {
        DefaultUnleash(
            UnleashConfig.builder()
                .appName("dp-regel-sats")
                .instanceId(runCatching { InetAddress.getLocalHost().hostName }.getOrElse { "ukjent" })
                .unleashAPI(properties[Key("UNLEASH_SERVER_API_URL", stringType)] + "/api/")
                .apiKey(properties[Key("UNLEASH_SERVER_API_TOKEN", stringType)])
                .environment(
                    when (System.getenv("NAIS_CLUSTER_NAME").orEmpty()) {
                        "prod-gcp" -> "production"
                        else -> "development"
                    },
                ).build(),
        )
    }
}
