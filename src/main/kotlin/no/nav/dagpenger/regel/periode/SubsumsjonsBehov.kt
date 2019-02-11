package no.nav.dagpenger.regel.periode

import org.json.JSONObject

data class SubsumsjonsBehov(val jsonObject: JSONObject) {

    companion object {
        val PERIODE_RESULTAT = "periodeResultat"
        val INNTEKT = "inntekt"
        val TASKS = "tasks"
        val TASKS_HENT_INNTEKT = "hentInntekt"
        val AVTJENT_VERNEPLIKT = "avtjentVerneplikt"

        val SPORINGSID = "sporingsId"
        val SUBSUMSJONSID = "subsumsjonsId"
        val REGELIDENTIFIKATOR = "regelIdentifikator"
        val PERIODE = "periodeAntallUker"
    }

    fun needsHentInntektsTask(): Boolean = !hasInntekt() && !hasHentInntektTask()

    fun needsPeriodeSubsumsjon(): Boolean = hasInntekt() && !hasPeriodeSubsumsjon()

    fun hasPeriodeSubsumsjon(): Boolean = jsonObject.has(PERIODE_RESULTAT)

    fun hasInntekt() = jsonObject.has(INNTEKT)

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

    fun getInntekt(): Int = jsonObject.get(INNTEKT) as Int

    data class PeriodeSubsumsjon(val sporingsId: String, val subsumsjonsId: String, val regelidentifikator: String, val periode: Int) {

        fun build(): JSONObject {
            return JSONObject()
                .put(SPORINGSID, sporingsId)
                .put(SUBSUMSJONSID, subsumsjonsId)
                .put(REGELIDENTIFIKATOR, regelidentifikator)
                .put(PERIODE, periode)
        }
    }

    class Builder {

        val jsonObject = JSONObject()

        fun inntekt(inntekt: Int): Builder {
            jsonObject.put(INNTEKT, inntekt)
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
