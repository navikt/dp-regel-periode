package no.nav.dagpenger.regel.periode

import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt

private val inntektAdapter =
    moshiInstance.adapter<no.nav.dagpenger.events.inntekt.v1.Inntekt>(no.nav.dagpenger.events.inntekt.v1.Inntekt::class.java)

private val bruktInntektsPeriodeAdapter = moshiInstance.adapter<InntektsPeriode>(InntektsPeriode::class.java)

internal fun packetToFakta(packet: Packet): Fakta {
    val verneplikt = packet.getNullableBoolean(Application.AVTJENT_VERNEPLIKT) ?: false
    val inntekt: Inntekt = packet.getObjectValue(Application.INNTEKT) { inntektAdapter.fromJsonValue(it)!! }
    val beregningsDato = packet.getLocalDate(Application.BEREGNINGSDATO)
    val lærling = packet.getNullableBoolean(Application.LÆRLING) == true

    val bruktInntektsPeriode =
        packet.getNullableObjectValue(Application.BRUKT_INNTEKTSPERIODE, bruktInntektsPeriodeAdapter::fromJsonValue)

    val fangstOgFisk = packet.getNullableBoolean(Application.FANGST_OG_FISK) ?: false

    val grunnlagBeregningsregel =
        packet.getMapValue(Application.GRUNNLAG_RESULTAT)[Application.BEREGNINGS_REGEL_GRUNNLAG].toString()

    return Fakta(
        inntekt = inntekt,
        bruktInntektsPeriode = bruktInntektsPeriode,
        verneplikt = verneplikt,
        fangstOgFisk = fangstOgFisk,
        grunnlagBeregningsregel = grunnlagBeregningsregel,
        beregningsDato = beregningsDato,
        lærling = lærling
    )
}
