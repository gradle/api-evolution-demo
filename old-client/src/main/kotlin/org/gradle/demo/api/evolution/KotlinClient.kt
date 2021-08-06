@file:JvmName("KotlinClient")

package org.gradle.demo.api.evolution

fun main() {
    val server = Server()
    server.name = "lajos"
    println(server.name)
}
