package no.nav.dagpenger.regel.periode

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.nare.core.evaluations.Resultat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth

internal class Periode104UkerTest {

    @Test
    fun `Skal gi periode på 104 uker når man har arbeidsinntekt over 2 G siste 12 mnd`() {

        val fakta = Fakta(inntekt = Inntekt("123", generate12MånederArbeidsInntekt()),
            senesteInntektsmåned = YearMonth.of(2019, 4),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false)

        val evaluering = ordinærSiste12Måneder104Uker.evaluer(fakta)

        assertEquals(Resultat.JA, evaluering.resultat)
    }

    @Test
    fun `Skal gi periode på 104 uker når man har næringsinntekt over 2 G siste 12 mnd og fangst og fisk er oppfylt`() {

        val fakta = Fakta(inntekt = Inntekt("123", generate12MånederFangstOgFiskInntekt()),
            senesteInntektsmåned = YearMonth.of(2019, 4),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = true)

        val evaluering = ordinærSiste12MånederMedFangstOgFiske104Uker.evaluer(fakta)

        assertEquals(Resultat.JA, evaluering.resultat)
    }

    @Test
    fun `Skal gi periode på 104 uker når man har arbeidsinntekt over 2 G i snitt de siste 36 mnd`() {

        val fakta = Fakta(inntekt = Inntekt("123", generate36MånederArbeidsInntekt()),
            senesteInntektsmåned = YearMonth.of(2019, 4),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false)

        val evaluering = ordinærSiste36Måneder104Uker.evaluer(fakta)

        assertEquals(Resultat.JA, evaluering.resultat)
    }

    @Test
    fun `Skal gi periode på 104 uker når man har næringsinntekt over 2 G i snitt de siste 36 mnd og fangst og fisk er oppfylt`() {

        val fakta = Fakta(inntekt = Inntekt("123", generate36MånederFangstOgFiskInntekt()),
            senesteInntektsmåned = YearMonth.of(2019, 4),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = true)

        val evaluering = ordinærSiste36MånederMedFangstOgFiske104Uker.evaluer(fakta)

        assertEquals(Resultat.JA, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi periode på 104 uker når man har arbeidsinntekt under 2 G siste 12 mnd`() {

        val fakta = Fakta(inntekt = Inntekt("123", generateArbeidsInntekt(1..12, BigDecimal(1))),
            senesteInntektsmåned = YearMonth.of(2019, 4),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false,
            grunnbeløp = BigDecimal(13)
        )

        val evaluering = ordinærSiste12Måneder104Uker.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi periode på 104 uker når man har næringsinntektinntekt under 2 G siste 12 mnd og fangst og fisk er oppfylt`() {

        val fakta = Fakta(inntekt = Inntekt("123", generateFangstOgFiskInntekt(1..12, BigDecimal(1))),
            senesteInntektsmåned = YearMonth.of(2019, 4),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = true,
            grunnbeløp = BigDecimal(13)
        )

        val evaluering = ordinærSiste12MånederMedFangstOgFiske104Uker.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi periode på 104 uker når man har arbeidsinntekt under 2 G i snitt de siste 36 mnd`() {

        val fakta = Fakta(inntekt = Inntekt("123", generateArbeidsInntekt(1..36, BigDecimal(1))),
            senesteInntektsmåned = YearMonth.of(2019, 4),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false,
            grunnbeløp = BigDecimal(37)
        )

        val evaluering = ordinærSiste36Måneder104Uker.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi periode på 104 uker når man har næringsinntekt under 2 G i snitt de siste 36 mnd og fangst og fisk er oppfylt`() {

        val fakta = Fakta(inntekt = Inntekt("123", generateFangstOgFiskInntekt(1..36, BigDecimal(1))),
            senesteInntektsmåned = YearMonth.of(2019, 4),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = true,
            grunnbeløp = BigDecimal(37)
        )

        val evaluering = ordinærSiste36MånederMedFangstOgFiske104Uker.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi periode på 104 uker når man har næringsinntektinntekt over 2 G siste 12 mnd men fangst og fisk er ikke oppfylt`() {

        val fakta = Fakta(inntekt = Inntekt("123", generate12MånederFangstOgFiskInntekt()),
            senesteInntektsmåned = YearMonth.of(2019, 4),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false,
            grunnbeløp = BigDecimal(13)
        )

        val evaluering = ordinærSiste12MånederMedFangstOgFiske104Uker.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi periode på 104 uker når man har næringsinntektinntekt over 2 G i snitt de siste 36 mnd men fangst og fisk er ikke oppfylt`() {

        val fakta = Fakta(inntekt = Inntekt("123", generate36MånederFangstOgFiskInntekt()),
            senesteInntektsmåned = YearMonth.of(2019, 4),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false,
            grunnbeløp = BigDecimal(13)
        )

        val evaluering = ordinærSiste36MånederMedFangstOgFiske104Uker.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    fun generateArbeidsInntekt(range: IntRange, beløpPerMnd: BigDecimal): List<KlassifisertInntektMåned> {
        return (range).toList().map {
            KlassifisertInntektMåned(YearMonth.of(2019, 1).minusMonths(it.toLong()), listOf(
                KlassifisertInntekt(
                    beløpPerMnd, InntektKlasse.ARBEIDSINNTEKT)
            ))
        }
    }

    fun generate12MånederArbeidsInntekt(): List<KlassifisertInntektMåned> {
        return generateArbeidsInntekt(1..12, BigDecimal(50000))
    }

    fun generate36MånederArbeidsInntekt(): List<KlassifisertInntektMåned> {
        return generateArbeidsInntekt(1..36, BigDecimal(50000))
    }

    fun generateFangstOgFiskInntekt(range: IntRange, beløpPerMnd: BigDecimal): List<KlassifisertInntektMåned> {
        return (range).toList().map {
            KlassifisertInntektMåned(YearMonth.of(2019, 1).minusMonths(it.toLong()), listOf(
                KlassifisertInntekt(
                    beløpPerMnd, InntektKlasse.FANGST_FISKE)
            ))
        }
    }

    fun generate12MånederFangstOgFiskInntekt(): List<KlassifisertInntektMåned> {
        return generateArbeidsInntekt(1..12, BigDecimal(50000))
    }

    fun generate36MånederFangstOgFiskInntekt(): List<KlassifisertInntektMåned> {
        return generateArbeidsInntekt(1..36, BigDecimal(50000))
    }
}