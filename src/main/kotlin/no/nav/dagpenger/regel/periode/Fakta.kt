package no.nav.dagpenger.regel.periode

import no.nav.dagpenger.events.inntekt.v1.Inntekt
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
    val inntektsListe = bruktInntektsPeriode?.let {
        filterBruktInntekt(inntekt.inntektsListe, bruktInntektsPeriode)
    } ?: inntekt.inntektsListe

    val arbeidsinntektSiste12 = sumArbeidInntekt(inntektsListe, senesteInntektsmåned, 11)
    val arbeidsinntektSiste36 = sumArbeidInntekt(inntektsListe, senesteInntektsmåned, 35)

    val inntektSiste12inkludertFangstOgFiske = arbeidsinntektSiste12 + sumNæringsInntekt(inntektsListe, senesteInntektsmåned, 11)
    val inntektSiste36inkludertFangstOgFiske = arbeidsinntektSiste36 + sumNæringsInntekt(inntektsListe, senesteInntektsmåned, 35)
}