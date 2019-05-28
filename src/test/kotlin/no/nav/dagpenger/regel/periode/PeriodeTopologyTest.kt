package no.nav.dagpenger.regel.periode

import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.streams.Topics.DAGPENGER_BEHOV_PACKET_EVENT
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.net.URI
import java.time.YearMonth
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class PeriodeTopologyTest {

    private val inntekt = Inntekt(
        inntektsId = "12345",
        inntektsListe = listOf(
            KlassifisertInntektMåned(
                årMåned = YearMonth.of(2019, 2),
                klassifiserteInntekter = listOf(
                    KlassifisertInntekt(
                        beløp = BigDecimal(25000),
                        inntektKlasse = InntektKlasse.ARBEIDSINNTEKT
                    )
                )

            )
        ),
        sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 2)
    )

    companion object {
        val factory = ConsumerRecordFactory<String, Packet>(
            DAGPENGER_BEHOV_PACKET_EVENT.name,
            DAGPENGER_BEHOV_PACKET_EVENT.keySerde.serializer(),
            DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.serializer()
        )

        val config = Properties().apply {
            this[StreamsConfig.APPLICATION_ID_CONFIG] = "test"
            this[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "dummy:1234"
        }
    }

    @Test
    fun ` Dagpenger behov without inntekt and "senesteInntektsmåned" should not be processed `() {
        val periode = Periode(
            Environment(
                username = "bogus",
                password = "bogus"
            )
        )
        val emptyjsonBehov = """
            {}
            """.trimIndent()

        TopologyTestDriver(periode.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(Packet(emptyjsonBehov))
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                DAGPENGER_BEHOV_PACKET_EVENT.name,
                DAGPENGER_BEHOV_PACKET_EVENT.keySerde.deserializer(),
                DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.deserializer()
            )

            assertTrue { null == ut }
        }
    }

    @Test
    fun ` Dagpenger behov without grunnlagResultat should not be processed `() {
        val periode = Periode(
            Environment(
                username = "bogus",
                password = "bogus"
            )
        )
        val json = """
            {
            "senesteInntektsmåned":"2019-01"

            }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", inntekt)

        TopologyTestDriver(periode.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(packet)
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                DAGPENGER_BEHOV_PACKET_EVENT.name,
                DAGPENGER_BEHOV_PACKET_EVENT.keySerde.deserializer(),
                DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.deserializer()
            )

            assertTrue { null == ut }
        }
    }

    @Test
    fun ` Should add PeriodeSubsumsjon `() {
        val periode = Periode(
            Environment(
                username = "bogus",
                password = "bogus"
            )
        )

        val json = """
            {
                "behovId":"01D6V5QCJCH0NQCHF4PZYB0NRJ",
                "aktørId":"1000052711564",
                "vedtakId":3.1018297E7,
                "beregningsDato":"2019-02-27",
                "harAvtjentVerneplikt":false,
                "senesteInntektsmåned":"2019-01",
                "grunnlagResultat":
                    {
                        "beregningsregel":"BLA"
                    },
                "bruktInntektsPeriode":
                    {
                        "førsteMåned":"2016-02",
                        "sisteMåned":"2016-11"
                    }

            }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", inntekt)
        TopologyTestDriver(periode.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(packet)
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                DAGPENGER_BEHOV_PACKET_EVENT.name,
                DAGPENGER_BEHOV_PACKET_EVENT.keySerde.deserializer(),
                DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.deserializer()
            )

            val result = ut.value()

            assertTrue { result.hasField("periodeResultat") }
        }
    }

    @Test
    fun ` Should add PeriodeSubsumsjon with oppfyllerKravTilFangstOgFisk`() {
        val periode = Periode(
            Environment(
                username = "bogus",
                password = "bogus"
            )
        )

        val inntekt: Inntekt = Inntekt(
            inntektsId = "12345",
            inntektsListe = listOf(
                KlassifisertInntektMåned(
                    årMåned = YearMonth.of(2018, 2),
                    klassifiserteInntekter = listOf(
                        KlassifisertInntekt(
                            beløp = BigDecimal(99999),
                            inntektKlasse = InntektKlasse.FANGST_FISKE
                        )
                    )

                )
            ),
            sisteAvsluttendeKalenderMåned = YearMonth.of(2018, 3)
        )

        val json = """
            {
                "behovId":"01D6V5QCJCH0NQCHF4PZYB0NRJ",
                "aktørId":"1000052711564",
                "vedtakId":3.1018297E7,
                "beregningsDato":"2018-04-06",
                "harAvtjentVerneplikt":false,
                "oppfyllerKravTilFangstOgFisk": true,
                "grunnlagResultat":
                    {
                        "beregningsregel":"BLA"
                    },
                "bruktInntektsPeriode":
                    {
                        "førsteMåned":"2016-02",
                        "sisteMåned":"2016-11"
                    }
            }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", inntekt)
        TopologyTestDriver(periode.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(packet)
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                DAGPENGER_BEHOV_PACKET_EVENT.name,
                DAGPENGER_BEHOV_PACKET_EVENT.keySerde.deserializer(),
                DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.deserializer()
            )

            val result = ut.value()

            assertTrue { result.hasField("periodeResultat") }
            assertEquals("52.0", result.getMapValue("periodeResultat")["periodeAntallUker"].toString())
        }
    }

    @Test
    fun ` Should add problem on failure`() {
        val minsteinntekt = Periode(
            Environment(
                username = "bogus",
                password = "bogus"
            )
        )

        val inntekt = Inntekt(
            inntektsId = "12345",
            inntektsListe = emptyList(),
            sisteAvsluttendeKalenderMåned = YearMonth.of(2018, 3)
        )

        val packet = Packet()
        packet.putValue("inntektV1", inntekt)
        packet.putValue("grunnlagResultat", "ERROR")

        TopologyTestDriver(minsteinntekt.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(packet)
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                DAGPENGER_BEHOV_PACKET_EVENT.name,
                DAGPENGER_BEHOV_PACKET_EVENT.keySerde.deserializer(),
                DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.deserializer()
            )

            assert(ut.value().hasProblem())
            Assertions.assertEquals(URI("urn:dp:error:regel"), ut.value().getProblem()!!.type)
            Assertions.assertEquals(URI("urn:dp:regel:periode"), ut.value().getProblem()!!.instance)
        }
    }
}