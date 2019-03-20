package no.nav.dagpenger.regel.periode

import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import java.math.BigDecimal
import java.time.YearMonth

class SumInntekterTest {

    fun generateSiste36MånederArbeidsInntekt(): List<KlassifisertInntektMåned> {

        return (1..36).toList().map {
            KlassifisertInntektMåned(
                YearMonth.now().minusMonths(it.toLong()),
                listOf(
                    KlassifisertInntekt(BigDecimal(1000),
                    InntektKlasse.ARBEIDSINNTEKT)
                ))
        }
    }

    fun generateSiste36MånederNæringsInntekt(): List<KlassifisertInntektMåned> {

        return (1..36).toList().map {
            KlassifisertInntektMåned(
                YearMonth.now().minusMonths(it.toLong()),
                listOf(
                    KlassifisertInntekt(BigDecimal(1000),
                    InntektKlasse.FANGST_FISKE)))
        }
    }
}