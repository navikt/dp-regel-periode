package no.nav.dagpenger.regel.periode

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.nare.core.evaluations.Resultat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

internal class PeriodeEtterAvtjentVernepliktTest {
    @Test
    fun ` § 4-19 - Dagpenger etter avtjent verneplikt skal gi 26 uker  `() {
        val beregningsdato = LocalDate.of(2019, 5, 20)
        val fakta =
            Fakta(
                inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
                bruktInntektsperiode = null,
                verneplikt = true,
                fangstOgFiske = false,
                beregningsdato = beregningsdato,
                regelverksdato = LocalDate.of(2019, 5, 20),
                lærling = false,
                grunnlagBeregningsregel = "Verneplikt",
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
            )

        val evaluering = vernepliktPeriode.evaluer(fakta)
        assertEquals(Resultat.JA, evaluering.resultat)
        assertEquals(26, Evalueringer.finnHøyestePeriodeFraEvaluering(evaluering, "Verneplikt"))
    }

    @Test
    fun ` § 4-19 - Dagpenger etter ikke å ha avtjent verneplikt skal ikke gi 26 uker  `() {
        val beregningsdato = LocalDate.of(2019, 5, 20)
        val fakta =
            Fakta(
                inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
                bruktInntektsperiode = null,
                verneplikt = false,
                fangstOgFiske = false,
                beregningsdato = beregningsdato,
                regelverksdato = LocalDate.of(2019, 5, 20),
                lærling = false,
                grunnlagBeregningsregel = "BLA",
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
            )

        val evaluering = vernepliktPeriode.evaluer(fakta)
        assertEquals(null, Evalueringer.finnHøyestePeriodeFraEvaluering(evaluering, "BLA"))
        assertEquals(Resultat.NEI, evaluering.resultat)
    }
}
