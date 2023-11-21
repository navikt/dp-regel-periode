package no.nav.dagpenger.regel.periode

import java.net.URI

data class Problem(
    val type: URI = URI.create("about:blank"),
    val title: String,
    val status: Int = 500,
    val instance: URI = URI.create("about:blank"),
) {
    companion object {
        fun fromJson(json: Map<String, Any>): Problem {
            return Problem(
                URI.create(json["type"] as String),
                json["title"] as String,
                json["status"].toString().toInt(),
                URI.create(json["instance"] as String),
            )
        }
    }

    val toMap: Map<String, Any>
        get() =
            mapOf(
                "type" to type,
                "title" to title,
                "status" to status,
                "instance" to instance,
            )
}
