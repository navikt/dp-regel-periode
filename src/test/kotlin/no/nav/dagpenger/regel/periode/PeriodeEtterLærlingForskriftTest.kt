package no.nav.dagpenger.regel.periode

import no.nav.dagpenger.events.Packet
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
        "2020-03-19, 0",
        "2020-03-20, 52",
        "2021-09-30, 52",
        "2021-10-01, 0",
        "2021-12-21, 0",
        "2021-12-22, 52",
        "2022-02-28, 52",
        "2022-03-01, 0",
    )


    fun `Periode for lærling skal være 52 uker`(dato: String, antallUker: Int) {

        val application = Application(Configuration())
        val jsonAdapterInntekt = moshiInstance.adapter(Inntekt::class.java)

        val json =
            """
        {
            "lærling": true,
            "beregningsDato": "$dato",
            "grunnlagResultat":{"beregningsregel": "Har ingen betydning for utfall"}
        }
            """.trimIndent()

        val packet = Packet(json)

        val nullInntekt = Inntekt(
            inntektsId = "123",
            inntektsListe = emptyList(),
            sisteAvsluttendeKalenderMåned = YearMonth.of(2021, 4)
        )

        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(nullInntekt)!!)

        val outPacket = application.onPacket(packet)

        assertEquals(antallUker, outPacket.getMapValue("periodeResultat")["periodeAntallUker"])
    }


    @Test
    fun ` § 2-6 - Periode for lærlinger – unntak fra folketrygdloven § 4-4 til § 4-6 og det er koronatid `() {

        // gitt fakta
        val fakta = Fakta(
            inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false,
            beregningsDato = LocalDate.of(2020, 3, 20),
            regelverksdato = LocalDate.of(2020, 3, 20),
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
            regelverksdato = LocalDate.of(2021, 10, 1),
            lærling = true,
            grunnlagBeregningsregel = "BLA"
        )

        assertFalse(fakta.erSærregel())
    }
}
