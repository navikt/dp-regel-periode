package no.nav.dagpenger.regel.periode

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.AVTJENT_VERNEPLIKT
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.GRUNNLAG_RESULTAT
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.INNTEKT
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.PERIODE_RESULTAT
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.apache.kafka.streams.TopologyTestDriver
import org.junit.jupiter.api.Test
import java.time.YearMonth
import kotlin.test.assertTrue

class PeriodeBehovløserTest {
    private val testrapid = TestRapid()

    init {
        PeriodeBehovløser(testrapid)
    }

    @Test
    fun `Vernepliktperiode burde være 26 uker`() {
        val nullInntekt =
            Inntekt(
                inntektsId = "123",
                inntektsListe = emptyList(),
                sisteAvsluttendeKalenderMåned = YearMonth.of(2021, 4),
            )

        val testMessage =
            JsonMessage.newMessage(
                mapOf(
                    AVTJENT_VERNEPLIKT to true,
                    BEREGNINGSDATO to "2020-05-20",
                    GRUNNLAG_RESULTAT to mapOf(PeriodeBehovløser.GRUNNLAG_BEREGNINGSREGEL to "Verneplikt"),
                    INNTEKT to
                        jsonMapper.convertValue(
                            nullInntekt, Map::class.java,
                        ),
                ),
            )

        testrapid.sendTestMessage(testMessage.toJson())

        val message = testrapid.inspektør.message(0)
        message[PERIODE_RESULTAT]["periodeAntallUker"].asInt() shouldBe 26
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
        TopologyTestDriver(ApplicationTopologyTest.periode.buildTopology(), ApplicationTopologyTest.config).use { topologyTestDriver ->
            topologyTestDriver.regelInputTopic().also { it.pipeInput(packet) }
            val ut = topologyTestDriver.regelOutputTopic().readValue()
            assertTrue { ut.hasField("periodeResultat") }
        }
    }
}
