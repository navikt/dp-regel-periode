package no.nav.dagpenger.regel.periode

import de.huxhorn.sulky.ulid.ULID
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.streams.KafkaCredential
import no.nav.dagpenger.streams.River
import no.nav.dagpenger.streams.streamConfig
import org.apache.kafka.streams.kstream.Predicate
import java.math.BigDecimal
import java.time.YearMonth
import java.util.Properties

class Periode(val env: Environment) : River() {

    override val SERVICE_APP_ID: String = "dagpenger-regel-periode"
    override val HTTP_PORT: Int = env.httpPort ?: super.HTTP_PORT

    private val inntektAdapter =
        moshiInstance.adapter<no.nav.dagpenger.events.inntekt.v1.Inntekt>(no.nav.dagpenger.events.inntekt.v1.Inntekt::class.java)
    private val jsonAdapterInntektsPeriode = moshiInstance.adapter(InntektsPeriode::class.java)
    private val ulidGenerator = ULID()

    companion object {
        val REGELIDENTIFIKATOR = "Periode.v1"
        val PERIODE_RESULTAT = "periodeResultat"
        val INNTEKT = "inntektV1"
        val AVTJENT_VERNEPLIKT = "harAvtjentVerneplikt"
        val FANGST_OG_FISK = "fangstOgFisk"
        val SENESTE_INNTEKTSMÅNED = "senesteInntektsmåned"
        val BRUKT_INNTEKTSPERIODE = "bruktInntektsPeriode"
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
        val bruktInntektsPeriode =
            packet.getNullableObjectValue(BRUKT_INNTEKTSPERIODE, jsonAdapterInntektsPeriode::fromJson)
        val fangstOgFisk = packet.getNullableBoolean(FANGST_OG_FISK) ?: false

        val periodeResultat = finnPeriode(verneplikt, inntekt, senesteInntektsmåned, bruktInntektsPeriode, fangstOgFisk)

        val subsumsjon = PeriodeSubsumsjon(
            ulidGenerator.nextULID(),
            ulidGenerator.nextULID(),
            REGELIDENTIFIKATOR,
            periodeResultat
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
}

fun main(args: Array<String>) {
    val service = Periode(Environment())
    service.start()
}

fun finnPeriode(
    verneplikt: Boolean,
    inntekt: Inntekt,
    senesteInntektsmåned: YearMonth,
    bruktInntektsPeriode: InntektsPeriode? = null,
    fangstOgFisk: Boolean
): Int {

    val filtrertInntekt = bruktInntektsPeriode?.let { inntektsPeriode -> inntekt.filterPeriod(inntektsPeriode.førsteMåned, inntektsPeriode.sisteMåned) } ?: inntekt

    val enG = BigDecimal(96883)

    var inntektSiste12 = filtrertInntekt.sumInntektLast12Months(listOf(InntektKlasse.ARBEIDSINNTEKT, InntektKlasse.DAGPENGER, InntektKlasse.SYKEPENGER, InntektKlasse.TILTAKSLØNN), senesteInntektsmåned)
    var inntektSiste36 = filtrertInntekt.sumInntektLast36Months(listOf(InntektKlasse.ARBEIDSINNTEKT, InntektKlasse.DAGPENGER, InntektKlasse.SYKEPENGER, InntektKlasse.TILTAKSLØNN), senesteInntektsmåned)

    var arbeidsInntektSiste12 = filtrertInntekt.sumInntektLast12Months(listOf(InntektKlasse.ARBEIDSINNTEKT), senesteInntektsmåned)
    var arbeidsInntektSiste36 = filtrertInntekt.sumInntektLast36Months(listOf(InntektKlasse.ARBEIDSINNTEKT), senesteInntektsmåned)

    if (fangstOgFisk) {
        arbeidsInntektSiste12 += filtrertInntekt.sumInntektLast12Months(listOf(InntektKlasse.FANGST_FISKE), senesteInntektsmåned)
        arbeidsInntektSiste36 += filtrertInntekt.sumInntektLast36Months(listOf(InntektKlasse.FANGST_FISKE), senesteInntektsmåned)

        inntektSiste12 += filtrertInntekt.sumInntektLast12Months(listOf(InntektKlasse.FANGST_FISKE, InntektKlasse.DAGPENGER_FANGST_FISKE, InntektKlasse.SYKEPENGER_FANGST_FISKE), senesteInntektsmåned)
        inntektSiste36 += filtrertInntekt.sumInntektLast36Months(listOf(InntektKlasse.FANGST_FISKE, InntektKlasse.DAGPENGER_FANGST_FISKE, InntektKlasse.SYKEPENGER_FANGST_FISKE), senesteInntektsmåned)
    }
    val årligSnittInntektSiste36 = inntektSiste36 / BigDecimal(3)

    var harTjentNok = false
    if (arbeidsInntektSiste12 > (enG.times(BigDecimal(1.5))) || arbeidsInntektSiste36 > (enG.times(BigDecimal(3)))) {
        harTjentNok = true
    }

    if (harTjentNok) {
        if (inntektSiste12 > enG.times(BigDecimal(2)) || årligSnittInntektSiste36 > enG.times(BigDecimal(2))) {
            return 104
        }

        if (inntektSiste12 < enG.times(BigDecimal(2)) || årligSnittInntektSiste36 < enG.times(BigDecimal(2))) {
            return 52
        }
    }

    return when (verneplikt) {
        true -> 26
        false -> 0
    }
}

fun filterBruktInntekt(
    inntektsListe: List<KlassifisertInntektMåned>,
    bruktInntektsPeriode: InntektsPeriode
): List<KlassifisertInntektMåned> {

    return inntektsListe.filter {
        it.årMåned.isBefore(bruktInntektsPeriode.førsteMåned) || it.årMåned.isAfter(bruktInntektsPeriode.sisteMåned)
    }
}

fun finnTidligsteMåned(fraMåned: YearMonth, lengde: Int): YearMonth {

    return fraMåned.minusMonths(lengde.toLong())
}