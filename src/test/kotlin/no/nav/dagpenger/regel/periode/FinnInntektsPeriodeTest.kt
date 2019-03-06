package no.nav.dagpenger.regel.periode

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.YearMonth

class FinnPeriodeTest {

    @Test
    fun ` skal returnere mars(19) til april(18) når lengden er 12 `() {

        val fraMåned = YearMonth.of(2019, 3)
        val tidligsteMåned = finnTidligsteMåned(fraMåned, 11)

        assertEquals(YearMonth.of(2018, 4), tidligsteMåned)
    }

    @Test
    fun ` skal returnere mars(19) til april(16) når lengden er 36 `() {

        val fraMåned = YearMonth.of(2019, 3)
        val tidligsteMåned = finnTidligsteMåned(fraMåned, 35)

        assertEquals(YearMonth.of(2016, 4), tidligsteMåned)
    }
}