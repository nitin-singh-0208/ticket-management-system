package com.ticketmanagement;

import com.ticketmanagement.rag.RagProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(RagProperties.class)
public class TicketManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketManagementApplication.class, args);
    }
}
