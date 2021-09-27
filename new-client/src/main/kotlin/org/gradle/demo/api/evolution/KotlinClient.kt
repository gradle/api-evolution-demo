@file:JvmName("KotlinClient")

package org.gradle.demo.api.evolution

private fun run(server: Server): String {
    server.stringProperty.set("lajos")
    return server.stringProperty.get()
}

fun main() {
    println(run(Server()))
}
