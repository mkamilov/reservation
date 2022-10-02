package com.example.islandreservation.service;

import com.example.islandreservation.model.CreateReservationRequest;
import com.example.islandreservation.model.GetReservationResponse;
import com.example.islandreservation.repository.model.ReservationEntity;
import com.example.islandreservation.model.UpdateReservationRequest;
import com.example.islandreservation.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository repository;

    ReservationService(ReservationRepository repository) {
        this.repository = repository;
    }

    public List<LocalDate> getAvailability(final LocalDate arrivalDate, final LocalDate departureDate) {
        return repository.getAvailability(arrivalDate, departureDate);
    }

    public String createReservation(final CreateReservationRequest createReservationRequest) {
        String reservationId = UUID.randomUUID().toString();
        ReservationEntity reservation = new ReservationEntity(reservationId, createReservationRequest.getFullName(),
                createReservationRequest.getEmail(), createReservationRequest.getArrivalDate(), createReservationRequest.getDepartureDate());
        repository.createReservationIfAvailable(reservation);
        return reservationId;
    }

    public GetReservationResponse getReservation(final String email, final String reservationId) {
       ReservationEntity reservationEntity = repository.getReservation(email, reservationId);
       return new GetReservationResponse(reservationEntity.getReservationId(), reservationEntity.getFullName(),
               reservationEntity.getEmail(), reservationEntity.getArrivalDate(), reservationEntity.getDepartureDate());
    }

    public String updateReservation(final String reservationId, final UpdateReservationRequest updateReservationRequest) {
        ReservationEntity reservation = new ReservationEntity(reservationId,
                updateReservationRequest.getFullName(), updateReservationRequest.getEmail(), updateReservationRequest.getArrivalDate(),
                updateReservationRequest.getDepartureDate());
        repository.updateReservationIfAvailable(reservation);
        return reservation.getReservationId();
    }

    public void cancelReservation(final String email, final String reservationId) {
        repository.deleteReservation(email, reservationId);
    }

}
