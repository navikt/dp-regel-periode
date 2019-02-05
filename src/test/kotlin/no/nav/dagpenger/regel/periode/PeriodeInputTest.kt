package no.nav.dagpenger.regel.periode

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PeriodeInputTest {

    @Test
    fun `Process behov without inntekt and no tasks`() {
        val behov = SubsumsjonsBehov(
                JSONObject()
                        .put("vedtaksId", "123456")
                        .put("aktorId", 123)
                        .put("beregningsDato", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
        )

        assert(shouldBeProcessed(behov))
    }

    @Test
    fun `Process behov without inntekt and no hentinntekt task`() {
        val behov = SubsumsjonsBehov(
                JSONObject()
                        .put("vedtaksId", "123456")
                        .put("aktorId", 123)
                        .put("beregningsDato", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .put("tasks", listOf("noe annet"))
        )

        assert(shouldBeProcessed(behov))
    }

    @Test
    fun `Do not process behov without inntekt but with hentinntekt task`() {
        val behov = SubsumsjonsBehov(
                JSONObject()
                        .put("vedtaksId", "123456")
                        .put("aktorId", 123)
                        .put("beregningsDato", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .put("tasks", listOf("hentInntekt"))
        )

        assertFalse(shouldBeProcessed(behov))
    }

    @Test
    fun `Process behov with inntekt`() {
        val behov = SubsumsjonsBehov(
                JSONObject()
                        .put("vedtaksId", "123456")
                        .put("aktorId", 123)
                        .put("beregningsDato", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .put("inntekt", 15)
        )

        assert(shouldBeProcessed(behov))
    }

    @Test
    fun `Do not reprocess behov with periodeSubsumsjon`() {
        val behov = SubsumsjonsBehov(
                JSONObject()
                        .put("vedtaksId", "123456")
                        .put("aktorId", 123)
                        .put("beregningsDato", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .put("inntekt", 0)
                        .put("periodeSubsumsjon", JSONObject()
                                .put("sporingsId", "123")
                                .put("subsumsjonsId", "456")
                                .put("regelIdentifikator", "Periode.v1")
                                .put("antallUker", 0)
                        ))

        assertFalse(shouldBeProcessed(behov))
    }
}
