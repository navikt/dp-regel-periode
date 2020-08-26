package no.nav.dagpenger.regel.periode

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.all
import no.nav.dagpenger.events.inntekt.v1.sumInntekt
import no.nav.dagpenger.grunnbelop.Grunnbeløp
import no.nav.dagpenger.grunnbelop.Regel
import no.nav.dagpenger.grunnbelop.forDato
import no.nav.dagpenger.grunnbelop.getGrunnbeløpForRegel
import java.math.BigDecimal
import java.time.LocalDate

data class Fakta(
    val inntekt: Inntekt,
    val bruktInntektsPeriode: InntektsPeriode? = null,
    val verneplikt: Boolean,
    val fangstOgFisk: Boolean,
    val beregningsDato: LocalDate,
    val grunnbeløp: BigDecimal = when {
        isThisGjusteringTest(beregningsDato) -> Grunnbeløp.GjusteringsTest.verdi
        else -> getGrunnbeløpForRegel(Regel.Minsteinntekt).forDato(beregningsDato).verdi
    },
    val lærling: Boolean,
    val grunnlagBeregningsregel: String
) {
    val filtrertInntekt = bruktInntektsPeriode?.let { inntektsPeriode ->
        inntekt.filterPeriod(
            inntektsPeriode.førsteMåned,
            inntektsPeriode.sisteMåned
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
    fun erlærling() = lærling && beregningsDato.erKoronaPeriode()
}

internal fun isThisGjusteringTest(
    beregningsdato: LocalDate
): Boolean {
    val isBeregningsDatoAfterGjustering = beregningsdato.isAfter(LocalDate.of(2020, 5, 1).minusDays(1))
    return configuration.features.gjustering() && isBeregningsDatoAfterGjustering
}
