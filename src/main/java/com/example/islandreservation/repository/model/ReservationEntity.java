package com.example.islandreservation.repository.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationEntity {
    private String reservationId;
    private String fullName;
    private String email;
    private LocalDate arrivalDate;
    private LocalDate departureDate;
}
