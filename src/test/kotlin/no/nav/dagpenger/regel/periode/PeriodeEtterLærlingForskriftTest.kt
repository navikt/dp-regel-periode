package no.nav.dagpenger.regel.periode

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.nare.core.evaluations.Resultat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

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
        val evaluering = periode.evaluer(fakta)

        // så
        assertEquals(Resultat.JA, evaluering.children[0].resultat)
        assertEquals("52", evaluering.children[0].begrunnelse)
        assertEquals("LÆRLING", evaluering.children[0].identifikator)
        assertEquals(
            "§ 2-6. Midlertidig inntekssikringsordning for lærlinger – unntak fra folketrygdloven § 4-4 til § 4-6",
            evaluering.children[0].beskrivelse
        )
    }

    @Test
    fun `§ 2-6 - Skal ikke gi periode for lærlinger hvis utenfor koronatid  – unntak fra folketrygdloven § 4-4 til § 4-6 `() {

        // gitt fakta
        val fakta = Fakta(
            inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false,
            beregningsDato = LocalDate.of(2021, 10, 1),
            lærling = true,
            grunnlagBeregningsregel = "BLA"
        )

        assertFalse(fakta.erSærregel())
    }
}
