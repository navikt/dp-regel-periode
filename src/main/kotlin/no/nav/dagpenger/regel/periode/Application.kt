package no.nav.dagpenger.regel.periode

import com.squareup.moshi.JsonAdapter
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter
import no.finn.unleash.Unleash
import no.nav.NarePrometheus
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.Problem
import no.nav.dagpenger.streams.KafkaAivenCredentials
import no.nav.dagpenger.streams.River
import no.nav.dagpenger.streams.streamConfigAiven
import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.evaluations.Resultat
import org.apache.kafka.streams.kstream.Predicate
import java.net.URI
import java.time.LocalDateTime

private val narePrometheus = NarePrometheus(CollectorRegistry.defaultRegistry)
private val periodeGittCounter = Counter.build()
    .name("utfall_dagpengeperiode")
    .labelNames("periode")
    .help("Hvor lang dagpengeperiode ble resultat av subsumsjonen")
    .register()

class Application(
    private val config: Configuration
) : River(config.regelTopic) {
    override val SERVICE_APP_ID: String = config.application.id
    override val HTTP_PORT: Int = config.application.httpPort

    val jsonAdapterEvaluering: JsonAdapter<Evaluering> = moshiInstance.adapter(Evaluering::class.java)

    companion object {
        val LÆRLING = "lærling"
        val REGELIDENTIFIKATOR = "Periode.v1"
        val PERIODE_RESULTAT = "periodeResultat"
        val INNTEKT = "inntektV1"
        val AVTJENT_VERNEPLIKT = "harAvtjentVerneplikt"
        val FANGST_OG_FISK = "oppfyllerKravTilFangstOgFisk"
        val BRUKT_INNTEKTSPERIODE = "bruktInntektsPeriode"
        val GRUNNLAG_RESULTAT = "grunnlagResultat"
        val BEREGNINGS_REGEL_GRUNNLAG = "beregningsregel"
        val BEREGNINGSDATO = "beregningsDato"
        val REGELVERKSDATO = "regelverksdato"
        var unleash: Unleash = setupUnleash(configuration.application.unleashUrl)
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

        val periodeResultat: Int? = finnHøyestePeriodeFraEvaluering(evaluering, fakta.grunnlagBeregningsregel).also {
            tellHvilkenPeriodeSomBleGitt(it)
        }

        val subsumsjon = PeriodeSubsumsjon(
            ulidGenerator.nextULID(),
            ulidGenerator.nextULID(),
            REGELIDENTIFIKATOR,
            periodeResultat ?: 0
        )

        packet.putValue(PERIODE_RESULTAT, subsumsjon.toMap())
        return packet
    }

    private fun tellHvilkenPeriodeSomBleGitt(periodeResultat: Int?) {
        periodeGittCounter.labels(periodeResultat.toString()).inc()
    }

    override fun getConfig() = streamConfigAiven(
        appId = SERVICE_APP_ID,
        bootStapServerUrl = configuration.kafka.aivenBrokers,
        aivenCredentials = KafkaAivenCredentials()
    )

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
            .maxOrNull()
    }
}

internal val configuration = Configuration()

fun main() {
    Application(configuration).start()
}
