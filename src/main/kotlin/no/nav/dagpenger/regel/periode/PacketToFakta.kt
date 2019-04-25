package no.nav.dagpenger.regel.periode

import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import java.time.YearMonth

private val inntektAdapter =
    moshiInstance.adapter<no.nav.dagpenger.events.inntekt.v1.Inntekt>(no.nav.dagpenger.events.inntekt.v1.Inntekt::class.java)

private val bruktInntektsPeriodeAdapter = moshiInstance.adapter<InntektsPeriode>(InntektsPeriode::class.java)

internal fun packetToFakta(packet: Packet): Fakta {
    val verneplikt = packet.getNullableBoolean(Periode.AVTJENT_VERNEPLIKT) ?: false
    val inntekt: Inntekt = packet.getObjectValue(Periode.INNTEKT) { inntektAdapter.fromJsonValue(it)!! }
    val senesteInntektsmåned = YearMonth.parse(packet.getStringValue(Periode.SENESTE_INNTEKTSMÅNED))

    val bruktInntektsPeriode =
        packet.getNullableObjectValue(Periode.BRUKT_INNTEKTSPERIODE, bruktInntektsPeriodeAdapter::fromJsonValue)

    val fangstOgFisk = packet.getNullableBoolean(Periode.FANGST_OG_FISK) ?: false

    val grunnlagBeregningsregel = packet.getMapValue(Periode.GRUNNLAG_RESULTAT)[Periode.BEREGNINGS_REGEL_GRUNNLAG].toString()

    return Fakta(
        inntekt,
        senesteInntektsmåned,
        bruktInntektsPeriode,
        verneplikt,
        fangstOgFisk,
        grunnlagBeregningsregel = grunnlagBeregningsregel
    )
}