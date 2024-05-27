package no.nav.dagpenger.regel.periode

import io.getunleash.Unleash
import no.nav.dagpenger.grunnbelop.Grunnbeløp
import no.nav.dagpenger.grunnbelop.Regel
import no.nav.dagpenger.grunnbelop.forDato
import no.nav.dagpenger.grunnbelop.getGrunnbeløpForRegel
import java.math.BigDecimal
import java.time.LocalDate

const val GJUSTERING_TEST = "dp-g-justeringstest"

class GrunnbeløpStrategy(private val unleash: Unleash = Config.unleash) {
    fun grunnbeløp(beregningsdato: LocalDate): BigDecimal {
        return if (isThisGjusteringTest(beregningsdato)) {
            Grunnbeløp.GjusteringsTest.verdi
        } else {
            getGrunnbeløpForRegel(Regel.Minsteinntekt).forDato(beregningsdato).verdi
        }
    }

    private fun isThisGjusteringTest(dato: LocalDate): Boolean {
        // Dette er HG (Hengende G)
        val gVirkning = LocalDate.of(2024, 6, 3)
        val isAfterGjustering = dato.isAfter(gVirkning.minusDays(1))
        return unleash.isEnabled(GJUSTERING_TEST, false) && isAfterGjustering
    }
}
