package no.nav.dagpenger.regel.periode

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.evaluations.Resultat
import org.junit.jupiter.api.Test

internal class PeriodeSpesifikasjonTest {

    @Test
    fun ` Ordinær skal ikke behandle lærling under koronatid`() {
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
            grunnlagBeregningsregel = "bla",
            lærling = true
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
    fun ` Ordinær skal behandle lærling når det ikke er koronatid`() {
        val fakta = Fakta(
            inntekt = Inntekt(
                "123",
                generateArbeidsInntekt(1..12, BigDecimal(1000000)),
                sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 2)
            ),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false,
            beregningsDato = LocalDate.of(2020, 3, 1),
            grunnlagBeregningsregel = "bla",
            lærling = true
        )

        val evaluering = periode.evaluer(fakta)
        assertSoftly {
            fakta.erSærregel() shouldBe false
            evaluering.resultat shouldBe Resultat.JA
            evaluering.children.filter { periodeEtterOrdinæreMedJa(it) }
                .shouldNotBeEmpty()
        }
    }

    @Test
    fun ` Ordinær skal ikke behandle verneplikt `() {
        val fakta = Fakta(
            inntekt = Inntekt(
                "123",
                generateArbeidsInntekt(1..12, BigDecimal(1000000)),
                sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 2)
            ),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = false,
            beregningsDato = LocalDate.of(2020, 3, 20),
            grunnlagBeregningsregel = "bla",
            lærling = false
        )
        val evaluering = periode.evaluer(fakta)
        assertSoftly {
            fakta.erSærregel() shouldBe true
            evaluering.resultat shouldBe Resultat.JA
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
            grunnlagBeregningsregel = "bla",
            lærling = false
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
                YearMonth.of(2020, 2).minusMonths(it.toLong()), listOf(
                    KlassifisertInntekt(
                        beløpPerMnd, InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        }
    }
}
