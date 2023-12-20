package no.nav.dagpenger.regel.periode

import io.getunleash.FakeUnleash
import no.nav.dagpenger.inntekt.v1.Inntekt
import no.nav.dagpenger.inntekt.v1.InntektKlasse
import no.nav.dagpenger.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.inntekt.v1.KlassifisertInntektMåned
import no.nav.nare.core.evaluations.Resultat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

internal class GjusteringTest {
    private var unleash = FakeUnleash()
    private val grunnbeløpStrategy = GrunnbeløpStrategy(unleash)

    @BeforeEach
    fun setup() {
        unleash.resetAll()
    }

    @Test
    @Disabled
    fun `Skal få periode på 104 uker dersom inntekt siste 12 måned er under 2G`() {
        val beregningsdato = LocalDate.of(2020, 10, 20)
        val fakta =
            Fakta(
                inntekt =
                    Inntekt(
                        "123",
                        generateArbeidsInntekt(1..1, BigDecimal(200000)),
                        sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 10),
                    ),
                bruktInntektsperiode = null,
                verneplikt = false,
                fangstOgFiske = false,
                beregningsdato = beregningsdato,
                regelverksdato = LocalDate.of(2020, 10, 20),
                lærling = false,
                grunnlagBeregningsregel = "BLA",
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
            )

        val evaluering = ordinærSiste12Måneder104Uker.evaluer(fakta)

        assertEquals(Resultat.JA, evaluering.resultat)
    }

    @Test
    fun `Skal få periode på 52 uker dersom inntekt siste 12 måned er under 2G`() {
        unleash.enable(GJUSTERING_TEST)

        val beregningsdato = LocalDate.of(2020, 10, 20)
        val fakta =
            Fakta(
                inntekt =
                    Inntekt(
                        "123",
                        generateArbeidsInntekt(1..1, BigDecimal(202701)),
                        sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 10),
                    ),
                bruktInntektsperiode = null,
                verneplikt = false,
                fangstOgFiske = false,
                beregningsdato = beregningsdato,
                regelverksdato = LocalDate.of(2020, 10, 20),
                lærling = false,
                grunnlagBeregningsregel = "BLA",
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
            )

        val evaluering = ordinærSiste12Måneder104Uker.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    fun generateArbeidsInntekt(
        range: IntRange,
        beløpPerMnd: BigDecimal,
    ): List<KlassifisertInntektMåned> {
        return (range).toList().map {
            KlassifisertInntektMåned(
                YearMonth.of(2020, 1).minusMonths(it.toLong()),
                listOf(
                    KlassifisertInntekt(
                        beløpPerMnd,
                        InntektKlasse.ARBEIDSINNTEKT,
                    ),
                ),
            )
        }
    }
}
