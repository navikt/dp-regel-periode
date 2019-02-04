package no.nav.dagpenger.regel.periode

import java.time.LocalDate

data class SubsumsjonsBehov (
    val aktørId: String,
    val vedtakId: Int,
    val beregningsDato: LocalDate,
    val avtjentVerneplikt: Boolean? = false,
    val eøs: Boolean? = false,
    val antallBarn: Int? = 0,
    val inntekt: Int? = null,

    var tasks: List<String>? = null,

    var periodeSubsumsjon: PeriodeSubsumsjon? = null
)

data class PeriodeSubsumsjon(
    val sporingsId: String,
    val subsumsjonsId: String,
    val regelIdentifikator: String,
    val antallUker: Int
)
