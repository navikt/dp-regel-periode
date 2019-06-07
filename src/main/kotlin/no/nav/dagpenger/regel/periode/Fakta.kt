package no.nav.dagpenger.regel.periode

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.all
import no.nav.dagpenger.events.inntekt.v1.sumInntekt
import no.nav.dagpenger.grunnbelop.getGrunnbeløpForDato
import java.math.BigDecimal
import java.time.LocalDate

data class Fakta(
    val inntekt: Inntekt,
    val bruktInntektsPeriode: InntektsPeriode? = null,
    val verneplikt: Boolean,
    val fangstOgFisk: Boolean,
    val beregningsDato: LocalDate,
    val grunnbeløp: BigDecimal = getGrunnbeløpForDato(beregningsDato).verdi,
    val grunnlagBeregningsregel: String
) {

    val filtrertInntekt = bruktInntektsPeriode?.let { inntektsPeriode -> inntekt.filterPeriod(inntektsPeriode.førsteMåned, inntektsPeriode.sisteMåned) } ?: inntekt

    val splitInntekt = filtrertInntekt.splitIntoInntektsPerioder()

    val arbeidsinntektSiste12 = splitInntekt.first.sumInntekt(listOf(InntektKlasse.ARBEIDSINNTEKT))
    val arbeidsinntektSiste36 = splitInntekt.all().sumInntekt(listOf(InntektKlasse.ARBEIDSINNTEKT))

    val inntektSiste12inkludertFangstOgFiske = arbeidsinntektSiste12 + splitInntekt.first.sumInntekt(listOf(InntektKlasse.FANGST_FISKE))
    val inntektSiste36inkludertFangstOgFiske = arbeidsinntektSiste36 + splitInntekt.all().sumInntekt(listOf(InntektKlasse.FANGST_FISKE))
}