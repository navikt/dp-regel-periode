package no.nav.dagpenger.regel.periode

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.booleanType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.streams.KafkaCredential
import no.nav.dagpenger.streams.Topic
import no.nav.dagpenger.streams.Topics

private const val TOPIC = "privat-dagpenger-behov-v2"

private val localProperties = ConfigurationMap(
    mapOf(
        "kafka.bootstrap.servers" to "localhost:9092",
        "KAFKA_BROKERS" to "localhost:9092",
        "kafka.topic" to TOPIC,
        "kafka.reset.policy" to "earliest",
        "application.profile" to Profile.LOCAL.toString(),
        "nav.truststore.path" to "",
        "nav.truststore.password" to "changeme",
        "behov.topic" to Topics.DAGPENGER_BEHOV_PACKET_EVENT.name,
        "inntekt.grpc.address" to "localhost",
        "inntekt.grpc.api.key" to "apikey",
        "inntekt.grpc.api.secret" to "secret"
    )
)
private val devProperties = ConfigurationMap(
    mapOf(
        "kafka.bootstrap.servers" to "b27apvl00045.preprod.local:8443,b27apvl00046.preprod.local:8443,b27apvl00047.preprod.local:8443",
        "kafka.topic" to TOPIC,
        "kafka.reset.policy" to "earliest",
        "application.profile" to Profile.DEV.toString(),
        "feature.gjustering" to false.toString(),
        "behov.topic" to Topics.DAGPENGER_BEHOV_PACKET_EVENT.name,
        "inntekt.grpc.address" to "dp-inntekt-api-grpc.teamdagpenger.svc.nais.local"
    )
)
private val prodProperties = ConfigurationMap(
    mapOf(
        "kafka.bootstrap.servers" to "a01apvl00145.adeo.no:8443,a01apvl00146.adeo.no:8443,a01apvl00147.adeo.no:8443,a01apvl00148.adeo.no:8443,a01apvl00149.adeo.no:8443,a01apvl00150.adeo.no:8443",
        "kafka.topic" to TOPIC,
        "kafka.reset.policy" to "earliest",
        "application.profile" to Profile.PROD.toString(),
        "behov.topic" to Topics.DAGPENGER_BEHOV_PACKET_EVENT.name,
        "inntekt.grpc.address" to "dp-inntekt-api-grpc.teamdagpenger.svc.nais.local"
    )
)

private fun config() = when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
    "dev-fss" -> systemProperties() overriding EnvironmentVariables overriding devProperties
    "prod-fss" -> systemProperties() overriding EnvironmentVariables overriding prodProperties
    else -> {
        systemProperties() overriding EnvironmentVariables overriding localProperties
    }
}

data class Configuration(
    val kafka: Kafka = Kafka(),
    val application: Application = Application(),
    val features: Features = Features(),
    val behovTopic: Topic<String, Packet> = Topics.DAGPENGER_BEHOV_PACKET_EVENT.copy(
        name = config()[Key("behov.topic", stringType)]
    ),
    val regelTopic: Topic<String, Packet> = behovTopic.copy("teamdagpenger.regel.v1")
) {
    data class Kafka(
        val brokers: String = config()[Key("kafka.bootstrap.servers", stringType)],
        val aivenBrokers: String = config()[Key("KAFKA_BROKERS", stringType)],
        val user: String? = config().getOrNull(Key("srvdp.regel.periode.username", stringType)),
        val password: String? = config().getOrNull(Key("srvdp.regel.periode.password", stringType))
    ) {
        fun credential(): KafkaCredential? {
            return if (user != null && password != null) {
                KafkaCredential(user, password)
            } else null
        }
    }

    data class Application(
        val id: String = config().getOrElse(Key("application.id", stringType), "dagpenger-regel-periode"),
        val profile: Profile = config()[Key("application.profile", stringType)].let { Profile.valueOf(it) },
        val httpPort: Int = 8080,
        val inntektgrpcAddress: String = config()[Key("inntekt.grpc.address", stringType)],
        val inntektgrpcApiKey: String = config()[Key("inntekt.grpc.api.key", stringType)],
        val inntektgrpcApiSecret: String = config()[Key("inntekt.grpc.api.secret", stringType)]
    )

    class Features {
        fun gjustering() = config().getOrElse(Key("feature.gjustering", booleanType), false)
    }
}

enum class Profile {
    LOCAL, DEV, PROD
}
