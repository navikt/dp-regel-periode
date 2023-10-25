package no.nav.dagpenger.regel.periode

import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.nare.core.evaluations.Resultat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

internal class PeriodeEtterAvtjentVernepliktTest {
    val jsonAdapterInntekt = moshiInstance.adapter(Inntekt::class.java)

    @Test
    fun `Vernepliktperiode burde være 26 uker`() {
        val application = Application(Configuration())
        val json =
            """
            {
                "harAvtjentVerneplikt": true,
                "beregningsDato": "2020-05-20",
                "grunnlagResultat":{"beregningsregel": "Verneplikt"}
            }
            """.trimIndent()

        val packet = Packet(json)

        val nullInntekt =
            Inntekt(
                inntektsId = "123",
                inntektsListe = emptyList(),
                sisteAvsluttendeKalenderMåned = YearMonth.of(2021, 4),
            )

        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(nullInntekt)!!)

        val outPacket = application.onPacket(packet)

        assertEquals(26, outPacket.getMapValue("periodeResultat")["periodeAntallUker"])
    }

    @Test
    fun ` § 4-19 - Dagpenger etter avtjent verneplikt skal gi 26 uker  `() {
        val fakta =
            Fakta(
                inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
                bruktInntektsPeriode = null,
                verneplikt = true,
                fangstOgFisk = false,
                beregningsDato = LocalDate.of(2019, 5, 20),
                regelverksdato = LocalDate.of(2019, 5, 20),
                lærling = false,
                grunnlagBeregningsregel = "Verneplikt",
            )

        val evaluering = vernepliktPeriode.evaluer(fakta)
        assertEquals(Resultat.JA, evaluering.resultat)
    }

    @Test
    fun ` § 4-19 - Dagpenger etter ikke å ha avtjent verneplikt skal ikke gi 26 uker  `() {
        val fakta =
            Fakta(
                inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
                bruktInntektsPeriode = null,
                verneplikt = false,
                fangstOgFisk = false,
                beregningsDato = LocalDate.of(2019, 5, 20),
                regelverksdato = LocalDate.of(2019, 5, 20),
                lærling = false,
                grunnlagBeregningsregel = "BLA",
            )

        val evaluering = vernepliktPeriode.evaluer(fakta)
        assertEquals(Resultat.NEI, evaluering.resultat)
    }
}
