package no.nav.dagpenger.regel.periode

import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TestOutputTopic
import org.apache.kafka.streams.TopologyTestDriver
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.net.URI
import java.time.YearMonth
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ApplicationTopologyTest {
    private val inntekt =
        Inntekt(
            inntektsId = "12345",
            inntektsListe =
                listOf(
                    KlassifisertInntektMåned(
                        årMåned = YearMonth.of(2019, 2),
                        klassifiserteInntekter =
                            listOf(
                                KlassifisertInntekt(
                                    beløp = BigDecimal(25000),
                                    inntektKlasse = InntektKlasse.ARBEIDSINNTEKT,
                                ),
                            ),
                    ),
                ),
            sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 2),
        )

    companion object {
        val periode = Application(Configuration())
        val config =
            Properties().apply {
                this[StreamsConfig.APPLICATION_ID_CONFIG] = "test"
                this[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "dummy:1234"
            }
    }

    @Test
    fun ` Should add PeriodeSubsumsjon `() {
        val json =
            """
            {
                "behovId":"01D6V5QCJCH0NQCHF4PZYB0NRJ",
                "aktørId":"1000052711564",
                "vedtakId":3.1018297E7,
                "beregningsDato":"2019-02-27",
                "harAvtjentVerneplikt":false,
                "grunnlagResultat":
                    {
                        "beregningsregel": "BLA"
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
            topologyTestDriver.regelInputTopic().also { it.pipeInput(packet) }
            val ut = topologyTestDriver.regelOutputTopic().readValue()
            assertTrue { ut.hasField("periodeResultat") }
        }
    }

    @Test
    fun ` Should add PeriodeSubsumsjon with oppfyllerKravTilFangstOgFisk`() {
        val inntekt =
            Inntekt(
                inntektsId = "12345",
                inntektsListe =
                    listOf(
                        KlassifisertInntektMåned(
                            årMåned = YearMonth.of(2018, 2),
                            klassifiserteInntekter =
                                listOf(
                                    KlassifisertInntekt(
                                        beløp = BigDecimal(99999),
                                        inntektKlasse = InntektKlasse.FANGST_FISKE,
                                    ),
                                ),
                        ),
                    ),
                sisteAvsluttendeKalenderMåned = YearMonth.of(2018, 3),
            )

        val json =
            """
            {
                "behovId":"01D6V5QCJCH0NQCHF4PZYB0NRJ",
                "aktørId":"1000052711564",
                "vedtakId":3.1018297E7,
                "beregningsDato":"2018-04-06",
                "harAvtjentVerneplikt":false,
                "oppfyllerKravTilFangstOgFisk": true,
                "grunnlagResultat":
                    {
                        "beregningsregel": "BLA"
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
            topologyTestDriver.regelInputTopic().also { it.pipeInput(packet) }
            val ut = topologyTestDriver.regelOutputTopic().readValue()
            assertTrue { ut.hasField("periodeResultat") }
            assertEquals("52.0", ut.getMapValue("periodeResultat")["periodeAntallUker"].toString())
        }
    }

    @Test
    fun ` Should add problem on failure`() {
        val json =
            """
            {
                "beregningsDato": "2019-05-20"
            }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", "ERROR")
        packet.putValue("grunnlagResultat", "ERROR")

        TopologyTestDriver(periode.buildTopology(), config).use { topologyTestDriver ->
            topologyTestDriver.regelInputTopic().also { it.pipeInput(packet) }
            val ut = topologyTestDriver.regelOutputTopic().readValue()

            assert(ut.hasProblem())
            assertEquals(URI("urn:dp:error:regel"), ut.getProblem()!!.type)
            assertEquals(URI("urn:dp:regel:periode"), ut.getProblem()!!.instance)
        }
    }
}

private fun TopologyTestDriver.regelInputTopic(): TestInputTopic<String, Packet> =
    this.createInputTopic(
        REGEL_TOPIC.name,
        REGEL_TOPIC.keySerde.serializer(),
        REGEL_TOPIC.valueSerde.serializer(),
    )

private fun TopologyTestDriver.regelOutputTopic(): TestOutputTopic<String, Packet> =
    this.createOutputTopic(
        REGEL_TOPIC.name,
        REGEL_TOPIC.keySerde.deserializer(),
        REGEL_TOPIC.valueSerde.deserializer(),
    )
