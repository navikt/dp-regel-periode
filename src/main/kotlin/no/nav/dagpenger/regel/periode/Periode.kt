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
import java.time.YearMonth
import java.util.Properties

class Periode(val env: Environment) : River() {

    override val SERVICE_APP_ID: String = "dagpenger-regel-periode"
    override val HTTP_PORT: Int = env.httpPort ?: super.HTTP_PORT

    private val inntektAdapter =
        moshiInstance.adapter<no.nav.dagpenger.events.inntekt.v1.Inntekt>(no.nav.dagpenger.events.inntekt.v1.Inntekt::class.java)
    private val ulidGenerator = ULID()

    companion object {
        val REGELIDENTIFIKATOR = "Periode.v1"
        val PERIODE_RESULTAT = "periodeResultat"
        val INNTEKT = "inntektV1"
        val AVTJENT_VERNEPLIKT = "harAvtjentVerneplikt"
        val FANGST_OG_FISK = "fangstOgFisk"
        val SENESTE_INNTEKTSMÅNED = "senesteInntektsmåned"
        val BRUKT_INNTEKTSPERIODE = "bruktInntektsPeriode"
        val GRUNNLAG_RESULTAT = "grunnlagResultat"
        val BEREGNINGS_REGEL_GRUNNLAG = "beregningsregel"
    }

    override fun filterPredicates(): List<Predicate<String, Packet>> {
        return listOf(
            Predicate { _, packet -> packet.hasField(INNTEKT) },
            Predicate { _, packet -> packet.hasField(SENESTE_INNTEKTSMÅNED) },
            Predicate { _, packet -> !packet.hasField(PERIODE_RESULTAT) })
    }

    override fun onPacket(packet: Packet): Packet {

        val verneplikt = packet.getNullableBoolean(AVTJENT_VERNEPLIKT) ?: false
        val inntekt: Inntekt = packet.getObjectValue(INNTEKT) { requireNotNull(inntektAdapter.fromJson(it)) }
        val senesteInntektsmåned = YearMonth.parse(packet.getStringValue(SENESTE_INNTEKTSMÅNED))

        val bruktInntektsPeriode = getInntektsPeriode(packet)

        val fangstOgFisk = packet.getNullableBoolean(FANGST_OG_FISK) ?: false

        val grunnlagBeregningsregel = packet.getMapValue(GRUNNLAG_RESULTAT)[BEREGNINGS_REGEL_GRUNNLAG].toString()

        val fakta = Fakta(inntekt, senesteInntektsmåned, bruktInntektsPeriode, verneplikt, fangstOgFisk, grunnlagBeregningsregel = grunnlagBeregningsregel)

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

    private fun getInntektsPeriode(packet: Packet): InntektsPeriode? {
        return if (packet.hasField(BRUKT_INNTEKTSPERIODE)) {
            packet.getMapValue(BRUKT_INNTEKTSPERIODE).runCatching {
                InntektsPeriode(
                    førsteMåned = YearMonth.parse(this["førsteMåned"] as String),
                    sisteMåned = YearMonth.parse(this["sisteMåned"] as String)
                )
            }.getOrNull()
        } else null
    }

    override fun getConfig(): Properties {
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

    if (fakta.grunnlagBeregningsregel == "VERNEPLIKT") {
        return 26
    } else {
        val periodeResultat: Int? =
            evaluering.children.filter { it.resultat == Resultat.JA }.flatMap { mapEvalureringResultatToInt(it) }.max()
        return periodeResultat
    }
}

fun main(args: Array<String>) {
    val service = Periode(Environment())
    service.start()
}