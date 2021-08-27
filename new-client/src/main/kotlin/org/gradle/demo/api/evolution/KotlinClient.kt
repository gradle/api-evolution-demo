@file:JvmName("KotlinClient")

package org.gradle.demo.api.evolution

private fun run(server: Server): String {
    server.testProperty.set("lajos")
    return server.testProperty.get()
}

fun main() {
    println(run(Server()))
}
