package no.nav.dagpenger.regel.periode

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.nare.core.evaluations.Resultat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDate
import java.time.YearMonth

internal class PeriodeEtterLærlingForskriftTest {
    @ParameterizedTest
    @CsvSource(
        "2020-03-19, ORDINÆR_12_52",
        "2020-03-20, LÆRLING",
        "2021-09-30, LÆRLING",
        "2021-10-01, ORDINÆR_12_52",
        "2021-12-14, ORDINÆR_12_52",
        "2021-12-15, LÆRLING",
        "2022-03-31, LÆRLING",
        "2022-04-01, ORDINÆR_12_52",
    )
    fun ` § 2-6 - Periode for lærlinger – unntak fra folketrygdloven § 4-4 til § 4-6 og det er koronatid `(
        beregningsdato: LocalDate,
        identifikator: String,
    ) {
        // gitt fakta
        val fakta =
            Fakta(
                inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
                bruktInntektsPeriode = null,
                verneplikt = false,
                fangstOgFisk = false,
                beregningsDato = beregningsdato,
                regelverksdato = beregningsdato,
                lærling = true,
                grunnlagBeregningsregel = "Har ingen betydning for utfall",
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
            )

        // når
        val evaluering = periode.evaluer(fakta)

        // så
        assertEquals(Resultat.JA, evaluering.children[0].resultat)
        assertEquals("52", evaluering.children[0].begrunnelse)
        assertEquals(identifikator, evaluering.children[0].identifikator)
    }

    @Test
    fun `§ 2-6 - Skal ikke gi periode for lærlinger hvis utenfor koronatid  – unntak fra folketrygdloven § 4-4 til § 4-6 `() {
        // gitt fakta
        val beregningsdato = LocalDate.of(2021, 10, 1)
        val fakta =
            Fakta(
                inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
                bruktInntektsPeriode = null,
                verneplikt = false,
                fangstOgFisk = false,
                beregningsDato = beregningsdato,
                regelverksdato = LocalDate.of(2021, 10, 1),
                lærling = true,
                grunnlagBeregningsregel = "BLA",
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
            )

        assertFalse(fakta.erSærregel())
    }
}
