package no.nav.dagpenger.regel.periode

import mu.KotlinLogging
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.regel.periode.FaktaMapper.avtjentVerneplikt
import no.nav.dagpenger.regel.periode.FaktaMapper.bruktInntektsPeriode
import no.nav.dagpenger.regel.periode.FaktaMapper.fangstOgFiske
import no.nav.dagpenger.regel.periode.FaktaMapper.grunnlagBeregningsregel
import no.nav.dagpenger.regel.periode.FaktaMapper.inntekt
import no.nav.dagpenger.regel.periode.FaktaMapper.lærling
import no.nav.dagpenger.regel.periode.FaktaMapper.regelverksdato
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.AVTJENT_VERNEPLIKT
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.BRUKT_INNTEKTSPERIODE
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.FANGST_OG_FISK
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.GRUNNLAG_BEREGNINGSREGEL
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.GRUNNLAG_RESULTAT
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.INNTEKT
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.LÆRLING
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.REGELVERKSDATO
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.isMissingOrNull

private val sikkerLogg = KotlinLogging.logger("tjenestekall")

internal fun packetToFakta(
    packet: JsonMessage,
    grunnbeløpStrategy: GrunnbeløpStrategy,
): Fakta {
    val verneplikt = packet.avtjentVerneplikt()
    val inntekt: Inntekt = packet.inntekt()
    val beregningsDato = packet[BEREGNINGSDATO].asLocalDate()
    val regelverksdato = packet.regelverksdato() ?: beregningsDato
    val lærling = packet.lærling()

    val bruktInntektsPeriode = packet.bruktInntektsPeriode()

    val fangstOgFisk = packet.fangstOgFiske()

    val grunnlagBeregningsregel = packet.grunnlagBeregningsregel()

    return Fakta(
        inntekt = inntekt,
        bruktInntektsPeriode = bruktInntektsPeriode,
        verneplikt = verneplikt,
        fangstOgFisk = fangstOgFisk,
        grunnlagBeregningsregel = grunnlagBeregningsregel,
        beregningsDato = beregningsDato,
        regelverksdato = regelverksdato,
        lærling = lærling,
        grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsDato),
    )
}

object FaktaMapper {
    fun JsonMessage.grunnlagBeregningsregel(): String {
        return this[GRUNNLAG_RESULTAT][GRUNNLAG_BEREGNINGSREGEL].asText()
    }

    fun JsonMessage.fangstOgFiske() =
        when (this.harVerdi(FANGST_OG_FISK)) {
            true -> this[FANGST_OG_FISK].asBoolean()
            false -> false
        }

    fun JsonMessage.lærling() =
        when (this.harVerdi(LÆRLING)) {
            true -> this[LÆRLING].asBoolean()
            false -> false
        }

    fun JsonMessage.regelverksdato() =
        when (this.harVerdi(REGELVERKSDATO)) {
            true -> this[REGELVERKSDATO].asLocalDate()
            false -> null
        }

    fun JsonMessage.avtjentVerneplikt() =
        when (this.harVerdi(AVTJENT_VERNEPLIKT)) {
            true -> this[AVTJENT_VERNEPLIKT].asBoolean()
            false -> false
        }

    fun JsonMessage.bruktInntektsPeriode(): InntektsPeriode? {
        val inntektsPerioder = this[BRUKT_INNTEKTSPERIODE]
        return when (this.harVerdi(BRUKT_INNTEKTSPERIODE)) {
            true -> jsonMapper.convertValue(inntektsPerioder, InntektsPeriode::class.java)
            false -> null
        }
    }

    fun JsonMessage.inntekt(): Inntekt {
        val inntekt = this[INNTEKT]
        return when (this.harVerdi(INNTEKT)) {
            true -> jsonMapper.convertValue(inntekt, Inntekt::class.java)
            false -> throw ManglendeInntektException()
        }
    }

    class ManglendeInntektException : RuntimeException("Mangler inntekt")

    private fun JsonMessage.harVerdi(field: String) = !this[field].isMissingOrNull()
}
