package no.nav.dagpenger.regel.periode

import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.json.JSONObject
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Properties
import kotlin.test.assertTrue

class PeriodeTopologyTest {

    companion object {
        val factory = ConsumerRecordFactory<String, JSONObject>(
                dagpengerBehovTopic.name,
                dagpengerBehovTopic.keySerde.serializer(),
                dagpengerBehovTopic.valueSerde.serializer()
        )

        val config = Properties().apply {
            this[StreamsConfig.APPLICATION_ID_CONFIG] = "test"
            this[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "dummy:1234"
        }
    }

    @Test
    fun ` Should add inntekt task to subsumsjonsBehov without inntekt `() {
        val datalaster = Periode(
                Environment(
                        username = "bogus",
                        password = "bogus"
                )
        )

        val behov = JSONObject()
                .put("vedtaksId", "123456")
                .put("aktorId", 123)
                .put("beregningsDato", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .put("tasks", listOf("othertask"))

        TopologyTestDriver(datalaster.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(behov)
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                    dagpengerBehovTopic.name,
                    dagpengerBehovTopic.keySerde.deserializer(),
                    dagpengerBehovTopic.valueSerde.deserializer()
            )

            assertTrue("Inntekt task should have been added") { "hentInntekt" in ut.value().getJSONArray("tasks") }
            assertTrue("Other task should be preserved") { "othertask" in ut.value().getJSONArray("tasks") }
        }
    }

    @Test
    fun ` Should add PeriodeSubsumsjon to subsumsjonsBehov with inntekt `() {
        val datalaster = Periode(
                Environment(
                        username = "bogus",
                        password = "bogus"
                )
        )

        val behov = JSONObject()
                .put("vedtaksId", "123456")
                .put("aktorId", 123)
                .put("beregningsDato", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .put("inntekt", 0)

        TopologyTestDriver(datalaster.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(behov)
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                    dagpengerBehovTopic.name,
                    dagpengerBehovTopic.keySerde.deserializer(),
                    dagpengerBehovTopic.valueSerde.deserializer()
            )

            assertTrue("MinsteinntektSubsumsjon should have been added") {
                ut.value().get("periodeSubsumsjon") != null
            }
        }
    }
}
