package no.nav.dagpenger.regel.periode

import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.specifications.Spesifikasjon
import java.math.BigDecimal

val ordinærSiste12Måneder = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode)",
    identitet = "ORDINÆR_12",
    implementasjon = { fakta ->
        if (fakta.arbeidsinntektSiste12 > (fakta.grunnbeløp.times(BigDecimal(2)))) {
            Evaluering.ja("Periode på 104 uker ihht § 4-15")
        } else {
            Evaluering.nei("Ikke periode på 104 uker ihht § 4-15")
        }
    }
)

val ordinærSiste36Måneder = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-15 Antall stønadsuker (stønadsperiode)",
    identitet = "ORDINÆR_36",
    implementasjon = { fakta ->
        if (fakta.arbeidsinntektSiste12 > (fakta.grunnbeløp.times(BigDecimal(2)))) {
            Evaluering.ja("Periode på 104 uker ihht § 4-15")
        } else {
            Evaluering.nei("Ikke periode på 104 uker ihht § 4-15")
        }
    }
)
