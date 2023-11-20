package no.nav.dagpenger.regel.periode

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.GRUNNLAG_RESULTAT
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.INNTEKT
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class RapidFilterTest {
    private val testRapid = TestRapid()
    private val inntektId = "01HF4BNZTR2F30GR0Q0TCH22KS"
    private val testMessage =
        mapOf(
            // PeriodeBehovløser.BEHOV_ID to "ULID",
            BEREGNINGSDATO to "2020-04-30",
            INNTEKT to inntektJson(inntektId),
            GRUNNLAG_RESULTAT to mapOf(PeriodeBehovløser.GRUNNLAG_BEREGNINGSREGEL to "VeldigFinBeregningsregel"),
        )

    @Test
    fun `Skal behandle pakker med alle required keys, unntatt pakker med løsning`() {
        val testListener = TestListener(testRapid)
        testRapid.sendTestMessage(
            JsonMessage.newMessage(testMessage).toJson(),
        )
        testListener.onPacketCalled shouldBe true
    }

    @Language("JSON")
    private fun inntektJson(inntektId: String) =
        """
        {
          "inntektsId": "$inntektId",
          "inntektsListe": [
            {
              "årMåned": "2020-10",
              "klassifiserteInntekter": [
                {
                  "beløp": "40000",
                  "inntektKlasse": "ARBEIDSINNTEKT"
                }
              ],
              "harAvvik": false
            },
            {
              "årMåned": "2020-11",
              "klassifiserteInntekter": [
                {
                  "beløp": "40000",
                  "inntektKlasse": "ARBEIDSINNTEKT"
                }
              ],
              "harAvvik": false
            }
          ],
          "manueltRedigert": false,
          "sisteAvsluttendeKalenderMåned": "2023-09"
        }
        """.trimIndent()

    private class TestListener(rapidsConnection: RapidsConnection) : River.PacketListener {
        var onPacketCalled = false

        init {
            River(rapidsConnection).apply(
                PeriodeBehovløser.rapidFilter,
            ).register(this)
        }

        override fun onPacket(
            packet: JsonMessage,
            context: MessageContext,
        ) {
            this.onPacketCalled = true
        }

        override fun onError(
            problems: MessageProblems,
            context: MessageContext,
        ) {
        }
    }
}
