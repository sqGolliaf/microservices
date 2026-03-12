package ru.sg.configserver

import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.config.server.EnableConfigServer
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.annotation.Bean


@EnableConfigServer
@SpringBootApplication
@RefreshScope
class ConfigserverApplication

fun main(args: Array<String>) {
    val run = runApplication<ConfigserverApplication>(*args)
}

fun testInject() {

}