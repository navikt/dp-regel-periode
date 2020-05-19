package no.nav.dagpenger.regel.periode

import com.fasterxml.jackson.databind.JsonNode
import de.huxhorn.sulky.ulid.ULID
import java.math.BigDecimal
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.inntekt.rpc.InntektHenter
import no.nav.dagpenger.regel.periode.LøsningService.Companion.BEREGNINGSDATO_NY_SRKIVEMÅTE
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asYearMonth

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

fun JsonNode.asULID(): ULID.Value = asText().let { ULID.parseULID(it) }

internal fun JsonMessage.toFakta(inntektHenter: InntektHenter): Fakta {
    val inntekt = this["inntektId"].asULID().let { runBlocking { inntektHenter.hentKlassifisertInntekt(it.toString()) } }
    val verneplikt = this[Application.AVTJENT_VERNEPLIKT].asBoolean(false)
    val beregningsDato = this[BEREGNINGSDATO_NY_SRKIVEMÅTE].asLocalDate()
    val lærling = this[Application.LÆRLING].asBoolean(false)
    val bruktInntektsPeriode = this[Application.BRUKT_INNTEKTSPERIODE].let {
        InntektsPeriode(
            førsteMåned = it["førsteMåned"].asYearMonth(),
            sisteMåned = it["sisteMåned"].asYearMonth()
        )
    }
    val fangstOgFisk = this[Application.FANGST_OG_FISK].asBoolean(false)

    val grunnlagBeregningsregel = this[Application.GRUNNLAG_RESULTAT][Application.BEREGNINGS_REGEL_GRUNNLAG].asText()

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
