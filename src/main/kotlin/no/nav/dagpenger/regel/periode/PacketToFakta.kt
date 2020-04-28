package no.nav.dagpenger.regel.periode

import com.fasterxml.jackson.databind.JsonNode
import java.math.BigDecimal
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.periode.LøsningService.Companion.BEREGNINGSDATO_NY_SRKIVEMÅTE
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asYearMonth

private val inntektAdapter =
    moshiInstance.adapter<no.nav.dagpenger.events.inntekt.v1.Inntekt>(no.nav.dagpenger.events.inntekt.v1.Inntekt::class.java)

private val bruktInntektsPeriodeAdapter = moshiInstance.adapter<InntektsPeriode>(InntektsPeriode::class.java)

internal fun packetToFakta(packet: Packet): Fakta {
    val verneplikt = packet.getNullableBoolean(Periode.AVTJENT_VERNEPLIKT) ?: false
    val inntekt: Inntekt = packet.getObjectValue(Periode.INNTEKT) { inntektAdapter.fromJsonValue(it)!! }
    val beregningsDato = packet.getLocalDate(Periode.BEREGNINGSDATO)
    val lærling = packet.getNullableBoolean(Periode.LÆRLING) == true

    val bruktInntektsPeriode =
        packet.getNullableObjectValue(Periode.BRUKT_INNTEKTSPERIODE, bruktInntektsPeriodeAdapter::fromJsonValue)

    val fangstOgFisk = packet.getNullableBoolean(Periode.FANGST_OG_FISK) ?: false

    val grunnlagBeregningsregel =
        packet.getMapValue(Periode.GRUNNLAG_RESULTAT)[Periode.BEREGNINGS_REGEL_GRUNNLAG].toString()

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

internal fun JsonMessage.toFakta(): Fakta {
    val inntekt: Inntekt = this[Periode.INNTEKT].asInntekt()
    val verneplikt = this[Periode.AVTJENT_VERNEPLIKT].asBoolean(false)
    val beregningsDato = this[BEREGNINGSDATO_NY_SRKIVEMÅTE].asLocalDate()
    val lærling = this[Periode.LÆRLING].asBoolean(false)
    val bruktInntektsPeriode = this[Periode.BRUKT_INNTEKTSPERIODE].let {
        InntektsPeriode(
            førsteMåned = it["førsteMåned"].asYearMonth(),
            sisteMåned = it["sisteMåned"].asYearMonth()
        )
    }
    val fangstOgFisk = this[Periode.FANGST_OG_FISK].asBoolean(false)

    val grunnlagBeregningsregel = this[Periode.GRUNNLAG_RESULTAT][Periode.BEREGNINGS_REGEL_GRUNNLAG].asText()

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

private fun JsonNode.asInntekt() = Inntekt(
    inntektsId = this["inntektsId"].asText(),
    inntektsListe = this["inntektsListe"].map { it.asKlassifisertInntektMåned() },
    manueltRedigert = this["manueltRedigert"].asBoolean(false),
    sisteAvsluttendeKalenderMåned = this["sisteAvsluttendeKalenderMåned"].asYearMonth()
)

private fun JsonNode.asKlassifisertInntektMåned() =
    KlassifisertInntektMåned(
        årMåned = this["årMåned"].asYearMonth(),
        klassifiserteInntekter = this["klassifiserteInntekter"].map {
            KlassifisertInntekt(
                beløp = BigDecimal(
                    it["beløp"].asInt()
                ), inntektKlasse = InntektKlasse.valueOf(it["inntektKlasse"].asText())
            )
        })
