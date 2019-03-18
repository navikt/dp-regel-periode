package no.nav.dagpenger.regel.periode

import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth
import kotlin.test.assertEquals

class FilterBruktInntektTest {

    fun generate36MånederInntekt(): List<KlassifisertInntektMåned> {

        return (1..36).toList().map {
            KlassifisertInntektMåned(
                YearMonth.of(2019, 1).minusMonths(it.toLong()),
                listOf(KlassifisertInntekt(BigDecimal(1000), InntektKlasse.ARBEIDSINNTEKT)))
        }
    }

    @Test
    fun ` Should filter based on brukt inntekt`() {
        val bruktInntekt = InntektsPeriode(
            YearMonth.of(2018, 3),
            YearMonth.of(2018, 5))

        val inntektsListe = generate36MånederInntekt()

        val filteredInntekt = filterBruktInntekt(inntektsListe, bruktInntekt)

        assertEquals(inntektsListe.size - 3, filteredInntekt.size)
        assertFalse(filteredInntekt.any { it.årMåned < bruktInntekt.sisteMåned && it.årMåned > bruktInntekt.førsteMåned })
    }
}