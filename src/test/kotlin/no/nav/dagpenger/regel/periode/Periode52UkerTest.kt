package no.nav.dagpenger.regel.periode

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.nare.core.evaluations.Resultat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

internal class Periode52UkerTest {
    @Test
    fun `Skal ikke gi periode på 52 uker når man har arbeidsinntekt over 2 G siste 12 mnd`() {
        val beregningsdato = LocalDate.of(2019, 5, 20)
        val fakta =
            Fakta(
                inntekt =
                    Inntekt(
                        "123",
                        generateArbeidsInntekt(1..12, BigDecimal(50000)),
                        sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4),
                    ),
                bruktInntektsperiode = null,
                verneplikt = false,
                fangstOgFiske = false,
                beregningsdato = beregningsdato,
                regelverksdato = LocalDate.of(2019, 5, 20),
                lærling = false,
                grunnlagBeregningsregel = "BLA",
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
            )

        val evaluering = ordinærSiste12Måneder52Uker.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi periode på 52 uker når man har næringsinntekt over 2 G siste 12 mnd og fangst og fisk er oppfylt`() {
        val beregningsdato = LocalDate.of(2019, 5, 20)
        val fakta =
            Fakta(
                inntekt =
                    Inntekt(
                        "123",
                        generateFangstOgFiskInntekt(1..12, BigDecimal(50000)),
                        sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4),
                    ),
                bruktInntektsperiode = null,
                verneplikt = false,
                fangstOgFiske = true,
                beregningsdato = beregningsdato,
                regelverksdato = LocalDate.of(2019, 5, 20),
                lærling = false,
                grunnlagBeregningsregel = "BLA",
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
            )

        val evaluering = ordinærSiste12MånederMedFangstOgFiske52Uker.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @ParameterizedTest
    @CsvSource(
        "2021-12-31, JA",
        "2022-01-01, NEI",
    )
    fun `Regelverk for fangst of fisk er avviklet fra og med 01-01-2022`(
        regelverksdato: LocalDate,
        forventetUtfall: String,
    ) {
        val forventetResultat = Resultat.valueOf(forventetUtfall)
        val sisteAvsluttendeKalenderMåned = YearMonth.of(2021, 11)

        val fangstOgFiskInntekter = generateFangstOgFiskInntekt(1..12, BigDecimal(50000))
        val arbeidsInntekter = generateArbeidsInntekt(1..12, BigDecimal(1000))

        val fakta =
            Fakta(
                inntekt =
                    Inntekt(
                        "123",
                        inntektsListe = fangstOgFiskInntekter + arbeidsInntekter,
                        sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned,
                    ),
                bruktInntektsperiode = null,
                verneplikt = false,
                fangstOgFiske = true,
                beregningsdato = regelverksdato,
                regelverksdato = regelverksdato,
                lærling = false,
                grunnlagBeregningsregel = "BLA",
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(regelverksdato),
            )

        val evaluering = fangstOgFiske52.evaluer(fakta)

        assertTrue(evaluering.children.all { it.resultat == forventetResultat })
    }

    @Test
    fun `Skal ikke gi periode på 52 uker når man har arbeidsinntekt over 2 G i snitt de siste 36 mnd`() {
        val beregningsdato = LocalDate.of(2019, 5, 20)
        val fakta =
            Fakta(
                inntekt =
                    Inntekt(
                        "123",
                        generateArbeidsInntekt(1..36, BigDecimal(50000)),
                        sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4),
                    ),
                bruktInntektsperiode = null,
                verneplikt = false,
                fangstOgFiske = false,
                beregningsdato = beregningsdato,
                regelverksdato = LocalDate.of(2019, 5, 20),
                lærling = false,
                grunnlagBeregningsregel = "BLA",
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
            )

        val evaluering = ordinærSiste36Måneder52Uker.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi periode på 52 uker når man har næringsinntekt over 2 G i snitt de siste 36 mnd og fangst og fisk er oppfylt`() {
        val beregningsdato = LocalDate.of(2019, 5, 20)
        val fakta =
            Fakta(
                inntekt =
                    Inntekt(
                        "123",
                        generateFangstOgFiskInntekt(1..12, BigDecimal(50000)),
                        sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4),
                    ),
                bruktInntektsperiode = null,
                verneplikt = false,
                fangstOgFiske = true,
                beregningsdato = beregningsdato,
                regelverksdato = LocalDate.of(2019, 5, 20),
                lærling = false,
                grunnlagBeregningsregel = "BLA",
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
            )

