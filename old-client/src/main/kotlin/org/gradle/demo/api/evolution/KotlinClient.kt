@file:JvmName("KotlinClient")

package org.gradle.demo.api.evolution

private fun run(server: Server): String {
    server.stringProperty = "lajos"
    return server.stringProperty
}

fun main() {
    println(run(Server()))
}
