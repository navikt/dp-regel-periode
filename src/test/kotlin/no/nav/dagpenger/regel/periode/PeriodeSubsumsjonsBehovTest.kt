package no.nav.dagpenger.regel.periode

import org.json.JSONException
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PeriodeSubsumsjonsBehovTest {

    val emptyjsonBehov = """
            {}
            """.trimIndent()
    val emptyjsonObject = JsonDeserializer().deserialize(null, emptyjsonBehov.toByteArray())!!
    val emptysubsumsjonsBehov = SubsumsjonsBehov(emptyjsonObject)

    val jsonBehovMedInntekt = """
            {
                "inntekt": 0
            }
            """.trimIndent()
    val jsonObjectMedInntekt = JsonDeserializer().deserialize(null, jsonBehovMedInntekt.toByteArray())!!
    val subsumsjonsBehovMedInntekt = SubsumsjonsBehov(jsonObjectMedInntekt)

    val jsonBehovMedPeriodeSubsumsjon = """
            {
                "periodeResultat": {}
            }
            """.trimIndent()
    val jsonObjectMedPeriodeSubsumsjon = JsonDeserializer().deserialize(null, jsonBehovMedPeriodeSubsumsjon.toByteArray())!!
    val subsumsjonsBehovMedPeriodeSubsumsjon = SubsumsjonsBehov(jsonObjectMedPeriodeSubsumsjon)

    val jsonBehovMedHentInntektTask = """
            {
                "tasks": ["hentInntekt"]
            }
            """.trimIndent()
    val jsonObjectMedHentInntektTask = JsonDeserializer().deserialize(null, jsonBehovMedHentInntektTask.toByteArray())!!
    val subsumsjonsBehovMedHentInntektTask = SubsumsjonsBehov(jsonObjectMedHentInntektTask)

    val jsonBehovMedAnnenTask = """
            {
                "tasks": ["annen task"]
            }
            """.trimIndent()
    val jsonObjectMedAnnenTask = JsonDeserializer().deserialize(null, jsonBehovMedAnnenTask.toByteArray())!!
    val subsumsjonsBehovAnnentTask = SubsumsjonsBehov(jsonObjectMedAnnenTask)

    val jsonBehovMedFlereTasks = """
            {
                "tasks": ["annen task", "hentInntekt"]
            }
            """.trimIndent()
    val jsonObjectMedFlereTasks = JsonDeserializer().deserialize(null, jsonBehovMedFlereTasks.toByteArray())!!
    val subsumsjonsBehovFleretTasks = SubsumsjonsBehov(jsonObjectMedFlereTasks)

    val jsonBehovMedInntektogPeriodeSubsumsjon = """
            {
                "inntekt": 0,
                "periodeResultat": {}
            }
            """.trimIndent()
    val jsonObjectMedInntektogPeriodeSubsumsjon = JsonDeserializer().deserialize(null, jsonBehovMedInntektogPeriodeSubsumsjon.toByteArray())!!
    val subsumsjonsBehovMedInntektogPeriodeSubsumsjon = SubsumsjonsBehov(jsonObjectMedInntektogPeriodeSubsumsjon)

    val jsonBehovMedInntektogHentInntektTask = """
            {
                "inntekt": 0,
                "tasks": ["hentInntekt"]
            }
            """.trimIndent()
    val jsonObjectMedInntektogHentInntektTask = JsonDeserializer().deserialize(null, jsonBehovMedInntektogHentInntektTask.toByteArray())!!
    val subsumsjonsBehovMedInntektogHentInntektTask = SubsumsjonsBehov(jsonObjectMedInntektogHentInntektTask)

    val jsonBehovMedVernepliktTrue = """
            {
                "avtjentVerneplikt": true
            }
            """.trimIndent()
    val jsonObjectMedVernepliktTrue = JsonDeserializer().deserialize(null, jsonBehovMedVernepliktTrue.toByteArray())!!
    val subsumsjonsBehovmedVernepliktTrue = SubsumsjonsBehov(jsonObjectMedVernepliktTrue)

    val jsonBehovMedVernepliktFalse = """
            {
                "avtjentVerneplikt": false
            }
            """.trimIndent()
    val jsonObjectMedVernepliktFalse = JsonDeserializer().deserialize(null, jsonBehovMedVernepliktFalse.toByteArray())!!
    val subsumsjonsBehovmedVernepliktFalse = SubsumsjonsBehov(jsonObjectMedVernepliktFalse)

    @Test
    fun ` Should need hentInntektsTask when there is no hentInntektsTask and no inntekt `() {

        assert(emptysubsumsjonsBehov.needsHentInntektsTask())
        assertFalse(subsumsjonsBehovMedInntekt.needsHentInntektsTask())
        assertFalse(subsumsjonsBehovMedHentInntektTask.needsHentInntektsTask())
        assertFalse(subsumsjonsBehovMedInntektogHentInntektTask.needsHentInntektsTask())
    }

    @Test
    fun ` Should need periodeSubsumsjon when there is inntekt and no periodeSubsumsjon `() {

        assert(subsumsjonsBehovMedInntekt.needsPeriodeSubsumsjon())
        assertFalse(emptysubsumsjonsBehov.needsPeriodeSubsumsjon())
        assertFalse(subsumsjonsBehovMedInntektogPeriodeSubsumsjon.needsPeriodeSubsumsjon())
        assertFalse(subsumsjonsBehovMedPeriodeSubsumsjon.needsPeriodeSubsumsjon())
    }

    @Test
    fun ` Should have periodeSubsumsjon when it has periodeSubsumsjon `() {

        assert(subsumsjonsBehovMedPeriodeSubsumsjon.hasPeriodeSubsumsjon())
        assertFalse(emptysubsumsjonsBehov.hasPeriodeSubsumsjon())
    }

    @Test
    fun ` Should have inntekt when it has inntekt `() {

        assert(subsumsjonsBehovMedInntekt.hasInntekt())
        assertFalse(emptysubsumsjonsBehov.hasInntekt())
    }

    @Test
    fun ` Should have hentInntektTask when it has hentInntektTask `() {

        assert(subsumsjonsBehovMedHentInntektTask.hasHentInntektTask())
        assert(subsumsjonsBehovFleretTasks.hasHentInntektTask())
        assertFalse(emptysubsumsjonsBehov.hasHentInntektTask())
        assertFalse(subsumsjonsBehovAnnentTask.hasHentInntektTask())
    }

    @Test
    fun ` Should have tasks when it has tasks `() {

        assert(subsumsjonsBehovMedHentInntektTask.hasTasks())
        assert(subsumsjonsBehovAnnentTask.hasTasks())
        assert(subsumsjonsBehovFleretTasks.hasTasks())
        assertFalse(emptysubsumsjonsBehov.hasTasks())
    }

    @Test
    fun ` Should be able to add tasks `() {
        val subsumsjonsBehov = emptysubsumsjonsBehov

        assertFalse(subsumsjonsBehov.hasTasks())

        subsumsjonsBehov.addTask("Annen Task")

        assert(subsumsjonsBehov.hasTasks())
        assertFalse(subsumsjonsBehov.hasHentInntektTask())

        subsumsjonsBehov.addTask("hentInntekt")

        assert(subsumsjonsBehov.hasTasks())
        assert(subsumsjonsBehov.hasHentInntektTask())
    }

    @Test
    fun ` Should be able to return verneplikt `() {

        assert(subsumsjonsBehovmedVernepliktTrue.getAvtjentVerneplikt())
        assertFalse(subsumsjonsBehovmedVernepliktFalse.getAvtjentVerneplikt())
        assertFalse(emptysubsumsjonsBehov.getAvtjentVerneplikt())
    }

    @Test
    fun ` Should be able to add periodeSubsumsjon `() {
        val subsumsjonsBehov = emptysubsumsjonsBehov

        assertFalse(subsumsjonsBehov.hasPeriodeSubsumsjon())

        val periodeSubsumsjon = SubsumsjonsBehov.PeriodeSubsumsjon("123", "456", "REGEL", 0)
        subsumsjonsBehov.addPeriodeSubsumsjon(periodeSubsumsjon)

        assert(subsumsjonsBehov.hasPeriodeSubsumsjon())
    }

    @Test
    fun ` Should be able to return inntekt `() {

        assertEquals(0, subsumsjonsBehovMedInntekt.getInntekt())
        assertThrows<JSONException> { emptysubsumsjonsBehov.getInntekt() }
    }
}