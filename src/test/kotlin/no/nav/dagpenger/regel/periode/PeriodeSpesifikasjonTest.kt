package no.nav.dagpenger.regel.periode

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.evaluations.Resultat
import no.nav.nare.core.specifications.Spesifikasjon
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

internal class PeriodeSpesifikasjonTest {

    fun identifikatorer(spesifikasjoner: List<Spesifikasjon<Fakta>>): Set<String> =
        (
            spesifikasjoner.map { it.identifikator }
                .toSet() + spesifikasjoner.flatMap { identifikatorer(it.children) }
            ).toSet()

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
        expectedIdentifikatorer shouldBe identifikatorer
    }

    @Test
    fun `Skal returnere 52 som antall uker`() {
        val evaluering = Evaluering.ja("52")
        mapEvalureringResultatToInt(evaluering).maxOrNull() shouldBe 52
    }

    @Test
    fun `Skal returnere evalueringene som int`() {
        val evaluering = listOf(Evaluering.ja("52"), Evaluering.ja("26"))

        val resultat = evaluering.flatMap { mapEvalureringResultatToInt(it) }

        listOf(52, 26) shouldBe resultat
    }

    @Test
    fun `Skal returnere høyeste periode som har evaluering lik JA`() {

        val evaluering = listOf(Evaluering.ja("52"), Evaluering.ja("26"), Evaluering.nei("104"))

        val grunnlagBeregningsregel = "BLA"
        val rotEvaluering = Evaluering(Resultat.JA, "52", children = evaluering)

        finnHøyestePeriodeFraEvaluering(rotEvaluering, grunnlagBeregningsregel) shouldBe 52
    }

    // Fjerner den gamle testen, og bruker kun denne, da lærlingflag settes manuelt av saksbehandler, og skal ikke settes av saksbehandler når særregelen ikke lenger gjelder
    @Test
    fun ` Ordinær skal ikke behandle lærling`() {
        val fakta = Fakta(
            inntekt = Inntekt(
                "123",
                generateArbeidsInntekt(1..12, BigDecimal(1000000)),
                sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 2)
            ),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false,
            beregningsDato = LocalDate.of(2020, 3, 20),
            regelverksdato = LocalDate.of(2020, 3, 20),
            lærling = true,
            grunnlagBeregningsregel = "BLA"
        )
        val evaluering = periode.evaluer(fakta)
        assertSoftly {
            evaluering.resultat shouldBe Resultat.JA
            fakta.erSærregel() shouldBe true
            evaluering.children.filter { periodeEtterOrdinæreMedJa(it) }
                .shouldBeEmpty()
        }
    }

    @Test
    fun ` Ordinær brukes hvis det ikke er særregel `() {
        val fakta = Fakta(
            inntekt = Inntekt(
                "123",
                generateArbeidsInntekt(1..12, BigDecimal(1000000)),
                sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 2)
            ),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false,
            beregningsDato = LocalDate.of(2020, 3, 20),
            regelverksdato = LocalDate.of(2020, 3, 20),
            lærling = false,
            grunnlagBeregningsregel = "BLA"
        )
        val evaluering = periode.evaluer(fakta)
        assertSoftly {
            fakta.erSærregel() shouldBe false
            evaluering.resultat shouldBe Resultat.JA
            evaluering.children.filter { periodeEtterOrdinæreMedJa(it) }
                .shouldNotBeEmpty()
        }
    }

    private fun periodeEtterOrdinæreMedJa(it: Evaluering) =
        it.resultat == Resultat.JA && it.identifikator.startsWith("ORDINÆR")

    private fun generateArbeidsInntekt(range: IntRange, beløpPerMnd: BigDecimal): List<KlassifisertInntektMåned> {
        return (range).toList().map {
            KlassifisertInntektMåned(
                YearMonth.of(2020, 2).minusMonths(it.toLong()),
                listOf(
                    KlassifisertInntekt(
                        beløpPerMnd,
                        InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        }
    }
}
