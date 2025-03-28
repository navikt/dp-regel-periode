package no.nav.dagpenger.regel.periode

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers.isMissingOrNull
import no.nav.dagpenger.inntekt.v1.Inntekt
import no.nav.dagpenger.regel.periode.FaktaMapper.avtjentVerneplikt
import no.nav.dagpenger.regel.periode.FaktaMapper.beregningsdato
import no.nav.dagpenger.regel.periode.FaktaMapper.bruktInntektsperiode
import no.nav.dagpenger.regel.periode.FaktaMapper.fangstOgFiske
import no.nav.dagpenger.regel.periode.FaktaMapper.grunnlagBeregningsregel
import no.nav.dagpenger.regel.periode.FaktaMapper.inntekt
import no.nav.dagpenger.regel.periode.FaktaMapper.lærling
import no.nav.dagpenger.regel.periode.FaktaMapper.regelverksdato
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.AVTJENT_VERNEPLIKT
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.BRUKT_INNTEKTSPERIODE
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.FANGST_OG_FISKE
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.GRUNNLAG_BEREGNINGSREGEL
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.GRUNNLAG_RESULTAT
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.INNTEKT
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.LÆRLING
import no.nav.dagpenger.regel.periode.PeriodeBehovløser.Companion.REGELVERKSDATO

internal fun packetToFakta(
    packet: JsonMessage,
    grunnbeløpStrategy: GrunnbeløpStrategy,
): Fakta {
    val verneplikt = packet.avtjentVerneplikt()
    val inntekt: Inntekt = packet.inntekt()
    val beregningsdato = packet.beregningsdato()
    val regelverksdato = packet.regelverksdato() ?: beregningsdato
    val lærling = packet.lærling()

    val bruktInntektsperiode = packet.bruktInntektsperiode()

    val fangstOgFisk = packet.fangstOgFiske()

    val grunnlagBeregningsregel = packet.grunnlagBeregningsregel()

    return Fakta(
        inntekt = inntekt,
        bruktInntektsperiode = bruktInntektsperiode,
        verneplikt = verneplikt,
        fangstOgFiske = fangstOgFisk,
        grunnlagBeregningsregel = grunnlagBeregningsregel,
        beregningsdato = beregningsdato,
        regelverksdato = regelverksdato,
        lærling = lærling,
        grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
    )
}

object FaktaMapper {
    fun JsonMessage.grunnlagBeregningsregel(): String {
        try {
            return this[GRUNNLAG_RESULTAT][GRUNNLAG_BEREGNINGSREGEL].asText()
        } catch (e: Exception) {
            throw ManglendeGrunnlagBeregningsregelException()
        }
    }

    fun JsonMessage.fangstOgFiske() =
        when (this.harVerdi(FANGST_OG_FISKE)) {
            true -> this[FANGST_OG_FISKE].toBooleanStrict()
            false -> false
        }

    fun JsonMessage.lærling() =
        when (this.harVerdi(LÆRLING)) {
            true -> this[LÆRLING].toBooleanStrict()
            false -> false
        }

    fun JsonMessage.beregningsdato() = this[BEREGNINGSDATO].asLocalDate()

    fun JsonMessage.regelverksdato() =
        when (this.harVerdi(REGELVERKSDATO)) {
            true -> this[REGELVERKSDATO].asLocalDate()
            false -> this.beregningsdato()
        }

    fun JsonMessage.avtjentVerneplikt() =
        when (this.harVerdi(AVTJENT_VERNEPLIKT)) {
            true -> this[AVTJENT_VERNEPLIKT].toBooleanStrict()
            false -> false
        }

    fun JsonMessage.bruktInntektsperiode(): InntektsPeriode? {
        val bruktInntektsperiode = this[BRUKT_INNTEKTSPERIODE]
        return when (this.harVerdi(BRUKT_INNTEKTSPERIODE)) {
            true -> jsonMapper.convertValue(bruktInntektsperiode, InntektsPeriode::class.java)
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

    private fun JsonNode.toBooleanStrict() = this.asText().toBooleanStrict()

    class ManglendeInntektException : RuntimeException("Mangler inntekt")

    class ManglendeGrunnlagBeregningsregelException : RuntimeException()

    private fun JsonMessage.harVerdi(field: String) = !this[field].isMissingOrNull()
}
