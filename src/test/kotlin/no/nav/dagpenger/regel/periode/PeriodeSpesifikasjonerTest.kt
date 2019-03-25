package no.nav.dagpenger.regel.periode

import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.evaluations.Resultat
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class PeriodeSpesifikasjonerTest {

    @Test
    fun `Periode består av ordinær`() {
        assertEquals("ORDINÆR_12_52 ELLER ORDINÆR_36_52 ELLER ORDINÆR52 ELLER ORDINÆR, ORDINÆR_12_52 ELLER ORDINÆR_36_52 ELLER ORDINÆR52, ORDINÆR, PERIODE, ", periode.children.joinToString { it.identitet + ", " + it.children.joinToString { it.identitet } })
    }

    @Test
    fun `Ordinær består av ordinær`() {
        assertEquals("ORDINÆR_12_52 ELLER ORDINÆR_36_52 ELLER ORDINÆR52, ORDINÆR", ordinær.children.joinToString { it.identitet })
    }

    @Test
    fun `Skal returnere 52 som antall uker`() {
        val evaluering = Evaluering.ja("52")

        assertEquals(52, mapEvalureringResultatToInt(evaluering).max())
    }

    @Test
    fun `Skal returnere evalueringene som int`() {
        val evaluering = listOf(Evaluering.ja("52"), Evaluering.ja("26"))

        val resultat = evaluering.flatMap { mapEvalureringResultatToInt(it) }

        assertEquals(listOf(52, 26), resultat)
    }

    @Test
    fun `Skal returnere høyeste periode som har evaluering lik JA`() {

        val evaluering = listOf(Evaluering.ja("52"), Evaluering.ja("26"), Evaluering.nei("104"))

        val rotEvaluering = Evaluering(Resultat.JA, "52", children = evaluering)

        assertEquals(52, finnHøyestePeriodeFraEvaluering(rotEvaluering))
    }
}