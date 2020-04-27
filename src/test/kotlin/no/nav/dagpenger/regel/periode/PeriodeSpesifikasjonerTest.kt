package no.nav.dagpenger.regel.periode

import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.evaluations.Resultat
import no.nav.nare.core.specifications.Spesifikasjon
import org.junit.jupiter.api.Test

internal class PeriodeSpesifikasjonerTest {

    @Test
    fun `Periode består av ordinær`() {

        val expectedIdentifikatorer = setOf<String>(
            "ORDINÆR_12_52",
            "ORDINÆR_36_52",
            "ORDINÆR_12_52_FANGSTOGFISK",
            "ORDINÆR_36_52_FANGSTOGFISK",
            "ORDINÆR_12_104",
            "ORDINÆR_36_104",
            "ORDINÆR_12_104_FANGSTOGFISK",
            "ORDINÆR_36_104_FANGSTOGFISK",
            "VERNEPLIKT",
            "LÆRLING"
        )
        val identifikatorer = identifikatorer(periode.children)
        assertEquals(expectedIdentifikatorer, identifikatorer)
    }

    fun identifikatorer(spesifikasjoner: List<Spesifikasjon<Fakta>>): Set<String> = (spesifikasjoner.map { it.identifikator }.toSet() + spesifikasjoner.flatMap { identifikatorer(it.children) }).toSet()

    @Test
    fun `Ordinær består av ordinær`() {

        val expectedIdentifikatorer = setOf<String>(
            "ORDINÆR_12_52",
            "ORDINÆR_36_52",
            "ORDINÆR_12_52_FANGSTOGFISK",
            "ORDINÆR_36_52_FANGSTOGFISK",
            "ORDINÆR_12_104",
            "ORDINÆR_36_104",
            "ORDINÆR_12_104_FANGSTOGFISK",
            "ORDINÆR_36_104_FANGSTOGFISK"
        )
        val identifikatorer = identifikatorer(ordinær.children)
        assertEquals(expectedIdentifikatorer, identifikatorer)
    }

    @Test
    fun `Særregel består av verneplikt eller lærling`() {

        val expectedIdentifikatorer = setOf<String>(
            "VERNEPLIKT",
            "LÆRLING"
        )
        val identifikatorer = identifikatorer(særregel.children)
        assertEquals(expectedIdentifikatorer, identifikatorer)
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
        val fakta = Fakta(
            Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false,
            beregningsDato = LocalDate.of(2019, 5, 20),
            grunnlagBeregningsregel = "bla",
            lærling = false
        )

        assertEquals(52, finnHøyestePeriodeFraEvaluering(rotEvaluering, fakta))
    }
}
