package no.nav.dagpenger.regel.periode

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.grunnbelop.Regel
import no.nav.dagpenger.grunnbelop.forDato
import no.nav.dagpenger.grunnbelop.getGrunnbeløpForRegel
import java.time.LocalDate
import kotlin.test.Test

class GrunnbeløpSanityTest {
    @Test
    fun `Sanity check for å sjekke at vi har fått ny G`() {
        // Denne endrer seg hvis vi får en ny G fra dp-grunnbelop
        // - forventet at den feiler da. Oppdater til ny verdi da
        getGrunnbeløpForRegel(Regel.Minsteinntekt)
            .forDato(
                LocalDate.now().plusYears(1),
                LocalDate.now().plusYears(1),
            ).verdi shouldBe 136549.toBigDecimal()
    }
}
