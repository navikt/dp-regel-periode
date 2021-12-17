package no.nav.dagpenger.regel.periode

import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.specifications.Spesifikasjon
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

const val scale = 20
val roundingMode = RoundingMode.HALF_UP

val vernepliktPeriode = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-19 Dagpenger etter avtjent verneplikt",
    identifikator = "VERNEPLIKT",
    implementasjon = {
        when {
            verneplikt -> Evaluering.ja("26")
            else -> Evaluering.nei("0")
        }
    }
)

val ordinærSiste12Måneder104Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), siste 12 mnd, 104 uker",
    identifikator = "ORDINÆR_12_104",
    implementasjon = {
        when {
            arbeidsinntektSiste12 >= (grunnbeløp.times(BigDecimal(2))) -> Evaluering.ja("104")
            else -> Evaluering.nei("0")
        }
    }
)

val ordinærSiste36Måneder104Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), siste 36 mnd, 104 uker",
    identifikator = "ORDINÆR_36_104",
    implementasjon = {
        when {
            arbeidsinntektSiste36.divide(
                BigDecimal(3),
                scale,
                roundingMode
            ) >= (grunnbeløp.times(BigDecimal(2))) -> Evaluering.ja("104")
            else -> Evaluering.nei("0")
        }
    }
)

val ordinærSiste12MånederMedFangstOgFiske104Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), med fangst og fisk, siste 12 mnd, 104 uker",
    identifikator = "ORDINÆR_12_104_FANGSTOGFISK",
    implementasjon = {
        when {
            erGyldigFangstOgFisk() && inntektSiste12inkludertFangstOgFiske >= (grunnbeløp.times(BigDecimal(2))) -> Evaluering.ja("104")
            else -> Evaluering.nei("0")
        }
    }
)

val ordinærSiste36MånederMedFangstOgFiske104Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), med fangst og fisk, siste 36 mnd, 104 uker",
    identifikator = "ORDINÆR_36_104_FANGSTOGFISK",
    implementasjon = {
        when {
            erGyldigFangstOgFisk() && inntektSiste36inkludertFangstOgFiske.divide(
                BigDecimal(3),
                scale,
                roundingMode
            ) >= (grunnbeløp.times(BigDecimal(2))) -> Evaluering.ja("104")
            else -> Evaluering.nei("0")
        }
    }
)

internal fun Fakta.erGyldigFangstOgFisk(): Boolean {
    val fangstOgFiskAvvikletFra = LocalDate.of(2022, 1, 1)
    return (fangstOgFisk && regelverksdato < fangstOgFiskAvvikletFra)
}

val ordinærSiste12Måneder52Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), siste 12 mnd, 52 uker",
    identifikator = "ORDINÆR_12_52",
    implementasjon = {
        when {
            arbeidsinntektSiste12 < (grunnbeløp.times(BigDecimal(2))) -> Evaluering.ja("52")
            else -> Evaluering.nei("0")
        }
    }
)

val ordinærSiste36Måneder52Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode, siste 36 mnd, 52 uker",
    identifikator = "ORDINÆR_36_52",
    implementasjon = {
        when {
            arbeidsinntektSiste36.divide(
                BigDecimal(3),
                scale,
                roundingMode
            ) < (grunnbeløp.times(BigDecimal(2))) -> Evaluering.ja("52")
            else -> Evaluering.nei("0")
        }
    }
)

val ordinærSiste12MånederMedFangstOgFiske52Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), med fangst og fisk, siste 12 mnd, 52 uker",
    identifikator = "ORDINÆR_12_52_FANGSTOGFISK",
    implementasjon = {
        when {
            erGyldigFangstOgFisk() && inntektSiste12inkludertFangstOgFiske < (grunnbeløp.times(BigDecimal(2))) -> Evaluering.ja("52")
            else -> Evaluering.nei("0")
        }
    }
)

val ordinærSiste36MånederMedFangstOgFiske52Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), med fangst og fisk, siste 36 mnd, 52 uker",
    identifikator = "ORDINÆR_36_52_FANGSTOGFISK",
    implementasjon = {
        when {
            erGyldigFangstOgFisk() && inntektSiste36inkludertFangstOgFiske.divide(
                BigDecimal(3),
                scale,
                roundingMode
            ) < (grunnbeløp.times(BigDecimal(2))) -> Evaluering.ja("52")
            else -> Evaluering.nei("0")
        }
    }
)

val lærlingPeriodeKorona = Spesifikasjon<Fakta>(
    beskrivelse = "§ 2-6. Midlertidig inntekssikringsordning for lærlinger – unntak fra folketrygdloven § 4-4 til § 4-6",
    identifikator = "LÆRLING",
    implementasjon = {
        when {
            erlærling() -> Evaluering.ja("52")
            else -> Evaluering.nei("0")
        }
    }
)

val fangstOgFiske52 = ordinærSiste12MånederMedFangstOgFiske52Uker eller ordinærSiste36MånederMedFangstOgFiske52Uker
val fangstOgFiske104 = ordinærSiste12MånederMedFangstOgFiske104Uker eller ordinærSiste36MånederMedFangstOgFiske104Uker

val ordinær52: Spesifikasjon<Fakta> =
    (ordinærSiste12Måneder52Uker eller ordinærSiste36Måneder52Uker) eller fangstOgFiske52

val ordinær104: Spesifikasjon<Fakta> =
    (ordinærSiste12Måneder104Uker eller ordinærSiste36Måneder104Uker) eller fangstOgFiske104

val særregel = lærlingPeriodeKorona eller vernepliktPeriode

val ordinær: Spesifikasjon<Fakta> = ordinær52 eller ordinær104

val periode: Spesifikasjon<Fakta> = Spesifikasjon(
    identifikator = "PERIODE",
    beskrivelse = "Antall uker som gis i dagpengeperiode",
    implementasjon = {
        when (erSærregel()) {
            true -> særregel.evaluer(this)
            false -> ordinær.evaluer(this)
        }
    }
)
