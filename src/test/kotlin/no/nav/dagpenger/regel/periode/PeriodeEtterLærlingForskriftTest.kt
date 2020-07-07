package no.nav.dagpenger.regel.periode

import java.time.LocalDate
import java.time.YearMonth
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.nare.core.evaluations.Resultat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class PeriodeEtterLærlingForskriftTest {

    @Test
    fun ` § 2-6 - Periode for lærlinger – unntak fra folketrygdloven § 4-4 til § 4-6 og det er koronatid `() {

        // gitt fakta
        val fakta = Fakta(
            inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false,
            beregningsDato = LocalDate.of(2020, 3, 20),
            lærling = true,
            grunnlagBeregningsregel = "BLA"
        )

        // når
        val evaluering = lærlingPeriode.evaluer(fakta)

        // så
        Assertions.assertEquals(Resultat.JA, evaluering.resultat)
        Assertions.assertEquals("52", evaluering.begrunnelse)
    }

    @Test
    fun `§ 2-6 - Skal ikke gi periode for lærlinger – unntak fra folketrygdloven § 4-4 til § 4-6 `() {

        // gitt fakta
        val fakta = Fakta(
            inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false,
            beregningsDato = LocalDate.of(2020, 4, 20),
            lærling = false,
            grunnlagBeregningsregel = "BLA"
        )

        // når
        val evaluering = vernepiktPeriode.evaluer(fakta)

        // så
        Assertions.assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `§ 2-6 - Skal ikke gi periode for lærlinger hvis utenfor koronatid  – unntak fra folketrygdloven § 4-4 til § 4-6 `() {

        // gitt fakta
        val fakta = Fakta(
            inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false,
            beregningsDato = LocalDate.of(2021, 1, 1),
            lærling = true,
            grunnlagBeregningsregel = "BLA"
        )

        // når
        val evaluering = vernepiktPeriode.evaluer(fakta)

        // så
        Assertions.assertEquals(Resultat.NEI, evaluering.resultat)
    }
}
