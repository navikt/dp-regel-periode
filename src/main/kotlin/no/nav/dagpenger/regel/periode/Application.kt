package no.nav.dagpenger.regel.periode

import com.squareup.moshi.JsonAdapter
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter
import no.nav.NarePrometheus
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.Problem
import no.nav.dagpenger.streams.River
import no.nav.dagpenger.streams.streamConfig
import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.evaluations.Resultat
import org.apache.kafka.streams.kstream.Predicate
import java.net.URI
import java.time.LocalDateTime
import java.util.Properties

private val narePrometheus = NarePrometheus(CollectorRegistry.defaultRegistry)
private val periodeGittCounter = Counter.build()
    .name("utfall_dagpengeperiode")
    .labelNames("periode")
    .help("Hvor lang dagpengeperiode ble resultat av subsumsjonen")
    .register()

class Application(
    private val config: Configuration
) : River(config.behovTopic) {
    override val SERVICE_APP_ID: String = config.application.id
    override val HTTP_PORT: Int = config.application.httpPort

    val jsonAdapterEvaluering: JsonAdapter<Evaluering> = moshiInstance.adapter(Evaluering::class.java)

    companion object {
        val LÆRLING = "lærling"
        val REGELIDENTIFIKATOR = "Periode.v1"
        val PERIODE_RESULTAT = "periodeResultat"
        val PERIODE_NARE_EVALUERING = "periodeNareEvaluering"
        val INNTEKT = "inntektV1"
        val AVTJENT_VERNEPLIKT = "harAvtjentVerneplikt"
        val FANGST_OG_FISK = "oppfyllerKravTilFangstOgFisk"
        val BRUKT_INNTEKTSPERIODE = "bruktInntektsPeriode"
        val GRUNNLAG_RESULTAT = "grunnlagResultat"
        val BEREGNINGS_REGEL_GRUNNLAG = "beregningsregel"
        val BEREGNINGSDATO = "beregningsDato"
    }

    override fun filterPredicates(): List<Predicate<String, Packet>> {
        return listOf(
            Predicate { _, packet -> packet.hasField(INNTEKT) },
            Predicate { _, packet -> packet.hasField(GRUNNLAG_RESULTAT) },
            Predicate { _, packet -> packet.hasField(BEREGNINGSDATO) },
            Predicate { _, packet -> !packet.hasField(PERIODE_RESULTAT) }
        )
    }

    override fun onPacket(packet: Packet): Packet {

        val started: LocalDateTime? =
            packet.getNullableStringValue("system_started")
                ?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }

        if (started?.isBefore(LocalDateTime.now().minusSeconds(30)) == true) {
            throw RuntimeException("Denne pakka er for gammal!")
        }

        val fakta = packetToFakta(packet)

        val evaluering: Evaluering = narePrometheus.tellEvaluering { periode.evaluer(fakta) }

        val periodeResultat: Int? = finnHøyestePeriodeFraEvaluering(evaluering, fakta.grunnlagBeregningsregel)

        val subsumsjon = PeriodeSubsumsjon(
            ulidGenerator.nextULID(),
            ulidGenerator.nextULID(),
            REGELIDENTIFIKATOR,
            periodeResultat ?: 0
        )

        tellHvilkenPeriodeSomBleGitt(periodeResultat)

        packet.putValue(PERIODE_NARE_EVALUERING, jsonAdapterEvaluering.toJson(evaluering))
        packet.putValue(PERIODE_RESULTAT, subsumsjon.toMap())
        return packet
    }

    private fun tellHvilkenPeriodeSomBleGitt(periodeResultat: Int?) {
        periodeGittCounter.labels(periodeResultat.toString()).inc()
    }

    override fun getConfig(): Properties {
        val props = streamConfig(
            appId = SERVICE_APP_ID,
            bootStapServerUrl = config.kafka.brokers,
            credential = config.kafka.credential()
        )
        return props
    }

    override fun onFailure(packet: Packet, error: Throwable?): Packet {
        packet.addProblem(
            Problem(
                type = URI("urn:dp:error:regel"),
                title = "Ukjent feil ved bruk av perioderegel",
                instance = URI("urn:dp:regel:periode")
            )
        )
        return packet
    }
}

fun mapEvalureringResultatToInt(it: Evaluering): List<Int> {
    return if (it.children.isEmpty()) {
        listOf(it.begrunnelse.toInt())
    } else {
        it.children.flatMap { mapEvalureringResultatToInt(it) }
    }
}

fun finnHøyestePeriodeFraEvaluering(evaluering: Evaluering, beregningsregel: String): Int? {
    return if (beregningsregel == "Verneplikt") {
        26
    } else {
        return evaluering
            .children
            .filter { it.resultat == Resultat.JA }
            .flatMap { mapEvalureringResultatToInt(it) }
            .max()
    }
}

internal val configuration = Configuration()

fun main() {
    Application(configuration).start()
}
