package no.nav.dagpenger.regel.periode

import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.assertions.json.shouldEqualSpecifiedJsonIgnoringOrder
import io.kotest.matchers.shouldBe
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class PeriodeBehovløserTest {
    private val testrapid = TestRapid()

    init {
        PeriodeBehovløser(testrapid)
    }

    @Test
    fun ` Should add PeriodeSubsumsjon`() {
        testrapid.sendTestMessage(inputJson)

        testrapid.inspektør.size shouldBe 1
        testrapid.inspektør.message(0).toString().let { resultJson ->
            resultJson shouldEqualSpecifiedJsonIgnoringOrder """
{
   "periodeResultat": {
     "regelIdentifikator": "Periode.v1",
     "periodeAntallUker": 52
   }
 }
 """
        }
    }

    @Test
    fun ` Should add PeriodeSubsumsjon with oppfyllerKravTilFangstOgFisk`() {
        testrapid.sendTestMessage(TestData.oppfyllerKravTilFangstOgFiskeJson)

        testrapid.inspektør.size shouldBe 1
        testrapid.inspektør.message(0).toString().let { resultJson: String ->
            resultJson.shouldContainJsonKeyValue(
                path = "$.periodeResultat.periodeAntallUker",
                value = 52,
            )
        }
    }

    @Test
    fun `Vernepliktperiode burde være 26 uker`() {
        testrapid.sendTestMessage(vernePliktigJson)

        testrapid.inspektør.size shouldBe 1
        testrapid.inspektør.message(0).toString().let { resultJson: String ->
            resultJson.shouldContainJsonKeyValue(
                path = "$.periodeResultat.periodeAntallUker",
                value = 26,
            )
        }
    }

    private companion object TestData {
        @Language("JSON")
        val oppfyllerKravTilFangstOgFiskeJson = """
        {
          "beregningsDato": "2018-04-06",
          "harAvtjentVerneplikt": false,
          "oppfyllerKravTilFangstOgFisk": true,
          "grunnlagResultat": {
            "beregningsregel": "BLA"
          },
          "bruktInntektsPeriode": {
            "førsteMåned": "2016-02",
            "sisteMåned": "2016-11"
          },
          "inntektV1": {
            "inntektsId": "12345",
            "inntektsListe": [
              {
                "årMåned": "2018-02",
                "klassifiserteInntekter": [
                  {
                    "beløp": "99999",
                    "inntektKlasse": "FANGST_FISKE"
                  }
                ]
              }
            ],
            "manueltRedigert": false,
            "sisteAvsluttendeKalenderMåned": "2019-02"
          }
        }
        """

        @Language("JSON")
        val vernePliktigJson =
            """
            {
              "harAvtjentVerneplikt": true,
              "beregningsDato": "2020-05-20",
              "grunnlagResultat": {
                "beregningsregel": "Verneplikt"
              },
              "inntektV1": {
                "inntektsId": "123",
                "inntektsListe": [],
                "manueltRedigert": false,
                "sisteAvsluttendeKalenderMåned": "2021-04"
              }
            } 
            """.trimIndent()

        @Language("JSON")
        val inputJson = """
        {
          "beregningsDato": "2019-02-27",
          "harAvtjentVerneplikt": false,
          "grunnlagResultat": {
            "beregningsregel": "BLA"
          },
          "bruktInntektsPeriode": {
            "førsteMåned": "2016-02",
            "sisteMåned": "2016-11"
          },
          "inntektV1": {
            "inntektsId": "12345",
            "inntektsListe": [
              {
                "årMåned": "2019-02",
                "klassifiserteInntekter": [
                  {
                    "beløp": "25000",
                    "inntektKlasse": "ARBEIDSINNTEKT"
                  }
                ]
              }
            ],
            "manueltRedigert": false,
            "sisteAvsluttendeKalenderMåned": "2019-02"
          }
        }
        """
    }
}
