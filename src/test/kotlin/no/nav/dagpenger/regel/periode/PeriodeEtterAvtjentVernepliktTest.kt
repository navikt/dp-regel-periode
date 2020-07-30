package no.nav.dagpenger.regel.periode

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.nare.core.evaluations.Resultat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

internal class PeriodeEtterAvtjentVernepliktTest {

    @Test
    fun ` § 4-19 - Dagpenger etter avtjent verneplikt skal gi 26 uker  `() {

        // gitt fakta
        val fakta = Fakta(
            inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = false,
            beregningsDato = LocalDate.of(2019, 5, 20),
            lærling = false,
            grunnlagBeregningsregel = "Verneplikt"
        )

        // når
        val evaluering = vernepliktPeriode.evaluer(fakta)

        // så
        Assertions.assertEquals(Resultat.JA, evaluering.resultat)
    }

    @Test
    fun ` § 4-19 - Dagpenger etter ikke å ha avtjent verneplikt skal ikke gi 26 uker  `() {

        // gitt fakta
        val fakta = Fakta(
            inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false,
            beregningsDato = LocalDate.of(2019, 5, 20),
            lærling = false,
            grunnlagBeregningsregel = "BLA"
        )

        // når
        val evaluering = vernepliktPeriode.evaluer(fakta)

        // så
        Assertions.assertEquals(Resultat.NEI, evaluering.resultat)
    }
}
