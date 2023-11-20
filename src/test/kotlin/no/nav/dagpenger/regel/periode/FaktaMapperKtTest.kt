package no.nav.dagpenger.regel.periode

import no.nav.helse.rapids_rivers.JsonMessage
import org.junit.jupiter.api.Test


class FaktaMapperKtTest {
    @Test
    fun hubba() {
        JsonMessage.newMessage(
            """{
                "beregningsDato": "2019-05-20",
                "grunnlagResultat":{"beregningsregel": "BLA"}
            }"""
        ).also {
            it.

        }
    }
}