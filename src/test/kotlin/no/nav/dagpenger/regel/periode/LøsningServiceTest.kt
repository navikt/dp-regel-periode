package no.nav.dagpenger.regel.periode

import com.fasterxml.jackson.databind.JsonNode
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.ints.shouldBeExactly
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

internal class LøsningServiceTest {

    private val rapid = TestRapid().apply {
        LøsningService(this)
    }

    @Test
    fun `skal legge på periode-løsning på pakker vi forstår`() {

        rapid.sendTestMessage("sldjjfnqaolsdjcb")
        rapid.sendTestMessage(json)

        val inspektør = rapid.inspektør
        inspektør.size shouldBeExactly 1
        inspektør.field(0, "@behov").map(JsonNode::asText).shouldContain("Periode")
        /*inspektør.field(0, "@løsning")["Periode"].shouldHave("")*/
    }

    @Language("JSON")
    private val json =
        """
            {
              "@behov": [
                "Periode"
              ],
              "@id": "123",
              "beregningsdato": "2020-04-01",
              "grunnlagResultat": {
                "beregningsregel": "ORDINAER"
              },
              "bruktInntektsPeriode": {
                "førsteMåned": "2019-12",
                "sisteMåned": "2020-04"
              },
              "inntektV1": {
                "inntektsId": "BLA",
                "manueltRedigert": false,
                "sisteAvsluttendeKalenderMåned": "2020-04",
                "inntektsListe": [
                  {
                    "årMåned": "2020-01",
                    "klassifiserteInntekter": [
                      {
                        "beløp": 100000,
                        "inntektKlasse": "ARBEIDSINNTEKT"
                      }
                    ]
                  }
                ]
              }
           }
            """.trimIndent()
}
