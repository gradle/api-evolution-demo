@file:JvmName("KotlinClient")

package org.gradle.demo.api.evolution

fun main() {
    val server = Server()
    server.testProperty = "lajos"
    println(server.testProperty)
}
