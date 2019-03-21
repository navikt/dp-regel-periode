package no.nav.dagpenger.regel.periode

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class PeriodeSpesifikasjonerTest {

    @Test
    fun `Periode består av ordinær`() {
        assertEquals("ORDINÆR_12_52 ELLER ORDINÆR_36_52 ELLER ORDINÆR52 ELLER ORDINÆR, ORDINÆR_12_52 ELLER ORDINÆR_36_52 ELLER ORDINÆR52, ORDINÆR, PERIODE, ", periode.children.joinToString { it.identitet + ", " + it.children.joinToString { it.identitet } })
    }

    @Test
    fun `Ordinær består av ordinær`() {
        assertEquals("ORDINÆR_12_52 ELLER ORDINÆR_36_52 ELLER ORDINÆR52, ORDINÆR", ordinær.children.joinToString { it.identitet })
    }
}