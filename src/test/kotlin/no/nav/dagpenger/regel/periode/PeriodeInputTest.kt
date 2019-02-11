package no.nav.dagpenger.regel.periode

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class PeriodeInputTest {

    @Test
    fun ` Process behov without inntekt and no tasks `() {

        val behov = SubsumsjonsBehov.Builder()
            .build()

        assert(shouldBeProcessed(behov))
    }

    @Test
    fun ` Process behov without inntekt and no hentinntekt task `() {

        val behov = SubsumsjonsBehov.Builder()
            .task(listOf("noe annet"))
            .build()

        assert(shouldBeProcessed(behov))
    }

    @Test
    fun ` Do not process behov without inntekt but with hentinntekt task `() {

        val behov = SubsumsjonsBehov.Builder()
            .task(listOf("hentInntekt"))
            .build()

        assertFalse(shouldBeProcessed(behov))
    }

    @Test
    fun ` Process behov with inntekt `() {

        val behov = SubsumsjonsBehov.Builder()
            .inntekt(Inntekt("", 0))
            .build()

        assert(shouldBeProcessed(behov))
    }

    @Test
    fun ` Do not reprocess behov with periodeSubsumsjon `() {

        val behov = SubsumsjonsBehov.Builder()
            .inntekt(Inntekt("", 0))
            .periodeSubsumsjon(PeriodeSubsumsjon("123", "987", "555", 26))
            .build()

        assertFalse(shouldBeProcessed(behov))
    }
}
