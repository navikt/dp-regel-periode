package no.nav.dagpenger.regel.periode

import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.specifications.Spesifikasjon
import java.math.BigDecimal
import java.math.RoundingMode

val scale = 20
val roundingMode = RoundingMode.HALF_UP

val verneplikt26Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-19 Dagpenger etter avtjent verneplikt",
    identitet = "VERNEPLIKT",
    implementasjon = { fakta ->
        if (fakta.verneplikt) {
            Evaluering.ja("26")
        } else {
            Evaluering.nei("0")
        }
    }
)

val ordinærSiste12Måneder104Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), siste 12 mnd, 104 uker",
    identitet = "ORDINÆR_12_104",
    implementasjon = { fakta ->
        if (fakta.arbeidsinntektSiste12 >= (fakta.grunnbeløp.times(BigDecimal(2)))) {
            Evaluering.ja("104")
        } else {
            Evaluering.nei("0")
        }
    }
)

val ordinærSiste36Måneder104Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), siste 36 mnd, 104 uker",
    identitet = "ORDINÆR_36_104",
    implementasjon = { fakta ->
        if (fakta.arbeidsinntektSiste36.divide(BigDecimal(3), scale, roundingMode) >= (fakta.grunnbeløp.times(BigDecimal(2)))) {
            Evaluering.ja("104")
        } else {
            Evaluering.nei("0")
        }
    }
)

val ordinærSiste12MånederMedFangstOgFiske104Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), med fangst og fisk, siste 12 mnd, 104 uker",
    identitet = "ORDINÆR_12_104_FANGSTOGFISK",
    implementasjon = { fakta ->
        if (fakta.fangstOgFisk && fakta.inntektSiste12inkludertFangstOgFiske >= (fakta.grunnbeløp.times(BigDecimal(2)))) {
            Evaluering.ja("104")
        } else {
            Evaluering.nei("0")
        }
    }
)

val ordinærSiste36MånederMedFangstOgFiske104Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), med fangst og fisk, siste 36 mnd, 104 uker",
    identitet = "ORDINÆR_36_104_FANGSTOGFISK",
    implementasjon = { fakta ->
        if (fakta.fangstOgFisk && fakta.inntektSiste36inkludertFangstOgFiske.divide(BigDecimal(3), scale, roundingMode) >= (fakta.grunnbeløp.times(BigDecimal(2)))) {
            Evaluering.ja("104")
        } else {
            Evaluering.nei("0")
        }
    }
)

val ordinærSiste12Måneder52Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), siste 12 mnd, 52 uker",
    identitet = "ORDINÆR_12_52",
    implementasjon = { fakta ->
        if (fakta.arbeidsinntektSiste12 < (fakta.grunnbeløp.times(BigDecimal(2)))) {
            Evaluering.ja("52")
        } else {
            Evaluering.nei("0")
        }
    }
)

val ordinærSiste36Måneder52Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode, siste 36 mnd, 52 uker",
    identitet = "ORDINÆR_36_52",
    implementasjon = { fakta ->
        if (fakta.arbeidsinntektSiste36.divide(BigDecimal(3), scale, roundingMode) < (fakta.grunnbeløp.times(BigDecimal(2)))) {
            Evaluering.ja("52")
        } else {
            Evaluering.nei("0")
        }
    }
)

val ordinærSiste12MånederMedFangstOgFiske52Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), med fangst og fisk, siste 12 mnd, 52 uker",
    identitet = "ORDINÆR_12_52_FANGSTOGFISK",
    implementasjon = { fakta ->
        if (fakta.fangstOgFisk && fakta.inntektSiste12inkludertFangstOgFiske < (fakta.grunnbeløp.times(BigDecimal(2)))) {
            Evaluering.ja("52")
        } else {
            Evaluering.nei("0")
        }
    }
)

val ordinærSiste36MånederMedFangstOgFiske52Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), med fangst og fisk, siste 36 mnd, 52 uker",
    identitet = "ORDINÆR_36_52_FANGSTOGFISK",
    implementasjon = { fakta ->
        if (fakta.fangstOgFisk && fakta.inntektSiste36inkludertFangstOgFiske.divide(BigDecimal(3), scale, roundingMode) < (fakta.grunnbeløp.times(BigDecimal(2)))) {
            Evaluering.ja("52")
        } else {
            Evaluering.nei("0")
        }
    }
)

val ordinær52: Spesifikasjon<Fakta> =
    (ordinærSiste12Måneder52Uker eller ordinærSiste36Måneder52Uker) eller (ordinærSiste12MånederMedFangstOgFiske52Uker eller ordinærSiste36MånederMedFangstOgFiske52Uker)
        .med(
            identitet = "ORDINÆR52",
            beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode)"
        )

val ordinær104: Spesifikasjon<Fakta> =
    (ordinærSiste12Måneder104Uker eller ordinærSiste36Måneder104Uker) eller (ordinærSiste12MånederMedFangstOgFiske104Uker eller ordinærSiste36MånederMedFangstOgFiske104Uker)
        .med(
            identitet = "ORDINÆR104",
            beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode)"
        )

val ordinær: Spesifikasjon<Fakta> = ordinær52 eller ordinær104
    .med(
        identitet = "ORDINÆR",
        beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode)"
    )

val periode: Spesifikasjon<Fakta> = ordinær eller verneplikt26Uker
    .med(
        identitet = "PERIODE",
        beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode)"
    )
