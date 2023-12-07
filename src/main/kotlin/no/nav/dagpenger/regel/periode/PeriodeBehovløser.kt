package no.nav.dagpenger.regel.periode

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.NarePrometheus
import no.nav.dagpenger.regel.periode.Evalueringer.finnHøyestePeriodeFraEvaluering
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.nare.core.evaluations.Evaluering
import java.net.URI

private val sikkerLogg = KotlinLogging.logger("tjenestekall")

class PeriodeBehovløser(rapidsConnection: RapidsConnection) : River.PacketListener {
    companion object {
        val LÆRLING = "lærling"
        val REGELIDENTIFIKATOR = "Periode.v1"
        val PERIODE_RESULTAT = "periodeResultat"
        val INNTEKT = "inntektV1"
        val AVTJENT_VERNEPLIKT = "harAvtjentVerneplikt"
        val FANGST_OG_FISKE = "oppfyllerKravTilFangstOgFisk"
        val BRUKT_INNTEKTSPERIODE = "bruktInntektsPeriode"
        val GRUNNLAG_RESULTAT = "grunnlagResultat"
        val GRUNNLAG_BEREGNINGSREGEL = "beregningsregel"
        val BEREGNINGSDATO = "beregningsDato"
        val REGELVERKSDATO = "regelverksdato"
        val BEHOV_ID = "behovId"
        val PROBLEM = "system_problem"
        internal val rapidFilter: River.() -> Unit = {
            validate { it.requireKey(BEHOV_ID) }
            validate { it.requireKey(INNTEKT, GRUNNLAG_RESULTAT, BEREGNINGSDATO) }
            validate {
                it.interestedIn(
                    AVTJENT_VERNEPLIKT,
                    REGELVERKSDATO,
                    LÆRLING,
                    BRUKT_INNTEKTSPERIODE,
                    FANGST_OG_FISKE,
                    GRUNNLAG_BEREGNINGSREGEL,
                )
            }
            validate { it.rejectKey(PERIODE_RESULTAT) }
        }
    }

    init {
        River(rapidsConnection).apply(rapidFilter).register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        withLoggingContext("behovId" to packet[BEHOV_ID].asText()) {
            try {
                sikkerLogg.info("Mottok behov for beregning av periode: ${packet.toJson()}")
                val fakta = packetToFakta(packet, GrunnbeløpStrategy())
                val evaluering: Evaluering = narePrometheus.tellEvaluering { periode.evaluer(fakta) }
                val periodeResultat: Int? =
                    finnHøyestePeriodeFraEvaluering(evaluering, fakta.grunnlagBeregningsregel).also {
                        tellHvilkenPeriodeSomBleGitt(it)
                    }
                val subsumsjon =
                    PeriodeSubsumsjon(
                        ulidGenerator.nextULID(),
                        ulidGenerator.nextULID(),
                        REGELIDENTIFIKATOR,
                        periodeResultat ?: 0,
                    )

                packet[PERIODE_RESULTAT] = subsumsjon.toMap()
                context.publish(packet.toJson())
                sikkerLogg.info { "Løste behov for beregning av periode: $periodeResultat med fakta $fakta" }
            } catch (e: Exception) {
                val problem =
                    Problem(
                        type = URI("urn:dp:error:regel"),
                        title = "Ukjent feil ved bruk av perioderegel",
                        instance = URI("urn:dp:regel:periode"),
                    )
                packet[PROBLEM] = problem.toMap
                context.publish(packet.toJson())
                throw e
            }
        }
    }
}

private val narePrometheus = NarePrometheus(CollectorRegistry.defaultRegistry)

private fun tellHvilkenPeriodeSomBleGitt(periodeResultat: Int?) {
    periodeGittCounter.labels(periodeResultat.toString()).inc()
}

private val periodeGittCounter =
    Counter.build()
        .name("utfall_dagpengeperiode")
        .labelNames("periode")
        .help("Hvor lang dagpengeperiode ble resultat av subsumsjonen")
        .register()
