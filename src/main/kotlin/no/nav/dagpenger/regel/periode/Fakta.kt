package no.nav.dagpenger.regel.periode

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.all
import no.nav.dagpenger.events.inntekt.v1.sumInntekt
import java.math.BigDecimal
import java.time.YearMonth

data class Fakta(
    val inntekt: Inntekt,
    val senesteInntektsmåned: YearMonth,
    val bruktInntektsPeriode: InntektsPeriode? = null,
    val verneplikt: Boolean,
    val fangstOgFisk: Boolean,
    val grunnbeløp: BigDecimal = BigDecimal(96883),
    val grunnlagBeregningsregel: String
) {

    val filtrertInntekt = bruktInntektsPeriode?.let { inntektsPeriode -> inntekt.filterPeriod(inntektsPeriode.førsteMåned, inntektsPeriode.sisteMåned) } ?: inntekt

    val splitInntekt = filtrertInntekt.splitIntoInntektsPerioder(senesteInntektsmåned)

    val arbeidsinntektSiste12 = splitInntekt.first.sumInntekt(listOf(InntektKlasse.ARBEIDSINNTEKT))
    val arbeidsinntektSiste36 = splitInntekt.all().sumInntekt(listOf(InntektKlasse.ARBEIDSINNTEKT))

    val inntektSiste12inkludertFangstOgFiske = arbeidsinntektSiste12 + splitInntekt.first.sumInntekt(listOf(InntektKlasse.FANGST_FISKE))
    val inntektSiste36inkludertFangstOgFiske = arbeidsinntektSiste36 + splitInntekt.all().sumInntekt(listOf(InntektKlasse.FANGST_FISKE))
}