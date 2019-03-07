package no.nav.dagpenger.regel.periode

import org.json.JSONObject
import java.math.BigDecimal
import java.time.YearMonth

data class SubsumsjonsBehov(val jsonObject: JSONObject) {

    companion object {
        val PERIODE_RESULTAT = "periodeResultat"
        val INNTEKT = "inntektV1"
        val TASKS = "tasks"
        val TASKS_HENT_INNTEKT = "hentInntekt"
        val AVTJENT_VERNEPLIKT = "harAvtjentVerneplikt"
        val SENESTE_INNTEKTSMÅNED = "senesteInntektsmåned"
        val BRUKT_INNTEKTSPERIODE = "bruktInntektsPeriode"

        val jsonAdapterInntekt = moshiInstance.adapter(Inntekt::class.java)
        val jsonAdapterInntektsPeriode = moshiInstance.adapter(InntektsPeriode::class.java)
    }

    fun needsHentInntektsTask(): Boolean = !hasInntekt() && !hasHentInntektTask()

    fun needsPeriodeSubsumsjon(): Boolean = hasInntekt() && !hasPeriodeSubsumsjon() && hasSenesteInntektsmåned()

    fun hasPeriodeSubsumsjon(): Boolean = jsonObject.has(PERIODE_RESULTAT)

    fun hasInntekt() = jsonObject.has(INNTEKT)

    fun hasSenesteInntektsmåned(): Boolean = jsonObject.has(SENESTE_INNTEKTSMÅNED)

    fun getSenesteInntektsmåned(): YearMonth = YearMonth.parse(jsonObject.get(SENESTE_INNTEKTSMÅNED).toString())

    fun hasBruktInntektsPeriode(): Boolean = jsonObject.has(BRUKT_INNTEKTSPERIODE)

    fun getBruktInntektsPeriode(): InntektsPeriode? {
        return if (hasBruktInntektsPeriode())
            jsonAdapterInntektsPeriode.fromJson(jsonObject.get(BRUKT_INNTEKTSPERIODE).toString())!!
        else null
    }

    fun hasHentInntektTask(): Boolean {
        if (jsonObject.has(TASKS)) {
            val tasks = jsonObject.getJSONArray(TASKS)
            for (task in tasks) {
                if (task.toString() == TASKS_HENT_INNTEKT) {
                    return true
                }
            }
        }
        return false
    }

    fun hasTasks(): Boolean = jsonObject.has(TASKS)

    fun addTask(task: String) {
        if (hasTasks()) {
            jsonObject.append(TASKS, task)
        } else {
            jsonObject.put(TASKS, listOf(task))
        }
    }

    fun getAvtjentVerneplikt(): Boolean = if (jsonObject.has(AVTJENT_VERNEPLIKT)) jsonObject.getBoolean(
        AVTJENT_VERNEPLIKT) else false

    fun addPeriodeSubsumsjon(periodeSubsumsjon: PeriodeSubsumsjon) { jsonObject.put(PERIODE_RESULTAT, periodeSubsumsjon.build()) }

    fun getInntekt(): Inntekt = jsonAdapterInntekt.fromJson(jsonObject.get(INNTEKT).toString())!!

    class Builder {

        val jsonObject = JSONObject()

        fun inntekt(inntekt: Inntekt): Builder {
            val json = jsonAdapterInntekt.toJson(inntekt)
            jsonObject.put(INNTEKT,
                JSONObject(json)
            )
            return this
        }

        fun senesteInntektsMåned(senesteInntektsMåned: YearMonth): Builder {
            jsonObject.put(SENESTE_INNTEKTSMÅNED, senesteInntektsMåned)
            return this
        }

        fun task(tasks: List<String>): Builder {
            jsonObject.put(TASKS, tasks)
            return this
        }

        fun periodeSubsumsjon(periodeSubsumsjon: PeriodeSubsumsjon): Builder {
            jsonObject.put(PERIODE_RESULTAT, periodeSubsumsjon.build())
            return this
        }

        fun build(): SubsumsjonsBehov = SubsumsjonsBehov(jsonObject)
    }
}

data class PeriodeSubsumsjon(val sporingsId: String, val subsumsjonsId: String, val regelidentifikator: String, val periode: Int) {

    companion object {
        val SPORINGSID = "sporingsId"
        val SUBSUMSJONSID = "subsumsjonsId"
        val REGELIDENTIFIKATOR = "regelIdentifikator"
        val PERIODE = "periodeAntallUker"
    }

    fun build(): JSONObject {
        return JSONObject()
            .put(SPORINGSID, sporingsId)
            .put(SUBSUMSJONSID, subsumsjonsId)
            .put(REGELIDENTIFIKATOR, regelidentifikator)
            .put(PERIODE, periode)
    }
}

data class InntektsPeriode(
    val førsteMåned: YearMonth,
    val sisteMåned: YearMonth
)

data class Inntekt(
    val inntektsId: String,
    val inntektsListe: List<KlassifisertInntektMåned>
)

data class KlassifisertInntektMåned(
    val årMåned: YearMonth,
    val klassifiserteInntekter: List<KlassifisertInntekt>
)

data class KlassifisertInntekt(
    val beløp: BigDecimal,
    val inntektKlasse: InntektKlasse
)

enum class InntektKlasse {
    ARBEIDSINNTEKT,
    DAGPENGER,
    DAGPENGER_FANGST_FISKE,
    SYKEPENGER_FANGST_FISKE,
    NÆRINGSINNTEKT,
    SYKEPENGER,
    TILTAKSLØNN
}