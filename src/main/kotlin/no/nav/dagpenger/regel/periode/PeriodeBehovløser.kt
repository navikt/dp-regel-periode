package no.nav.dagpenger.regel.periode

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter
import no.nav.NarePrometheus
import no.nav.dagpenger.regel.periode.Evalueringer.finnHøyestePeriodeFraEvaluering
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.nare.core.evaluations.Evaluering

class PeriodeBehovløser(rapidsConnection: RapidsConnection) : River.PacketListener {
    companion object {
        val LÆRLING = "lærling"
        val REGELIDENTIFIKATOR = "Periode.v1"
        val PERIODE_RESULTAT = "periodeResultat"
        val INNTEKT = "inntektV1"
        val AVTJENT_VERNEPLIKT = "harAvtjentVerneplikt"
        val FANGST_OG_FISK = "oppfyllerKravTilFangstOgFisk"
        val BRUKT_INNTEKTSPERIODE = "bruktInntektsPeriode"
        val GRUNNLAG_RESULTAT = "grunnlagResultat"
        val BEREGNINGSREGEL_GRUNNLAG = "beregningsregel"
        val BEREGNINGSDATO = "beregningsDato"
        val REGELVERKSDATO = "regelverksdato"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.requireKey(INNTEKT, GRUNNLAG_RESULTAT, BEREGNINGSDATO) }
            validate { it.interestedIn(AVTJENT_VERNEPLIKT, REGELVERKSDATO, LÆRLING, BRUKT_INNTEKTSPERIODE, FANGST_OG_FISK) }
            validate { it.rejectKey(PERIODE_RESULTAT) }
        }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
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
    }
}

// Prometheus stuff
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