        val evaluering = ordinærSiste36MånederMedFangstOgFiske52Uker.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal gi periode på 52 uker når man har arbeidsinntekt under 2 G siste 12 mnd`() {
        val fakta =
            Fakta(
                inntekt =
                    Inntekt(
                        "123",
                        generateArbeidsInntekt(1..12, BigDecimal(1)),
                        sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4),
                    ),
                bruktInntektsperiode = null,
                verneplikt = false,
                fangstOgFiske = false,
                beregningsdato = LocalDate.of(2019, 5, 20),
                regelverksdato = LocalDate.of(2019, 5, 20),
                grunnbeløp = BigDecimal(13),
                lærling = false,
                grunnlagBeregningsregel = "BLA",
            )

        val evaluering = ordinærSiste12Måneder52Uker.evaluer(fakta)

        assertEquals(Resultat.JA, evaluering.resultat)
    }

    @Test
    fun `Skal gi periode på 52 uker når man har næringsinntektinntekt under 2 G siste 12 mnd og fangst og fisk er oppfylt`() {
        val fakta =
            Fakta(
                inntekt =
                    Inntekt(
                        "123",
                        generateFangstOgFiskInntekt(1..12, BigDecimal(1)),
                        sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4),
                    ),
                bruktInntektsperiode = null,
                verneplikt = false,
                fangstOgFiske = true,
                beregningsdato = LocalDate.of(2019, 5, 20),
                regelverksdato = LocalDate.of(2019, 5, 20),
                grunnbeløp = BigDecimal(13),
                lærling = false,
                grunnlagBeregningsregel = "BLA",
            )

        val evaluering = ordinærSiste12MånederMedFangstOgFiske52Uker.evaluer(fakta)

        assertEquals(Resultat.JA, evaluering.resultat)
    }

    @Test
    fun `Skal gi periode på 52 uker når man har arbeidsinntekt under 2 G i snitt de siste 36 mnd`() {
        val fakta =
            Fakta(
                inntekt =
                    Inntekt(
                        "123",
                        generateArbeidsInntekt(1..36, BigDecimal(1)),
                        sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4),
                    ),
                bruktInntektsperiode = null,
                verneplikt = false,
                fangstOgFiske = false,
                beregningsdato = LocalDate.of(2019, 5, 20),
                regelverksdato = LocalDate.of(2019, 5, 20),
                grunnbeløp = BigDecimal(37),
                lærling = false,
                grunnlagBeregningsregel = "BLA",
            )

        val evaluering = ordinærSiste36Måneder52Uker.evaluer(fakta)

        assertEquals(Resultat.JA, evaluering.resultat)
    }

    @Test
    fun `Skal gi periode på 52 uker når man har næringsinntekt under 2 G i snitt de siste 36 mnd og fangst og fisk er oppfylt`() {
        val fakta =
            Fakta(
                inntekt =
                    Inntekt(
                        "123",
                        generateFangstOgFiskInntekt(1..36, BigDecimal(1)),
                        sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4),
                    ),
                bruktInntektsperiode = null,
                verneplikt = false,
                fangstOgFiske = true,
                beregningsdato = LocalDate.of(2019, 5, 20),
                regelverksdato = LocalDate.of(2019, 5, 20),
                grunnbeløp = BigDecimal(37),
                lærling = false,
                grunnlagBeregningsregel = "BLA",
            )

        val evaluering = ordinærSiste36MånederMedFangstOgFiske52Uker.evaluer(fakta)

        assertEquals(Resultat.JA, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi periode på 52 uker når man har næringsinntektinntekt over 2 G siste 12 mnd men fangst og fisk er ikke oppfylt`() {
        val fakta =
            Fakta(
                inntekt =
                    Inntekt(
                        "123",
                        generate12MånederFangstOgFiskInntekt(),
                        sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4),
                    ),
                bruktInntektsperiode = null,
                verneplikt = false,
                fangstOgFiske = false,
                beregningsdato = LocalDate.of(2019, 5, 20),
                regelverksdato = LocalDate.of(2019, 5, 20),
                grunnbeløp = BigDecimal(13),
                lærling = false,
                grunnlagBeregningsregel = "BLA",
            )

        val evaluering = ordinærSiste12MånederMedFangstOgFiske52Uker.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    @Suppress("ktlint:standard:max-line-length")
    fun `Skal ikke gi periode på 52 uker når man har næringsinntektinntekt over 2 G i snitt de siste 36 mnd men fangst og fisk er ikke oppfylt`() {
        val fakta =
            Fakta(
                inntekt =
                    Inntekt(
                        "123",
                        generate36MånederFangstOgFiskInntekt(),
                        sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4),
                    ),
                bruktInntektsperiode = null,
                verneplikt = false,
                fangstOgFiske = false,
                beregningsdato = LocalDate.of(2019, 5, 20),
                regelverksdato = LocalDate.of(2019, 5, 20),
                grunnbeløp = BigDecimal(13),
                lærling = false,
                grunnlagBeregningsregel = "BLA",
            )

        val evaluering = ordinærSiste36MånederMedFangstOgFiske52Uker.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    fun generateArbeidsInntekt(
        range: IntRange,
        beløpPerMnd: BigDecimal,
    ): List<KlassifisertInntektMåned> {
        return (range).toList().map {
            KlassifisertInntektMåned(
                YearMonth.of(2019, 1).minusMonths(it.toLong()),
                listOf(
                    KlassifisertInntekt(
                        beløpPerMnd,
                        InntektKlasse.ARBEIDSINNTEKT,
                    ),
                ),
            )
        }
    }

    fun generateFangstOgFiskInntekt(
        range: IntRange,
        beløpPerMnd: BigDecimal,
    ): List<KlassifisertInntektMåned> {
        return (range).toList().map {
            KlassifisertInntektMåned(
                YearMonth.of(2019, 1).minusMonths(it.toLong()),
                listOf(
                    KlassifisertInntekt(
                        beløpPerMnd,
                        InntektKlasse.FANGST_FISKE,
                    ),
                ),
            )
        }
    }

    fun generate12MånederFangstOgFiskInntekt(): List<KlassifisertInntektMåned> {
        return generateArbeidsInntekt(1..12, BigDecimal(1000))
    }

    fun generate36MånederFangstOgFiskInntekt(): List<KlassifisertInntektMåned> {
        return generateArbeidsInntekt(1..36, BigDecimal(1000))
    }
}
