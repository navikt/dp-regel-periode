package no.nav.dagpenger.regel.periode

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.inntekt.rpc.InntektHenter
import no.nav.dagpenger.regel.periode.Application.Companion.AVTJENT_VERNEPLIKT
import no.nav.dagpenger.regel.periode.Application.Companion.BRUKT_INNTEKTSPERIODE
import no.nav.dagpenger.regel.periode.Application.Companion.FANGST_OG_FISK
import no.nav.dagpenger.regel.periode.Application.Companion.GRUNNLAG_RESULTAT
import no.nav.dagpenger.regel.periode.Application.Companion.LÆRLING
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.nare.core.evaluations.Evaluering

class LøsningService(
    rapidsConnection: RapidsConnection,
    private val inntektHenter: InntektHenter
) : River.PacketListener {
    private val log = KotlinLogging.logger {}
    private val sikkerlogg = KotlinLogging.logger("tjenestekall")

    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf(PERIODE_BEHOV)) }
            validate { it.rejectKey("@løsning") }
            validate {
                it.requireKey(
                    "@id",
                    GRUNNLAG_RESULTAT,
                    BRUKT_INNTEKTSPERIODE,
                    BEREGNINGSDATO_NY_SRKIVEMÅTE,
                    VEDTAK_ID,
                    INNTEKT_ID
                )
            }
            validate { it.interestedIn(AVTJENT_VERNEPLIKT, FANGST_OG_FISK, LÆRLING) }
        }.register(this)
    }

    companion object {
        const val BEREGNINGSDATO_NY_SRKIVEMÅTE = "beregningsdato"
        const val PERIODE_BEHOV = "Periode"
        const val INNTEKT_ID = "InntektId"
        const val VEDTAK_ID = "vedtakId"
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        withLoggingContext(
            "behovId" to packet["@id"].asText(),
        "vedtakId" to packet[VEDTAK_ID].asText()
        ) {
            val fakta = packet.toFakta(inntektHenter)

            val evaluering: Evaluering = periode.evaluer(fakta)

            val periodeResultat: Int? = finnHøyestePeriodeFraEvaluering(evaluering)

            val subsumsjon = PeriodeSubsumsjon(
                ulidGenerator.nextULID(),
                ulidGenerator.nextULID(),
                Application.REGELIDENTIFIKATOR,
                periodeResultat ?: 0
            )

            packet["@løsning"] = mapOf(
                PERIODE_BEHOV to mapOf(
                Application.PERIODE_NARE_EVALUERING to evaluering,
                Application.PERIODE_RESULTAT to subsumsjon.toMap()
            ))

            context.send(packet.toJson())
        }
    }

    override fun onError(problems: MessageProblems, context: RapidsConnection.MessageContext) {
        log.info { problems.toString() }
        sikkerlogg.info { problems.toExtendedReport() }
    }
}
