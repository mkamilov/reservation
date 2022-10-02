package com.example.islandreservation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetReservationsResponse {
    private String reservationId;
    private String fullName;
    private String email;
    private List<LocalDate> arrivalDate;
    private List<LocalDate> departureDate;
}
