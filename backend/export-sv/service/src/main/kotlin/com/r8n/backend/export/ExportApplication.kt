package com.r8n.backend.export

import com.r8n.backend.mock.integration.configuration.MockRestClientConfiguration
import com.r8n.backend.users.integration.UsersRestClientConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication(
    exclude = [
        DataSourceAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class,
        DataSourceTransactionManagerAutoConfiguration::class,
    ],
)
@EnableAsync
@Import(
    value = [
        UsersRestClientConfiguration::class,
        MockRestClientConfiguration::class,
    ],
)
class ExportApplication

fun main(args: Array<String>) {
    runApplication<ExportApplication>(*args)
}