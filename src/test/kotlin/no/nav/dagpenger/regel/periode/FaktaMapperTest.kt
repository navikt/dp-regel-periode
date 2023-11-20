package no.nav.dagpenger.regel.periode

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.regel.periode.FaktaMapper.ManglendeGrunnlagBeregningsregelException
import no.nav.dagpenger.regel.periode.FaktaMapper.grunnlagBeregningsregel
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.FANGST_OG_FISK
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.GRUNNLAG_BEREGNINGSREGEL
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.GRUNNLAG_RESULTAT
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.INNTEKT
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.LÆRLING
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

class FaktaMapperTest {
    private val testRapid = TestRapid()

    private companion object {
        private val emptyInntekt =
            mapOf(
                "inntektsId" to "12345",
                "inntektsListe" to emptyList<String>(),
                "sisteAvsluttendeKalenderMåned" to YearMonth.of(2018, 3),
            )

        private fun testMessage(
            behovId: String = "behovId",
            beregningsdato: LocalDate = LocalDate.MAX,
            inntekt: Map<String, Any> = emptyInntekt,
            lærling: Any? = false,
            regelverksdato: LocalDate? = null,
            beregningsregelGrunnlag: String? = "Mikke",
            fangstOgFisk: Any? = true,
        ): String {
            val testMap =
                mutableMapOf(
                    // BEHOV_ID to behovId,
                    INNTEKT to inntekt,
                    BEREGNINGSDATO to beregningsdato,
                    GRUNNLAG_RESULTAT to "{}",
                )

            beregningsregelGrunnlag?.let {
                testMap[GRUNNLAG_RESULTAT] = mapOf(GRUNNLAG_BEREGNINGSREGEL to beregningsregelGrunnlag)
            }
            fangstOgFisk?.let {
                testMap[FANGST_OG_FISK] = fangstOgFisk
            }

            lærling?.let {
                testMap[LÆRLING] = lærling
            }

            return JsonMessage.newMessage(testMap).toJson()
        }
    }

    @Test
    fun `Beregningsregel grunnlag`() {
        val behovløser = OnPacketTestListener(testRapid)
        testRapid.sendTestMessage(testMessage(beregningsregelGrunnlag = "Langbein"))
        packetToFakta(behovløser.packet, GrunnbeløpStrategy()).grunnlagBeregningsregel shouldBe "Langbein"

        testRapid.sendTestMessage(testMessage(beregningsregelGrunnlag = null))

        shouldThrow<ManglendeGrunnlagBeregningsregelException> {
            packetToFakta(behovløser.packet, GrunnbeløpStrategy()).grunnlagBeregningsregel
        }
    }

    @Test
    fun `Fangst og fiske blir mappet riktig`() {
        val behovløser = OnPacketTestListener(testRapid)

        testRapid.sendTestMessage(testMessage(fangstOgFisk = true))
        packetToFakta(behovløser.packet, GrunnbeløpStrategy()).fangstOgFisk shouldBe true

        testRapid.sendTestMessage(testMessage(fangstOgFisk = false))
        packetToFakta(behovløser.packet, GrunnbeløpStrategy()).fangstOgFisk shouldBe false

        testRapid.sendTestMessage(testMessage(fangstOgFisk = null))
        packetToFakta(behovløser.packet, GrunnbeløpStrategy()).fangstOgFisk shouldBe false

        testRapid.sendTestMessage(testMessage(fangstOgFisk = 100))
        shouldThrow<IllegalArgumentException> {
            packetToFakta(behovløser.packet, GrunnbeløpStrategy()).fangstOgFisk
        }
    }

    @Test
    fun `lærling mappet riktig`() {
        val behovløser = OnPacketTestListener(testRapid)

        testRapid.sendTestMessage(testMessage(lærling = true))
        packetToFakta(behovløser.packet, GrunnbeløpStrategy()).lærling shouldBe true

        testRapid.sendTestMessage(testMessage(lærling = false))
        packetToFakta(behovløser.packet, GrunnbeløpStrategy()).lærling shouldBe false

        testRapid.sendTestMessage(testMessage(lærling = null))
        packetToFakta(behovløser.packet, GrunnbeløpStrategy()).lærling shouldBe false

        testRapid.sendTestMessage(testMessage(lærling = 100))
        shouldThrow<IllegalArgumentException> {
            packetToFakta(behovløser.packet, GrunnbeløpStrategy()).lærling
        }
    }

    private class OnPacketTestListener(rapidsConnection: RapidsConnection) : River.PacketListener {
        var problems: MessageProblems? = null
        lateinit var packet: JsonMessage

        init {
            River(rapidsConnection).apply(PeriodeBehovløser.rapidFilter).register(this)
        }

        override fun onPacket(
            packet: JsonMessage,
            context: MessageContext,
        ) {
            this.packet = packet
        }

        override fun onError(
            problems: MessageProblems,
            context: MessageContext,
        ) {
            this.problems = problems
        }
    }
}
