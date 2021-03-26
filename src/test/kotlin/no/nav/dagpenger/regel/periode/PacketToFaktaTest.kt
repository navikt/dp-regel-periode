package no.nav.dagpenger.regel.periode

import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

class PacketToFaktaTest {

    val emptyInntekt: Inntekt = Inntekt(
        inntektsId = "12345",
        inntektsListe = emptyList(),
        sisteAvsluttendeKalenderMåned = YearMonth.of(2018, 3)
    )

    val jsonAdapterInntekt = moshiInstance.adapter(Inntekt::class.java)

    @Test
    fun ` should map fangst_og_fisk from packet to Fakta `() {
        val json =
            """
        {
            "oppfyllerKravTilFangstOgFisk": true,
            "beregningsDato": "2019-05-20",
            "grunnlagResultat":{"beregningsregel": "BLA"}
        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(emptyInntekt)!!)

        val fakta = packetToFakta(packet)

        assertTrue(fakta.fangstOgFisk)
    }

    @Test
    fun ` should map avtjent_verneplikt from packet to Fakta `() {
        val json =
            """
        {
            "harAvtjentVerneplikt": true,
            "beregningsDato": "2019-05-20",
            "grunnlagResultat":{"beregningsregel": "BLA"}
        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(emptyInntekt)!!)

        val fakta = packetToFakta(packet)

        assertTrue(fakta.verneplikt)
    }

    @Test
    fun ` should map beregningsdato from packet to Fakta `() {
        val json =
            """
        {
            "harAvtjentVerneplikt": true,
            "beregningsDato": "2019-05-20",
            "grunnlagResultat":{"beregningsregel": "BLA"}
        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(emptyInntekt)!!)

        val fakta = packetToFakta(packet)

        assertEquals(LocalDate.of(2019, 5, 20), fakta.beregningsDato)
    }

    @Test
    fun ` should map regelverksdato from packet to Fakta `() {
        val jsonMedRegelverksdato =
            """
        {
            "harAvtjentVerneplikt": true,
            "grunnlagResultat":{"beregningsregel": "BLA"},
            "beregningsDato": "2019-04-10",
            "regelverksdato": "2019-05-10"
            
        }
            """.trimIndent()
        val jsonUtenRegelverksdato =
            """
        {
            "harAvtjentVerneplikt": true,
            "grunnlagResultat":{"beregningsregel": "BLA"},
            "beregningsDato": "2019-04-10"
        }
            """.trimIndent()

        Packet(jsonMedRegelverksdato).also { packet ->
            packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(emptyInntekt)!!)
            assertEquals(LocalDate.of(2019, 5, 10), packetToFakta(packet).regelverksdato)
        }
        Packet(jsonUtenRegelverksdato).also { packet ->
            packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(emptyInntekt)!!)
            assertEquals(LocalDate.of(2019, 4, 10), packetToFakta(packet).regelverksdato)
        }
    }

    @Test
    fun ` should get correct grunnbelop before new G`() {
        val json =
            """
        {
            "harAvtjentVerneplikt": true,
            "beregningsDato": "2019-05-20",
            "grunnlagResultat":{"beregningsregel": "BLA"}
        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(emptyInntekt)!!)

        val fakta = packetToFakta(packet)

        assertEquals(96883.toBigDecimal(), fakta.grunnbeløp)
    }

    @Test
    fun ` should get correct grunnbelop after new G`() {
        val json =
            """
        {
            "harAvtjentVerneplikt": true,
            "beregningsDato": "2019-05-27",
            "grunnlagResultat":{"beregningsregel": "BLA"}
        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(emptyInntekt)!!)

        val fakta = packetToFakta(packet)

        assertEquals(99858.toBigDecimal(), fakta.grunnbeløp)
    }

    @Test
    fun ` should map brukt_inntektsperiode from packet to Fakta `() {
        val json =
            """
        {
            "bruktInntektsPeriode": {"førsteMåned":"2019-02", "sisteMåned":"2019-03"},
            "beregningsDato": "2019-05-20",
            "grunnlagResultat":{"beregningsregel": "BLA"}
        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(emptyInntekt)!!)

        val fakta = packetToFakta(packet)

        assertEquals(YearMonth.of(2019, 2), fakta.bruktInntektsPeriode!!.førsteMåned)
        assertEquals(YearMonth.of(2019, 3), fakta.bruktInntektsPeriode!!.sisteMåned)
    }

    @Test
    fun ` should map inntekt from packet to Fakta `() {
        val json =
            """
        {
            "beregningsDato": "2019-05-20",
            "grunnlagResultat":{"beregningsregel": "BLA"}
        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(emptyInntekt)!!)

        val fakta = packetToFakta(packet)

        assertEquals("12345", fakta.inntekt.inntektsId)
    }

    @Test
    fun ` should map lærling from packet to Fakta `() {
        val json =
            """
        {
            "beregningsDato": "2019-05-20",
            "lærling": "true",
            "grunnlagResultat":{"beregningsregel": "BLA"}
        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(emptyInntekt)!!)

        val fakta = packetToFakta(packet)

        assertTrue(fakta.lærling)
    }
}
