package no.nav.dagpenger.regel.periode

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.specifications.Spesifikasjon

const val scale = 20
val roundingMode = RoundingMode.HALF_UP

val vernepiktPeriode = Spesifikasjon<Fakta>(
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
            erIkkeSærregel() && arbeidsinntektSiste12 >= (grunnbeløp.times(BigDecimal(2))) -> Evaluering.ja("104")
            else -> Evaluering.nei("0")
        }
    }
)

val ordinærSiste36Måneder104Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), siste 36 mnd, 104 uker",
    identifikator = "ORDINÆR_36_104",
    implementasjon = {
        when {
            erIkkeSærregel() && arbeidsinntektSiste36.divide(BigDecimal(3), scale, roundingMode) >= (grunnbeløp.times(BigDecimal(2))) -> Evaluering.ja("104")
            else -> Evaluering.nei("0")
        }
    }
)

val ordinærSiste12MånederMedFangstOgFiske104Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), med fangst og fisk, siste 12 mnd, 104 uker",
    identifikator = "ORDINÆR_12_104_FANGSTOGFISK",
    implementasjon = {
        when {
            erIkkeSærregel() && fangstOgFisk && inntektSiste12inkludertFangstOgFiske >= (grunnbeløp.times(BigDecimal(2))) -> Evaluering.ja("104")
            else -> Evaluering.nei("0")
        }
    }
)

val ordinærSiste36MånederMedFangstOgFiske104Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), med fangst og fisk, siste 36 mnd, 104 uker",
    identifikator = "ORDINÆR_36_104_FANGSTOGFISK",
    implementasjon = {
        when {
            erIkkeSærregel() && fangstOgFisk && inntektSiste36inkludertFangstOgFiske.divide(BigDecimal(3), scale, roundingMode) >= (grunnbeløp.times(BigDecimal(2))) -> Evaluering.ja("104")
            else -> Evaluering.nei("0")
        }
    }
)

val ordinærSiste12Måneder52Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), siste 12 mnd, 52 uker",
    identifikator = "ORDINÆR_12_52",
    implementasjon = {
        when {
            erIkkeSærregel() && arbeidsinntektSiste12 < (grunnbeløp.times(BigDecimal(2))) -> Evaluering.ja("52")
            else -> Evaluering.nei("0")
        }
    }
)

val ordinærSiste36Måneder52Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode, siste 36 mnd, 52 uker",
    identifikator = "ORDINÆR_36_52",
    implementasjon = {
        when {
            erIkkeSærregel() && arbeidsinntektSiste36.divide(BigDecimal(3), scale, roundingMode) < (grunnbeløp.times(BigDecimal(2))) -> Evaluering.ja("52")
            else -> Evaluering.nei("0")
        }
    }
)

val ordinærSiste12MånederMedFangstOgFiske52Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), med fangst og fisk, siste 12 mnd, 52 uker",
    identifikator = "ORDINÆR_12_52_FANGSTOGFISK",
    implementasjon = {
        when {
            erIkkeSærregel() && fangstOgFisk && inntektSiste12inkludertFangstOgFiske < (grunnbeløp.times(BigDecimal(2))) -> Evaluering.ja("52")
            else -> Evaluering.nei("0")
        }
    }
)

val ordinærSiste36MånederMedFangstOgFiske52Uker = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode), med fangst og fisk, siste 36 mnd, 52 uker",
    identifikator = "ORDINÆR_36_52_FANGSTOGFISK",
    implementasjon = {
        when {
            erIkkeSærregel() && fangstOgFisk && inntektSiste36inkludertFangstOgFiske.divide(BigDecimal(3), scale, roundingMode) < (grunnbeløp.times(BigDecimal(2))) -> Evaluering.ja("52")
            else -> Evaluering.nei("0")
        }
    }
)

val lærlingPeriode = Spesifikasjon<Fakta>(
    beskrivelse = "§ 2-6. Midlertidig inntekssikringsordning for lærlinger – unntak fra folketrygdloven § 4-4 til § 4-6",
    identifikator = "LÆRLING",
    implementasjon = {
        when {
            lærling && beregningsDato.erKoronaPeriode() -> Evaluering.ja("52")
            else -> Evaluering.nei("0")
        }
    }
)

fun LocalDate.erKoronaPeriode() = this in (LocalDate.of(2020, 3, 20)..LocalDate.of(2020, 12, 31))

val ordinær52: Spesifikasjon<Fakta> =
    (ordinærSiste12Måneder52Uker eller ordinærSiste36Måneder52Uker) eller (ordinærSiste12MånederMedFangstOgFiske52Uker eller ordinærSiste36MånederMedFangstOgFiske52Uker)

val ordinær104: Spesifikasjon<Fakta> =
    (ordinærSiste12Måneder104Uker eller ordinærSiste36Måneder104Uker) eller (ordinærSiste12MånederMedFangstOgFiske104Uker eller ordinærSiste36MånederMedFangstOgFiske104Uker)

val særregel = vernepiktPeriode eller lærlingPeriode

val ordinær: Spesifikasjon<Fakta> = ordinær52 eller ordinær104

val periode: Spesifikasjon<Fakta> = (ordinær eller særregel).med(
    identifikator = "PERIODE",
    beskrivelse = "Antall uker som gis i dagpengeperiode")
