package no.nav.dagpenger.regel.periode

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.BEHOV_ID
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.GRUNNLAG_RESULTAT
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.INNTEKT
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.PERIODE_RESULTAT
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.PROBLEM
import org.junit.jupiter.api.Test

class RapidFilterTest {
    private val testRapid = TestRapid()
    private val testMessage =
        mapOf(
            BEHOV_ID to "behovIdVerdi",
            BEREGNINGSDATO to "beregningsdatoVerdi",
            INNTEKT to "inntektVerdi",
            GRUNNLAG_RESULTAT to "grunnlagResultatVerdi",
        )

    @Test
    fun `Skal behandle pakker med alle required keys`() {
        val testListener = TestListener(testRapid)
        testRapid.sendTestMessage(
            JsonMessage.newMessage(testMessage).toJson(),
        )
        testListener.onPacketCalled shouldBe true
    }

    @Test
    fun `Skal ikke behandle pakker med løsning`() {
        val testListener = TestListener(testRapid)
        val messageMedLøsning =
            testMessage.toMutableMap().also {
                it[PERIODE_RESULTAT] = "verdi"
            }
        testRapid.sendTestMessage(
            JsonMessage.newMessage(messageMedLøsning).toJson(),
        )
        testListener.onPacketCalled shouldBe false
    }

    @Test
    fun `Skal ikke behandle pakker med problem`() {
        val testListener = TestListener(testRapid)
        testRapid.sendTestMessage(
            JsonMessage.newMessage(
                testMessage.toMutableMap().also {
                    it[PROBLEM] = "problem"
                },
            ).toJson(),
        )
        testListener.onPacketCalled shouldBe false
    }

    @Test
    fun `Skal kke behandle pakker med  manglende required keys`() {
        val testListener = TestListener(testRapid)

        testRapid.sendTestMessage(
            testMessage.muterOgKonverterToJsonString { it.remove(BEREGNINGSDATO) },
        )
        testListener.onPacketCalled shouldBe false

        testRapid.sendTestMessage(
            testMessage.muterOgKonverterToJsonString { it.remove(INNTEKT) },
        )
        testListener.onPacketCalled shouldBe false

        testRapid.sendTestMessage(
            testMessage.muterOgKonverterToJsonString { it.remove(GRUNNLAG_RESULTAT) },
        )
        testListener.onPacketCalled shouldBe false
    }

    private fun Map<String, Any>.muterOgKonverterToJsonString(block: (map: MutableMap<String, Any>) -> Unit): String {
        val mutableMap = this.toMutableMap()
        block.invoke(mutableMap)
        return JsonMessage.newMessage(mutableMap).toJson()
    }

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
