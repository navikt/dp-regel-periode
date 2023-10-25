package no.nav.dagpenger.regel.periode

import java.time.YearMonth

data class PeriodeSubsumsjon(
    val sporingsId: String,
    val subsumsjonsId: String,
    val regelidentifikator: String,
    val periode: Int,
) {
    companion object {
        val SPORINGSID = "sporingsId"
        val SUBSUMSJONSID = "subsumsjonsId"
        val REGELIDENTIFIKATOR = "regelIdentifikator"
        val PERIODE = "periodeAntallUker"
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            SPORINGSID to sporingsId,
            SUBSUMSJONSID to subsumsjonsId,
            REGELIDENTIFIKATOR to regelidentifikator,
            PERIODE to periode,
        )
    }
}

data class InntektsPeriode(
    val førsteMåned: YearMonth,
    val sisteMåned: YearMonth,
)
