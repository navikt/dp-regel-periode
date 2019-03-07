package no.nav.dagpenger.regel.periode

import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth
import kotlin.test.assertEquals

class InnvilgetPeriodeTest {

    fun generate36MånederArbeidsInntekt(): List<KlassifisertInntektMåned> {
        return (1..36).toList().map {
            KlassifisertInntektMåned(YearMonth.of(2019, 1).minusMonths(it.toLong()), listOf(KlassifisertInntekt(BigDecimal(50000), InntektKlasse.ARBEIDSINNTEKT)))
        }
    }

    @Test
    fun `Skal få periode på 26 uker ved verneplikt`() {
        val resultat = finnPeriode(
            true,
            Inntekt("123", emptyList()),
            YearMonth.of(2019, 4))
        assertEquals(26, resultat)
    }

    @Test
    fun `Skal få periode på 0 uker uten inntekt`() {
        val resultat = finnPeriode(
            false,
            Inntekt("123", emptyList()),
            YearMonth.of(2019, 4))
        assertEquals(0, resultat)
    }

    @Test
    fun `Skal få periode på 52 uker med mellom 1,5 og 2G inntekt siste 12 mnd`() {

        val inntektsListe = (1..36).toList().map {
            KlassifisertInntektMåned(
                YearMonth.now().minusMonths(it.toLong()),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(14500),
                        InntektKlasse.ARBEIDSINNTEKT)))
        }

        val resultat = finnPeriode(
            false,
            Inntekt("123", inntektsListe),
            YearMonth.of(2019, 4))
        assertEquals(52, resultat)
    }

    @Test
    fun `Hvis tidligere brukte inntekter finnes skal de ikke taes med`() {
        val resultat = finnPeriode(
            false,
            Inntekt("123", generate36MånederArbeidsInntekt()),
            YearMonth.of(2019, 2),
            InntektsPeriode(YearMonth.of(2015, 1), YearMonth.of(2018, 10 ))
        )

        assertEquals(0, resultat)
    }
}