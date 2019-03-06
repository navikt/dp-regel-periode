package no.nav.dagpenger.regel.periode

import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth
import kotlin.test.assertEquals

class SumInntekterTest {

    fun generateSiste36MånederArbeidsInntekt(): List<KlassifisertInntektMåned> {

        return (1..36).toList().map {
            KlassifisertInntektMåned(
                YearMonth.now().minusMonths(it.toLong()),
                listOf(
                    KlassifisertInntekt(BigDecimal(1000),
                    InntektKlasse.ARBEIDSINNTEKT)))
        }
    }

    fun generateSiste36MånederNæringsInntekt(): List<KlassifisertInntektMåned> {

        return (1..36).toList().map {
            KlassifisertInntektMåned(
                YearMonth.now().minusMonths(it.toLong()),
                listOf(
                    KlassifisertInntekt(BigDecimal(1000),
                    InntektKlasse.NÆRINGSINNTEKT)))
        }
    }

    @Test
    fun ` should add Arbeidsinntekt in sumSiste12 `() {

        assertEquals(BigDecimal(12000), sumArbeidsInntekt(
            Inntekt(
                "123",
                generateSiste36MånederArbeidsInntekt()),
            YearMonth.now().minusMonths(1),
            11))
    }

    @Test
    fun ` should not add næringsinntekt in sumSiste12 when there is no fangst og fisk `() {

        assertEquals(BigDecimal(0), sumArbeidsInntekt(
            Inntekt(
                "123",
                generateSiste36MånederNæringsInntekt()),
            YearMonth.now().minusMonths(1),
            11))
    }
}