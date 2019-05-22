package no.nav.dagpenger.regel.periode

import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.specifications.Spesifikasjon
import java.math.BigDecimal
import java.math.RoundingMode

val scale = 20
val roundingMode = RoundingMode.HALF_UP

val verneplikt26Uker = Spesifikasjon<Fakta>(
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
            arbeidsinntektSiste36.divide(BigDecimal(3), scale, roundingMode) >= (grunnbeløp.times(BigDecimal(2))) -> Evaluering.ja("104")
            else -> Evaluering.nei("0")
        }
    }
)

val ordinærSiste12MånederMedFangstOgFiske104Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), med fangst og fisk, siste 12 mnd, 104 uker",
    identifikator = "ORDINÆR_12_104_FANGSTOGFISK",
    implementasjon = {
        when {
            fangstOgFisk && inntektSiste12inkludertFangstOgFiske >= (grunnbeløp.times(BigDecimal(2))) -> Evaluering.ja("104")
            else -> Evaluering.nei("0")
        }
    }
)

val ordinærSiste36MånederMedFangstOgFiske104Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), med fangst og fisk, siste 36 mnd, 104 uker",
    identifikator = "ORDINÆR_36_104_FANGSTOGFISK",
    implementasjon = {
        when {
            fangstOgFisk && inntektSiste36inkludertFangstOgFiske.divide(BigDecimal(3), scale, roundingMode) >= (grunnbeløp.times(BigDecimal(2))) -> Evaluering.ja("104")
            else -> Evaluering.nei("0")
        }
    }
)

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
            arbeidsinntektSiste36.divide(BigDecimal(3), scale, roundingMode) < (grunnbeløp.times(BigDecimal(2))) -> Evaluering.ja("52")
            else -> Evaluering.nei("0")
        }
    }
)

val ordinærSiste12MånederMedFangstOgFiske52Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), med fangst og fisk, siste 12 mnd, 52 uker",
    identifikator = "ORDINÆR_12_52_FANGSTOGFISK",
    implementasjon = {
        when {
            fangstOgFisk && inntektSiste12inkludertFangstOgFiske < (grunnbeløp.times(BigDecimal(2))) -> Evaluering.ja("52")
            else -> Evaluering.nei("0")
        }
    }
)

val ordinærSiste36MånederMedFangstOgFiske52Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), med fangst og fisk, siste 36 mnd, 52 uker",
    identifikator = "ORDINÆR_36_52_FANGSTOGFISK",
    implementasjon = {
        when {
            fangstOgFisk && inntektSiste36inkludertFangstOgFiske.divide(BigDecimal(3), scale, roundingMode) < (grunnbeløp.times(BigDecimal(2))) -> Evaluering.ja("52")
            else -> Evaluering.nei("0")
        }
    }
)

val ordinær52: Spesifikasjon<Fakta> =
    (ordinærSiste12Måneder52Uker eller ordinærSiste36Måneder52Uker) eller (ordinærSiste12MånederMedFangstOgFiske52Uker eller ordinærSiste36MånederMedFangstOgFiske52Uker)

val ordinær104: Spesifikasjon<Fakta> =
    (ordinærSiste12Måneder104Uker eller ordinærSiste36Måneder104Uker) eller (ordinærSiste12MånederMedFangstOgFiske104Uker eller ordinærSiste36MånederMedFangstOgFiske104Uker)

val ordinær: Spesifikasjon<Fakta> = ordinær52 eller ordinær104

val periode: Spesifikasjon<Fakta> = ordinær eller verneplikt26Uker
