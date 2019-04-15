package no.nav.dagpenger.regel.periode

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth
import kotlin.test.assertEquals

class FinnerRettPeriodeTest {

    @Test
    fun ` Skal returnere 26 uker periode dersom beregningsregelen fra grunnlag er verneplikt `() {

        val fakta = Fakta(
            inntekt = Inntekt(
                "123",
                generateArbeidsInntekt(1..12, BigDecimal(3000))),
            senesteInntektsmåned = YearMonth.of(2019, 4),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = false,
            grunnlagBeregningsregel = "Verneplikt")

        val resultat = periode.evaluer(fakta)

        assertEquals(26, finnHøyestePeriodeFraEvaluering(resultat, fakta))
    }

    @Test
    fun ` Skal returnere 52 uker periode dersom beregningsregelen fra grunnlag er ikke er verneplikt og har tjent mindre enn 2G `() {

        val fakta = Fakta(
            inntekt = Inntekt(
                "123",
                generateArbeidsInntekt(1..12, BigDecimal(3000))),
            senesteInntektsmåned = YearMonth.of(2019, 4),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = false,
            grunnlagBeregningsregel = "BLA")

        val resultat = periode.evaluer(fakta)

        assertEquals(52, finnHøyestePeriodeFraEvaluering(resultat, fakta))
    }

    @Test
    fun ` Skal returnere 104 uker periode dersom beregningsregelen fra grunnlag er ikke er verneplikt og har tjent mer enn 2G `() {

        val fakta = Fakta(
            inntekt = Inntekt(
                "123",
                generateArbeidsInntekt(1..12, BigDecimal(30000))),
            senesteInntektsmåned = YearMonth.of(2019, 4),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = false,
            grunnlagBeregningsregel = "BLA")

        val resultat = periode.evaluer(fakta)

        assertEquals(104, finnHøyestePeriodeFraEvaluering(resultat, fakta))
    }

    fun generateArbeidsInntekt(range: IntRange, beløpPerMnd: BigDecimal): List<KlassifisertInntektMåned> {
        return (range).toList().map {
            KlassifisertInntektMåned(
                YearMonth.of(2019, 1).minusMonths(it.toLong()), listOf(
                    KlassifisertInntekt(
                        beløpPerMnd, InntektKlasse.ARBEIDSINNTEKT)
                ))
        }
    }
}