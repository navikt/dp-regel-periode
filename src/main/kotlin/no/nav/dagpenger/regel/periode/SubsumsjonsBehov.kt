package no.nav.dagpenger.regel.periode

import org.json.JSONObject

data class SubsumsjonsBehov(val jsonObject: JSONObject) {

    fun needsHentInntektsTask(): Boolean = !hasInntekt() && !hasHentInntektTask()

    fun needsPeriodeSubsumsjon(): Boolean = hasInntekt() && !hasPeriodeSubsumsjon()

    private fun hasInntekt() = jsonObject.has("inntekt")

    private fun hasHentInntektTask(): Boolean {
        if (jsonObject.has("tasks")) {
            val tasks = jsonObject.getJSONArray("tasks")
            for (task in tasks) {
                if (task.toString() == "hentInntekt") {
                    return true
                }
            }
        }
        return false
    }

    private fun hasPeriodeSubsumsjon() = jsonObject.has("periodeSubsumsjon")

    fun hasTasks(): Boolean = jsonObject.has("tasks")

    fun hasVerneplikt(): Boolean = jsonObject.has("tasks")
}

