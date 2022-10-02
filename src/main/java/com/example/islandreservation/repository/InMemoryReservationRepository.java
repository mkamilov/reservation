package com.example.islandreservation.repository;

import com.example.islandreservation.exception.AlreadyReservedDateException;
import com.example.islandreservation.exception.ResourceNotFoundException;
import com.example.islandreservation.repository.model.ReservationEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryReservationRepository implements ReservationRepository {
    // TODO Should we use AtomicReference instead of syncronized?
    private Map<LocalDate, ReservationEntity> reservedDays = new ConcurrentHashMap<>();
    private Map<String, ReservationEntity> reservations = new ConcurrentHashMap<>();

    @Override
    public List<LocalDate> getAvailability(final LocalDate arrivalDate, final LocalDate departureDate) {
        final List<LocalDate> availableDates = new ArrayList<>();
        for (LocalDate date = arrivalDate; date.compareTo(departureDate) < 0; date = date.plusDays(1)) {
            if (!reservedDays.containsKey(date)) {
                availableDates.add(date);
            }
        }
        return availableDates;
    }

    @Override
    public synchronized void createReservationIfAvailable(final ReservationEntity reservation) {
        // TODO check if a customer is not reserving too many days with multiple reservations
        List<LocalDate> justReservedDays = new ArrayList<>();
        for (LocalDate date = reservation.getArrivalDate(); date.compareTo(reservation.getDepartureDate()) < 0; date = date.plusDays(1)) {
            if (reservedDays.containsKey(date)) {
                for (int i = 0; i < justReservedDays.size(); i++) {
                    reservedDays.remove(justReservedDays.get(i));
                }
                throw new AlreadyReservedDateException(String.format("Unfortunately %s date has already been reserved", date));
            }
            reservedDays.put(date, reservation);
            justReservedDays.add(date);
        }
        reservations.put(reservation.getReservationId(), reservation);
    }

    @Override
    public ReservationEntity getReservation(final String email, final String reservationId) {
        if (!reservations.containsKey(reservationId) || !reservations.get(reservationId).getEmail().equals(email)) {
            throw new ResourceNotFoundException(String.format("%s does not have %s reservation", email, reservationId));
        }
        return reservations.get(reservationId);
    }

    @Override
    public synchronized void updateReservationIfAvailable(final ReservationEntity newReservation) {
        // TODO check if a customer is not reserving too many days with multiple reservations
        if (!reservations.containsKey(newReservation.getReservationId()) || reservations.get(newReservation.getReservationId()).getEmail()
                .equals(newReservation.getEmail()) == false) {
            throw new ResourceNotFoundException(String.format("%s does not have %s reservation", newReservation.getEmail(),
                    newReservation.getReservationId()));
        }
        ReservationEntity oldReservation = reservations.get(newReservation.getReservationId());

        // create/update new reservation
        Set<LocalDate> visitedReservationDates = new HashSet<>();
        List<LocalDate> justReservedDays = new ArrayList<>();
        for (LocalDate date = newReservation.getArrivalDate(); date.compareTo(newReservation.getDepartureDate()) < 0; date = date.plusDays(1)) {
            if (reservedDays.containsKey(date)) {
                ReservationEntity existingReservation = reservedDays.get(date);
                if (existingReservation.getReservationId().equals(newReservation.getReservationId())) {
                    // Existing reservation is the same reservation
                    visitedReservationDates.add(date);
                    continue;
                }
                for (int i = 0; i < justReservedDays.size(); i++) {
                    reservedDays.remove(justReservedDays.get(i));
                }
                throw new AlreadyReservedDateException(String.format("Unfortunately %s date has already been reserved", date));
            }
            reservedDays.put(date, newReservation);
            justReservedDays.add(date);
        }
        reservations.put(newReservation.getReservationId(), newReservation);

        // Cancel old reservation if applicable
        for (LocalDate date = oldReservation.getArrivalDate(); date.compareTo(oldReservation.getDepartureDate()) < 0; date = date.plusDays(1)) {
            if (visitedReservationDates.contains(date)) {
                continue;
            }
            reservedDays.remove(date);
        }
    }

    @Override
    public void deleteReservation(final String email, final String reservationId) {
        if (!reservations.containsKey(reservationId) || reservations.get(reservationId).getEmail().equals(email) == false) {
            throw new ResourceNotFoundException(String.format("%s does not have %s reservation", email, reservationId));
        }
        ReservationEntity reservation = reservations.get(reservationId);
        for (LocalDate date = reservation.getArrivalDate(); date.compareTo(reservation.getDepartureDate()) < 0; date = date.plusDays(1)) {
            reservedDays.remove(date);
        }
        reservations.remove(reservationId);
    }
}
