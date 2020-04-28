package no.nav.dagpenger.regel.periode

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.regel.periode.Periode.Companion.BRUKT_INNTEKTSPERIODE
import no.nav.dagpenger.regel.periode.Periode.Companion.GRUNNLAG_RESULTAT
import no.nav.dagpenger.regel.periode.Periode.Companion.INNTEKT
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

class LøsningService(
    rapidsConnection: RapidsConnection
) : River.PacketListener {
    private val log = KotlinLogging.logger {}
    private val sikkerlogg = KotlinLogging.logger("tjenestekall")

    init {
        River(rapidsConnection).apply {
            validate { it.requireAll("@behov", listOf(PERIODE_BEHOV)) }
            validate { it.forbid("@løsning") }
            validate {
                it.requireKey(
                    "@id",
                    GRUNNLAG_RESULTAT,
                    BRUKT_INNTEKTSPERIODE,
                    BEREGNINGSDATO_NY_SRKIVEMÅTE,
                    INNTEKT
                )
            }
        }.register(this)
    }

    companion object {
        const val BEREGNINGSDATO_NY_SRKIVEMÅTE = "beregningsdato"
        const val PERIODE_BEHOV = "Periode"
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        withLoggingContext(
            "behovId" to packet["@id"].asText()
        ) {
            packet["@løsning"] = mapOf("removeme" to "dead")
            context.send(packet.toJson())
        }
    }

    override fun onError(problems: MessageProblems, context: RapidsConnection.MessageContext) {
        log.info { problems.toString() }
        sikkerlogg.info { problems.toExtendedReport() }
    }
}
