package no.nav.dagpenger.regel.periode

import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication

class ApplicationBuilder(config: Map<String, String>) : RapidsConnection.StatusListener {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    private val rapidsConnection = RapidApplication.create(config)

    init {
        rapidsConnection.register(this)
        PeriodeBehovløser(rapidsConnection)
    }

    fun start() = rapidsConnection.start()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        logger.info { "Starter opp dp-regel-periode" }
    }
}
