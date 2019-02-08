package no.nav.dagpenger.regel.periode

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class InnvilgetPeriodeTest {

    @Test
    fun `Skal få periode på 26 uker ved verneplikt`() {
        val resultat = finnPeriode(true, 0)
        assertEquals(26, resultat)
    }

    @Test
    fun `Skal få periode på 0 uker uten inntekt`() {
        val resultat = finnPeriode(false, 0)
        assertEquals(0, resultat)
    }
}