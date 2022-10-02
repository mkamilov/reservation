package com.example.islandreservation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReservationRequest {
    private String fullName;
    @NonNull
    private String email;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate arrivalDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate departureDate;
}
