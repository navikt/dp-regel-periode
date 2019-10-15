package no.nav.dagpenger.regel.periode

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.booleanType
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import no.nav.dagpenger.streams.KafkaCredential

private val localProperties = ConfigurationMap(
    mapOf(
        "kafka.bootstrap.servers" to "localhost:9092",
        "application.profile" to Profile.LOCAL.toString(),
        "application.httpPort" to "8096"
    )
)
private val devProperties = ConfigurationMap(
    mapOf(
        "kafka.bootstrap.servers" to "b27apvl00045.preprod.local:8443,b27apvl00046.preprod.local:8443,b27apvl00047.preprod.local:8443",
        "application.profile" to Profile.DEV.toString(),
        "application.httpPort" to "8096",
        "feature.gjustering" to true.toString()
    )
)
private val prodProperties = ConfigurationMap(
    mapOf(
        "kafka.bootstrap.servers" to "a01apvl00145.adeo.no:8443,a01apvl00146.adeo.no:8443,a01apvl00147.adeo.no:8443,a01apvl00148.adeo.no:8443,a01apvl00149.adeo.no:8443,a01apvl00150.adeo.no:8443",
        "application.profile" to Profile.PROD.toString(),
        "application.httpPort" to "8096"
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
    val features: Features = Features()
) {
    data class Kafka(
        val brokers: String = config()[Key("kafka.bootstrap.servers", stringType)],
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
        val profile: Profile = config()[Key("application.profile", stringType)].let { Profile.valueOf(it) },
        val httpPort: Int = config()[Key("application.httpPort", intType)]
    )

    class Features {
        fun gjustering() = config().getOrElse(Key("feature.gjustering", booleanType), false)
    }
}

enum class Profile {
    LOCAL, DEV, PROD
}
