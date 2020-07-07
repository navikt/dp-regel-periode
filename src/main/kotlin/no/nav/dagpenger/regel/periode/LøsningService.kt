package no.nav.dagpenger.regel.periode

import com.fasterxml.jackson.databind.JsonNode
import de.huxhorn.sulky.ulid.ULID
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.inntekt.rpc.InntektHenter
import no.nav.dagpenger.regel.periode.Application.Companion.AVTJENT_VERNEPLIKT
import no.nav.dagpenger.regel.periode.Application.Companion.BEREGNINGS_REGEL_GRUNNLAG
import no.nav.dagpenger.regel.periode.Application.Companion.BRUKT_INNTEKTSPERIODE
import no.nav.dagpenger.regel.periode.Application.Companion.FANGST_OG_FISK
import no.nav.dagpenger.regel.periode.Application.Companion.GRUNNLAG_RESULTAT
import no.nav.dagpenger.regel.periode.Application.Companion.LÆRLING
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asYearMonth
import no.nav.nare.core.evaluations.Evaluering

class LøsningService(
    rapidsConnection: RapidsConnection,
    private val inntektHenter: InntektHenter
) : River.PacketListener {
    private val log = KotlinLogging.logger {}
    private val sikkerlogg = KotlinLogging.logger("tjenestekall")

    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf("Periode")) }
            validate { it.rejectKey("@løsning") }
            validate {
                it.requireKey(
                    "@id",
                    GRUNNLAG_RESULTAT,
                    "beregningsdato",
                    "vedtakId",
                    "inntektId"
                )
            }
            validate {
                it.interestedIn(
                    AVTJENT_VERNEPLIKT,
                    BRUKT_INNTEKTSPERIODE,
                    FANGST_OG_FISK,
                    LÆRLING
                )
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        withLoggingContext(
            "behovId" to packet["@id"].asText(),
            "vedtakId" to packet["vedtakId"].asText()
        ) {
            val fakta = packet.toFakta(inntektHenter)
            val evaluering: Evaluering = periode.evaluer(fakta)
            val periodeResultat = finnHøyestePeriodeFraEvaluering(evaluering) ?: 0

            packet["@løsning"] = mapOf(
                "Periode" to mapOf(
                    "periodeAntallUker" to periodeResultat
                    // TODO: Implementer støtte for å svare med regel brukt
                )
            )

            log.info { "løser behov for ${packet["vedtakId"].asText()}" }

            context.send(packet.toJson())
        }
    }

    override fun onError(problems: MessageProblems, context: RapidsConnection.MessageContext) {
        log.info { problems.toString() }
        sikkerlogg.info { problems.toExtendedReport() }
    }
}

internal fun JsonNode.asULID(): ULID.Value = asText().let { ULID.parseULID(it) }

private fun JsonMessage.toFakta(inntektHenter: InntektHenter): Fakta = Fakta(
    inntekt = this["inntektId"].asULID().let { runBlocking { inntektHenter.hentKlassifisertInntekt(it.toString()) } },
    bruktInntektsPeriode = this[BRUKT_INNTEKTSPERIODE].let {
        when (!it.hasNonNull("førsteMåned")) {
            true -> null
            false -> InntektsPeriode(
                førsteMåned = it["førsteMåned"].asYearMonth(),
                sisteMåned = it["sisteMåned"].asYearMonth()
            )
        }
    },
    verneplikt = this[AVTJENT_VERNEPLIKT].asBoolean(false),
    fangstOgFisk = this[FANGST_OG_FISK].asBoolean(false),
    grunnlagBeregningsregel = this[GRUNNLAG_RESULTAT][BEREGNINGS_REGEL_GRUNNLAG].asText(),
    beregningsDato = this["beregningsdato"].asLocalDate(),
    lærling = this[LÆRLING].asBoolean(false)
)
