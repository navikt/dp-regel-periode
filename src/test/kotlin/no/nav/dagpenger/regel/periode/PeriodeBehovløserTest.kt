package no.nav.dagpenger.regel.periode

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.AVTJENT_VERNEPLIKT
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.GRUNNLAG_RESULTAT
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.INNTEKT
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.PERIODE_RESULTAT
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test
import java.time.YearMonth

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
}
