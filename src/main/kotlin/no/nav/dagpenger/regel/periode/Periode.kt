package no.nav.dagpenger.regel.periode

import de.huxhorn.sulky.ulid.ULID
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.streams.KafkaCredential
import no.nav.dagpenger.streams.River
import no.nav.dagpenger.streams.streamConfig
import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.evaluations.Resultat
import org.apache.kafka.streams.kstream.Predicate
import org.apache.logging.log4j.LogManager
import java.time.YearMonth
import java.util.Properties

class Periode(private val env: Environment) : River() {
    private val logger = LogManager.getLogger()

    override val SERVICE_APP_ID: String = "dagpenger-regel-periode"
    override val HTTP_PORT: Int = env.httpPort ?: super.HTTP_PORT

    private val inntektAdapter =
        moshiInstance.adapter<no.nav.dagpenger.events.inntekt.v1.Inntekt>(no.nav.dagpenger.events.inntekt.v1.Inntekt::class.java)


    private val bruktInntektsPeriodeAdapter = moshiInstance.adapter<InntektsPeriode>(InntektsPeriode::class.java)
    private val ulidGenerator = ULID()

    companion object {
        val REGELIDENTIFIKATOR = "Periode.v1"
        val PERIODE_RESULTAT = "periodeResultat"
        val INNTEKT = "inntektV1"
        val AVTJENT_VERNEPLIKT = "harAvtjentVerneplikt"
        val FANGST_OG_FISK = "oppfyllerKravTilFangstOgFisk"
        val SENESTE_INNTEKTSMÅNED = "senesteInntektsmåned"
        val BRUKT_INNTEKTSPERIODE = "bruktInntektsPeriode"
        val GRUNNLAG_RESULTAT = "grunnlagResultat"
        val BEREGNINGS_REGEL_GRUNNLAG = "beregningsregel"
    }

    override fun filterPredicates(): List<Predicate<String, Packet>> {
        return listOf(
            Predicate { _, packet -> packet.hasField(INNTEKT) },
            Predicate { _, packet -> packet.hasField(SENESTE_INNTEKTSMÅNED) },
            Predicate { _, packet -> packet.hasField(GRUNNLAG_RESULTAT) },
            Predicate { _, packet -> !packet.hasField(PERIODE_RESULTAT) })
    }

    override fun onPacket(packet: Packet): Packet {

        val verneplikt = packet.getNullableBoolean(AVTJENT_VERNEPLIKT) ?: false
        val inntekt: Inntekt = packet.getObjectValue(INNTEKT) { inntektAdapter.fromJsonValue(it)!! }
        val senesteInntektsmåned = YearMonth.parse(packet.getStringValue(SENESTE_INNTEKTSMÅNED))

        val bruktInntektsPeriode =
            packet.getNullableObjectValue(BRUKT_INNTEKTSPERIODE, bruktInntektsPeriodeAdapter::fromJsonValue)

        val fangstOgFisk = packet.getNullableBoolean(FANGST_OG_FISK) ?: false

        val grunnlagBeregningsregel = packet.getMapValue(GRUNNLAG_RESULTAT)[BEREGNINGS_REGEL_GRUNNLAG].toString()

        val fakta = Fakta(
            inntekt,
            senesteInntektsmåned,
            bruktInntektsPeriode,
            verneplikt,
            fangstOgFisk,
            grunnlagBeregningsregel = grunnlagBeregningsregel
        )

        val evaluering = periode.evaluer(fakta)

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

        logger.info("USING ${env.password.substring(4)} to authenticate")

        val props = streamConfig(
            appId = SERVICE_APP_ID,
            bootStapServerUrl = env.bootstrapServersUrl,
            credential = KafkaCredential(env.username, env.password)
        )
        return props
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