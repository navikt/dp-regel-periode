package no.nav.dagpenger.regel.periode

import de.huxhorn.sulky.ulid.ULID
import io.prometheus.client.CollectorRegistry
import no.nav.NarePrometheus
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.Problem
import no.nav.dagpenger.streams.KafkaCredential
import no.nav.dagpenger.streams.River
import no.nav.dagpenger.streams.streamConfig
import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.evaluations.Resultat
import org.apache.kafka.streams.kstream.Predicate
import java.net.URI
import java.util.Properties

private val narePrometheus = NarePrometheus(CollectorRegistry.defaultRegistry)

class Periode(private val env: Environment) : River() {

    override val SERVICE_APP_ID: String = "dagpenger-regel-periode"
    override val HTTP_PORT: Int = env.httpPort ?: super.HTTP_PORT

    private val ulidGenerator = ULID()

    companion object {
        val REGELIDENTIFIKATOR = "Periode.v1"
        val PERIODE_RESULTAT = "periodeResultat"
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
            Predicate { _, packet -> !packet.hasField(PERIODE_RESULTAT) })
    }

    override fun onPacket(packet: Packet): Packet {

        val fakta = packetToFakta(packet)

        val evaluering: Evaluering = narePrometheus.tellEvaluering { periode.evaluer(fakta) }

        val periodeResultat: Int? = finnHøyestePeriodeFraEvaluering(evaluering, fakta)

        val subsumsjon = PeriodeSubsumsjon(
            ulidGenerator.nextULID(),
            ulidGenerator.nextULID(),
            REGELIDENTIFIKATOR,
            periodeResultat ?: 0
        )

        packet.putValue(PERIODE_RESULTAT, subsumsjon.toMap())
        return packet
    }

    override fun getConfig(): Properties {
        val props = streamConfig(
            appId = SERVICE_APP_ID,
            bootStapServerUrl = env.bootstrapServersUrl,
            credential = KafkaCredential(env.username, env.password)
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

fun finnHøyestePeriodeFraEvaluering(evaluering: Evaluering, fakta: Fakta): Int? {

    return if (fakta.grunnlagBeregningsregel == "Verneplikt") {
        26
    } else {
        val periodeResultat: Int? =
            evaluering.children.filter { it.resultat == Resultat.JA }.flatMap { mapEvalureringResultatToInt(it) }.max()
        periodeResultat
    }
}

fun main(args: Array<String>) {
    val service = Periode(Environment())
    service.start()
}