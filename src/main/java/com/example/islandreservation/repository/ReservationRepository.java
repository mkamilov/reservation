package com.example.islandreservation.repository;

import com.example.islandreservation.repository.model.ReservationEntity;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository {
    List<LocalDate> getAvailability(final LocalDate startDate, final LocalDate endDate);
    ReservationEntity getReservation(final String email, final String reservationId);
    void createReservationIfAvailable(final ReservationEntity reservation);
    void updateReservationIfAvailable(final ReservationEntity reservation);
    void deleteReservation(final String email, final String reservationId);
}
