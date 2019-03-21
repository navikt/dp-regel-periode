package no.nav.dagpenger.regel.periode

import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.specifications.Spesifikasjon
import java.math.BigDecimal

val verneplikt26Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-19 Dagpenger etter avtjent verneplikt",
    identitet = "VERNEPLIKT",
    implementasjon = { fakta ->
        if (fakta.verneplikt) {
            Evaluering.ja("Periode på 26 uker ihht § 4-19")
        } else {
            Evaluering.nei("Ikke periode på 26 uker ihht § 4-19")
        }
    }
)

val ordinærSiste12Måneder104Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), siste 12 mnd, 104 uker",
    identitet = "ORDINÆR_12_104",
    implementasjon = { fakta ->
        if (fakta.arbeidsinntektSiste12 >= (fakta.grunnbeløp.times(BigDecimal(2)))) {
            Evaluering.ja("Periode på 104 uker ihht § 4-15, siste 12 mnd")
        } else {
            Evaluering.nei("Ikke periode på 104 uker ihht § 4-15, siste 12 mnd")
        }
    }
)

val ordinærSiste36Måneder104Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), siste 36 mnd, 104 uker",
    identitet = "ORDINÆR_36_104",
    implementasjon = { fakta ->
        if (fakta.arbeidsinntektSiste36 >= (fakta.grunnbeløp.times(BigDecimal(2)))) {
            Evaluering.ja("Periode på 104 uker ihht § 4-15, siste 36 mnd")
        } else {
            Evaluering.nei("Ikke periode på 104 uker ihht § 4-15, siste 36 mnd")
        }
    }
)

val ordinærSiste12MånederMedFangstOgFiske104Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), med fangst og fisk, siste 12 mnd, 104 uker",
    identitet = "ORDINÆR_12_104_FANGSTOGFISK",
    implementasjon = { fakta ->
        if (fakta.fangstOgFisk && fakta.inntektSiste12inkludertFangstOgFiske >= (fakta.grunnbeløp.times(BigDecimal(2)))) {
            Evaluering.ja("Periode på 104 uker ihht dagpengeforskriften § 8-1, siste 12 mnd")
        } else {
            Evaluering.nei("Ikke periode på 104 uker ihht dagpengeforskriften § 8-1, siste 12 mnd")
        }
    }
)

val ordinærSiste36MånederMedFangstOgFiske104Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), med fangst og fisk, siste 36 mnd, 104 uker",
    identitet = "ORDINÆR_36_104_FANGSTOGFISK",
    implementasjon = { fakta ->
        if (fakta.fangstOgFisk && fakta.inntektSiste36inkludertFangstOgFiske >= (fakta.grunnbeløp.times(BigDecimal(2)))) {
            Evaluering.ja("Periode på 104 uker ihht dagpengeforskriften § 8-1, siste 36 mnd")
        } else {
            Evaluering.nei("Ikke periode på 104 uker ihht dagpengeforskriften § 8-1, siste 36 mnd")
        }
    }
)

val ordinærSiste12Måneder52Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), siste 12 mnd, 52 uker",
    identitet = "ORDINÆR_12_52",
    implementasjon = { fakta ->
        if (fakta.arbeidsinntektSiste12 < (fakta.grunnbeløp.times(BigDecimal(2)))) {
            Evaluering.ja("Periode på 52 uker ihht § 4-15, siste 12 mnd")
        } else {
            Evaluering.nei("Ikke periode på 52 uker ihht § 4-15, siste 12 mnd")
        }
    }
)

val ordinærSiste36Måneder52Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode, siste 36 mnd, 52 uker",
    identitet = "ORDINÆR_36_52",
    implementasjon = { fakta ->
        if (fakta.arbeidsinntektSiste36 < (fakta.grunnbeløp.times(BigDecimal(2)))) {
            Evaluering.ja("Periode på 52 uker ihht § 4-15, siste 36 mnd")
        } else {
            Evaluering.nei("Ikke periode på 52 uker ihht § 4-15, siste 36 mnd")
        }
    }
)

val ordinærSiste12MånederMedFangstOgFiske52Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), med fangst og fisk, siste 12 mnd, 52 uker",
    identitet = "ORDINÆR_12_52_FANGSTOGFISK",
    implementasjon = { fakta ->
        if (fakta.fangstOgFisk && fakta.inntektSiste12inkludertFangstOgFiske < (fakta.grunnbeløp.times(BigDecimal(2)))) {
            Evaluering.ja("Periode på 52 uker ihht dagpengeforskriften § 8-1, siste 12 mnd")
        } else {
            Evaluering.nei("Ikke periode på 52 uker ihht dagpengeforskriften § 8-1, siste 12 mnd")
        }
    }
)

val ordinærSiste36MånederMedFangstOgFiske52Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), med fangst og fisk, siste 36 mnd, 52 uker",
    identitet = "ORDINÆR_36_52_FANGSTOGFISK",
    implementasjon = { fakta ->
        if (fakta.fangstOgFisk && fakta.inntektSiste36inkludertFangstOgFiske < (fakta.grunnbeløp.times(BigDecimal(2)))) {
            Evaluering.ja("Periode på 52 uker ihht dagpengeforskriften § 8-1, siste 36 mnd")
        } else {
            Evaluering.nei("Ikke periode på 52 uker ihht dagpengeforskriften § 8-1, siste 36 mnd")
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
