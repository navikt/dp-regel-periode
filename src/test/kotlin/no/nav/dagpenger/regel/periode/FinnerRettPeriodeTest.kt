package no.nav.dagpenger.regel.periode

import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import org.junit.jupiter.api.Test

class FinnerRettPeriodeTest {

    @Test
    fun ` Skal returnere 26 uker periode dersom beregningsregel fra grunnlag er Verneplikt`() {

        val grunnlagBeregningsregel = "Verneplikt"
        val inntektsListe = generateArbeidsInntekt(
            1..12, BigDecimal(3000)
        )
        val fakta = Fakta(
            inntekt = Inntekt(
                "123",
                inntektsListe, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)

            ),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = false,
            beregningsDato = LocalDate.of(2019, 5, 20),
            lærling = false,
            grunnlagBeregningsregel = grunnlagBeregningsregel
        )

        val resultat = periode.evaluer(fakta)

        assertEquals(26, finnHøyestePeriodeFraEvaluering(resultat, grunnlagBeregningsregel))
    }

    @Test
    fun ` Skal returnere 26 uker periode dersom beregningsregel fra grunnlag er Verneplikt og har minus i inntektsum `() {
        val grunnlagBeregningsregel = "Verneplikt"
        val fakta = Fakta(
            inntekt = Inntekt(
                "123",
                getMinusInntekt(),
                sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)
            ),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = false,
            beregningsDato = LocalDate.of(2019, 5, 20),
            lærling = false,
            grunnlagBeregningsregel = grunnlagBeregningsregel
        )

        assertEquals(expected = (-950000).toBigDecimal(), actual = fakta.arbeidsinntektSiste12)

        val resultat = periode.evaluer(fakta)

        assertEquals(26, finnHøyestePeriodeFraEvaluering(resultat, grunnlagBeregningsregel))
    }

    @Test
    fun ` Skal returnere 52 uker periode dersom beregningsregel fra grunnlag ikke er Verneplikt og har tjent mindre enn 2G `() {

        val grunnlagBeregningsregel = "BLA"
        val inntektsListe = generateArbeidsInntekt(1..12, BigDecimal(3000))
        val fakta = Fakta(
            inntekt = Inntekt(
                "123",
                inntektsListe,
                sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)
            ),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false,
            beregningsDato = LocalDate.of(2019, 5, 20),
            lærling = false,
            grunnlagBeregningsregel = grunnlagBeregningsregel
        )

        val resultat = periode.evaluer(fakta)

        assertEquals(52, finnHøyestePeriodeFraEvaluering(resultat, grunnlagBeregningsregel))
    }

    @Test
    fun ` Skal returnere 104 uker periode dersom beregningsregel fra grunnlag ikke er Verneplikt og har tjent mer enn 2G `() {

        val grunnlagBeregningsregel = "BLA"
        val inntektsListe = generateArbeidsInntekt(1..12, BigDecimal(30000))
        val fakta = Fakta(
            inntekt = Inntekt(
                "123",
                inntektsListe,
                sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)
            ),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false,
            beregningsDato = LocalDate.of(2019, 5, 20),
            lærling = false,
            grunnlagBeregningsregel = grunnlagBeregningsregel
        )

        val resultat = periode.evaluer(fakta)

        assertEquals(104, finnHøyestePeriodeFraEvaluering(resultat, grunnlagBeregningsregel))
    }

    @Test
    fun ` Skal returnere 52 uker periode dersom beregningsregel fra grunnlag ikke er Verneplikt og har tjent mindre enn 2G pga minusinntekt `() {
        val grunnlagBeregningsregel = "BLA"
        val inntekt = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2019, 3), klassifiserteInntekter = listOf(
                    KlassifisertInntekt(
                        beløp = BigDecimal(1000000),
                        inntektKlasse = InntektKlasse.ARBEIDSINNTEKT
                    ),
                    KlassifisertInntekt(
                        beløp = BigDecimal(-950000),
                        inntektKlasse = InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt(
                "123",
                inntekt,
                sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)
            ),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false,
            beregningsDato = LocalDate.of(2019, 5, 20),
            lærling = false,
            grunnlagBeregningsregel = grunnlagBeregningsregel
        )

        assertEquals(50000.toBigDecimal(), fakta.arbeidsinntektSiste12)

        val resultat = periode.evaluer(fakta)

        assertEquals(52, finnHøyestePeriodeFraEvaluering(resultat, grunnlagBeregningsregel))
    }

    @Test
    fun ` Skal returnere 52 uker periode dersom beregningsregel fra grunnlag ikke er Verneplikt og har minus i inntektsum `() {
        val grunnlagBeregningsregel = "BLA"
        val fakta = Fakta(
            inntekt = Inntekt(
                "123",
                getMinusInntekt(),
                sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)
            ),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false,
            beregningsDato = LocalDate.of(2019, 5, 20),
            lærling = false,
            grunnlagBeregningsregel = grunnlagBeregningsregel
        )

        assertEquals((-950000.toBigDecimal()), fakta.arbeidsinntektSiste12)

        val resultat = periode.evaluer(fakta)

        assertEquals(52, finnHøyestePeriodeFraEvaluering(resultat, grunnlagBeregningsregel))
    }

    @Test
    fun ` Skal returnere 104 uker periode dersom beregningsregel fra grunnlag er ikke Verneplikt og har tjent mer enn 3G `() {

        val grunnlagBeregningsregel = "IKKE_VERNEPLIKT"
        val inntektsListe = generateArbeidsInntekt(1..12, BigDecimal(30000))
        val fakta = Fakta(
            inntekt = Inntekt(
                "123",
                inntektsListe,
                sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)
            ),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = false,
            beregningsDato = LocalDate.of(2019, 5, 20),
            lærling = false,
            grunnlagBeregningsregel = grunnlagBeregningsregel
        )

        val resultat = periode.evaluer(fakta)

        assertEquals(104, finnHøyestePeriodeFraEvaluering(resultat, grunnlagBeregningsregel))
    }

    fun generateArbeidsInntekt(range: IntRange, beløpPerMnd: BigDecimal): List<KlassifisertInntektMåned> {
        return (range).toList().map {
            KlassifisertInntektMåned(
                YearMonth.of(2019, 1).minusMonths(it.toLong()), listOf(
                    KlassifisertInntekt(
                        beløpPerMnd, InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        }
    }

    fun getMinusInntekt(): List<KlassifisertInntektMåned> {
        return listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2019, 3),
                klassifiserteInntekter = listOf(
                    KlassifisertInntekt(
                        beløp = BigDecimal(1000000),
                        inntektKlasse = InntektKlasse.ARBEIDSINNTEKT
                    ),
                    KlassifisertInntekt(
                        beløp = BigDecimal(-1950000),
                        inntektKlasse = InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        )
    }
}
