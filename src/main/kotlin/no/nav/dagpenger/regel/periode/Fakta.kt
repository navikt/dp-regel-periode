package no.nav.dagpenger.regel.periode

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.all
import no.nav.dagpenger.events.inntekt.v1.sumInntekt
import no.nav.dagpenger.grunnbelop.Grunnbeløp
import no.nav.dagpenger.grunnbelop.Regel
import no.nav.dagpenger.grunnbelop.forDato
import no.nav.dagpenger.grunnbelop.getGrunnbeløpForRegel
import no.nav.dagpenger.regel.periode.Application.Companion.unleash
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month

data class Fakta(
    val inntekt: Inntekt,
    val bruktInntektsPeriode: InntektsPeriode? = null,
    val verneplikt: Boolean,
    val fangstOgFisk: Boolean,
    val beregningsDato: LocalDate,
    val regelverksdato: LocalDate,
    val grunnbeløp: BigDecimal =
        when {
            isThisGjusteringTest(beregningsDato) -> Grunnbeløp.GjusteringsTest.verdi
            else -> getGrunnbeløpForRegel(Regel.Minsteinntekt).forDato(beregningsDato).verdi
        },
    val lærling: Boolean,
    val grunnlagBeregningsregel: String,
) {
    val filtrertInntekt =
        bruktInntektsPeriode?.let { inntektsPeriode ->
            inntekt.filterPeriod(
                inntektsPeriode.førsteMåned,
                inntektsPeriode.sisteMåned,
            )
        } ?: inntekt
    val splitInntekt = filtrertInntekt.splitIntoInntektsPerioder()

    val arbeidsinntektSiste12 = splitInntekt.first.sumInntekt(listOf(InntektKlasse.ARBEIDSINNTEKT))
    val arbeidsinntektSiste36 = splitInntekt.all().sumInntekt(listOf(InntektKlasse.ARBEIDSINNTEKT))

    val inntektSiste12inkludertFangstOgFiske =
        arbeidsinntektSiste12 + splitInntekt.first.sumInntekt(listOf(InntektKlasse.FANGST_FISKE))
    val inntektSiste36inkludertFangstOgFiske =
        arbeidsinntektSiste36 + splitInntekt.all().sumInntekt(listOf(InntektKlasse.FANGST_FISKE))

    fun erSærregel(): Boolean = erlærling()

    fun erlærling() = lærling && regelverksdato.erKoronaPeriode()

    private fun LocalDate.erKoronaPeriode() = førsteKoronaperiode() || andreKoronaperiode()

    private fun LocalDate.førsteKoronaperiode() = this in (LocalDate.of(2020, Month.MARCH, 20)..LocalDate.of(2021, Month.SEPTEMBER, 30))

    private fun LocalDate.andreKoronaperiode() = this in (LocalDate.of(2021, Month.DECEMBER, 15)..LocalDate.of(2022, Month.MARCH, 31))
}

internal fun isThisGjusteringTest(beregningsdato: LocalDate): Boolean {
    val gVirkning = LocalDate.of(2023, 5, 15)
    val isBeregningsDatoAfterGjustering = beregningsdato.isAfter(gVirkning.minusDays(1))
    return unleash.isEnabled(GJUSTERING_TEST) && isBeregningsDatoAfterGjustering
}
