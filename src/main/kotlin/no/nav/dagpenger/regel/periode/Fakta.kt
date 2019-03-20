package no.nav.dagpenger.regel.periode

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import java.math.BigDecimal
import java.time.YearMonth

data class Fakta(
    val inntekt: Inntekt,
    val senesteInntektsmåned: YearMonth,
    val bruktInntektsPeriode: InntektsPeriode? = null,
    val verneplikt: Boolean,
    val fangstOgFisk: Boolean,
    val grunnbeløp: BigDecimal = BigDecimal(96883)
) {

    val filtrertInntekt = bruktInntektsPeriode?.let { inntektsPeriode -> inntekt.filterPeriod(inntektsPeriode.førsteMåned, inntektsPeriode.sisteMåned) } ?: inntekt

    val arbeidsinntektSiste12 = filtrertInntekt.sumInntektLast12Months(listOf(InntektKlasse.ARBEIDSINNTEKT), senesteInntektsmåned)
    val arbeidsinntektSiste36 = filtrertInntekt.sumInntektLast36Months(listOf(InntektKlasse.ARBEIDSINNTEKT), senesteInntektsmåned)

    val inntektSiste12inkludertFangstOgFiske = arbeidsinntektSiste12 + filtrertInntekt.sumInntektLast12Months(listOf(InntektKlasse.FANGST_FISKE), senesteInntektsmåned)
    val inntektSiste36inkludertFangstOgFiske = arbeidsinntektSiste36 + filtrertInntekt.sumInntektLast12Months(listOf(InntektKlasse.FANGST_FISKE), senesteInntektsmåned)
}