package no.nav.dagpenger.regel.periode

import com.fasterxml.jackson.databind.JsonNode
import de.huxhorn.sulky.ulid.ULID
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal
import java.time.YearMonth
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.inntekt.rpc.InntektHenter
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

internal class LøsningServiceTest {

    private val inntekt = Inntekt(
        inntektsId = "12345",
        inntektsListe = listOf(
            KlassifisertInntektMåned(
                årMåned = YearMonth.of(2018, 2),
                klassifiserteInntekter = listOf(
                    KlassifisertInntekt(
                        beløp = BigDecimal(25000),
                        inntektKlasse = InntektKlasse.ARBEIDSINNTEKT
                    )
                )

            )
        ),
        sisteAvsluttendeKalenderMåned = YearMonth.of(2018, 2)
    )

    private val inntektHenter = mockk<InntektHenter>().also {
        every { runBlocking { it.hentKlassifisertInntekt(any()) } } returns inntekt
    }

    private val rapid = TestRapid().apply {
        LøsningService(this, inntektHenter)
    }

    @Test
    fun `skal legge på periode-løsning på pakker vi forstår`() {

        rapid.sendTestMessage("sldjjfnqaolsdjcb")
        rapid.sendTestMessage(json)

        with(rapid.inspektør) {
            size shouldBeExactly 1
            field(0, "@behov").map(JsonNode::asText).shouldContain("Periode")
            field(0, "@løsning")["Periode"]["periodeAntallUker"] shouldNotBe null
        }
    }

    @Language("JSON")
    private val json =
        """
            {
              "@behov": [
                "Periode"
              ],
              "@id": "123",
              "beregningsdato": "2020-04-01",
              "bruktInntektsPeriode": {
              },
              "vedtakId": "abc",
              "inntektId": "${ULID().nextULID()}"
           }
            """.trimIndent()
}
