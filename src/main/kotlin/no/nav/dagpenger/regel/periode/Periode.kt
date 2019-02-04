package no.nav.dagpenger.regel.periode

import mu.KotlinLogging
import no.nav.dagpenger.streams.KafkaCredential
import no.nav.dagpenger.streams.Service
import no.nav.dagpenger.streams.Topics
import no.nav.dagpenger.streams.kbranch
import no.nav.dagpenger.streams.streamConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import java.util.Properties

private val LOGGER = KotlinLogging.logger {}

class Periode(val env: Environment) : Service() {
    override val SERVICE_APP_ID: String = "dagpenger-regel-periode"
    override val HTTP_PORT: Int = env.httpPort ?: super.HTTP_PORT

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val service = Periode(Environment())
            service.start()
        }
    }

    override fun setupStreams(): KafkaStreams {
        LOGGER.info { "Initiating start of $SERVICE_APP_ID" }
        return KafkaStreams(buildTopology(), getConfig())
    }

    internal fun buildTopology(): Topology {
        val builder = StreamsBuilder()

        val topic = Topics.DAGPENGER_BEHOV_EVENT

        val stream = builder.stream(
            Topics.DAGPENGER_BEHOV_EVENT.name,
            Consumed.with(Serdes.StringSerde(), Serdes.serdeFrom(JsonSerializer(), JsonDeserializer()))
        )

        val (needsInntekt, needsSubsumsjon) = stream
            .peek { key, value -> LOGGER.info("Processing ${value.javaClass} with key $key") }
            .filter { _, behov -> shouldBeProcessed(behov) }
            .kbranch(
                { _, behov: SubsumsjonsBehov -> behov.inntekt == null },
                { _, behov: SubsumsjonsBehov -> behov.inntekt != null })

        needsInntekt.mapValues(this::addInntektTask)
        needsSubsumsjon.mapValues(this::addRegelresultat)

        needsInntekt.merge(needsSubsumsjon)
            .peek { key, value -> LOGGER.info("Producing ${value.javaClass} with key $key") }
            .to(topic.name, Produced.with(Serdes.StringSerde(), Serdes.serdeFrom(JsonSerializer(), JsonDeserializer())))

        return builder.build()
    }

    override fun getConfig(): Properties {
        val props = streamConfig(
            appId = SERVICE_APP_ID,
            bootStapServerUrl = env.bootstrapServersUrl,
            credential = KafkaCredential(env.username, env.password)
        )
        return props
    }

    private fun addInntektTask(behov: SubsumsjonsBehov): SubsumsjonsBehov {
        behov.tasks = listOf("hentInntekt")
        return behov
    }

    private fun addRegelresultat(behov: SubsumsjonsBehov): SubsumsjonsBehov {
        behov.periodeSubsumsjon = PeriodeSubsumsjon(
            "aaa",
            "bbb",
            "Periode.v1",
            0
        )
        return behov
    }
}

fun shouldBeProcessed(behov: SubsumsjonsBehov): Boolean {
    return when {
        needsInntektTask(behov) -> true
        needsMinsteinntektSubsumsjon(behov) -> true
        else -> false
    }
}

fun needsInntektTask(behov: SubsumsjonsBehov): Boolean {
    return behov.inntekt == null && behov.tasks == null
}

fun needsMinsteinntektSubsumsjon(behov: SubsumsjonsBehov): Boolean {
    return behov.inntekt != null && behov.periodeSubsumsjon == null
}

class MinsteinntektRegelException(override val message: String) : RuntimeException(message)
