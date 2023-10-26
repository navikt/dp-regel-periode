package no.nav.dagpenger.regel.periode

import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.evaluations.Resultat

object Evalueringer {
    fun mapEvalueringResultatToInt(it: Evaluering): List<Int> {
        return if (it.children.isEmpty()) {
            listOf(it.begrunnelse.toInt())
        } else {
            it.children.flatMap { mapEvalueringResultatToInt(it) }
        }
    }

    // TODO: Mer intuitiv og robust løsning for 26 ukers dagpengeperiode for vernepliktige
    fun finnHøyestePeriodeFraEvaluering(
        evaluering: Evaluering,
        beregningsregel: String,
    ): Int? {
        return if (beregningsregel == "Verneplikt") {
            26
        } else {
            return evaluering
                .children
                .filter { it.resultat == Resultat.JA }
                .flatMap { mapEvalueringResultatToInt(it) }
                .maxOrNull()
        }
    }
}
