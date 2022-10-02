package com.example.islandreservation;

import com.example.islandreservation.repository.InMemoryReservationRepository;
import com.example.islandreservation.repository.ReservationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication
public class IslandReservationApplication {

    public static void main(String[] args) {

        SpringApplication.run(IslandReservationApplication.class, args);
    }

    @Bean
    public ReservationRepository reservationRepository() {
        return new InMemoryReservationRepository();
    }

}
