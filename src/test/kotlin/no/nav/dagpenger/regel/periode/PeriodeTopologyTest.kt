package no.nav.dagpenger.regel.periode

import no.nav.dagpenger.streams.Topics
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.json.JSONObject
import org.junit.jupiter.api.Test
import java.util.Properties
import kotlin.test.assertTrue

class PeriodeTopologyTest {

    companion object {
        val factory = ConsumerRecordFactory<String, String>(
            Topics.DAGPENGER_BEHOV_EVENT.name,
            Serdes.String().serializer(),
            Serdes.String().serializer()
        )

        val config = Properties().apply {
            this[StreamsConfig.APPLICATION_ID_CONFIG] = "test"
            this[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "dummy:1234"
        }
    }

    @Test
    fun ` Should add inntekt task to subsumsjonsBehov without inntekt `() {
        val periode = Periode(
                Environment(
                        username = "bogus",
                        password = "bogus"
                )
        )

        val behov = SubsumsjonsBehov.Builder()
            .build()

        TopologyTestDriver(periode.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(behov.jsonObject.toString())
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                Topics.DAGPENGER_BEHOV_EVENT.name,
                Serdes.String().deserializer(),
                Serdes.String().deserializer()
            )

            val utBehov = SubsumsjonsBehov(JSONObject(ut.value()))

            assertTrue { utBehov.hasTasks() }
            assertTrue { utBehov.hasHentInntektTask() }
        }
    }

    @Test
    fun ` Should add PeriodeSubsumsjon to subsumsjonsBehov with inntekt `() {
        val periode = Periode(
                Environment(
                        username = "bogus",
                        password = "bogus"
                )
        )

        val behov = SubsumsjonsBehov.Builder()
            .inntekt(Inntekt("", 0))
            .build()

        TopologyTestDriver(periode.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(behov.jsonObject.toString())
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                Topics.DAGPENGER_BEHOV_EVENT.name,
                Serdes.String().deserializer(),
                Serdes.String().deserializer()
            )

            val utBehov = SubsumsjonsBehov(JSONObject(ut.value()))

            assertTrue { utBehov.hasPeriodeSubsumsjon() }
        }
    }
}