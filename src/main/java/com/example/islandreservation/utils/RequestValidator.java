package com.example.islandreservation.utils;

import com.example.islandreservation.exception.InvalidRequestException;
import com.example.islandreservation.model.CreateReservationRequest;
import com.example.islandreservation.model.UpdateReservationRequest;

import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

public class RequestValidator {
    public static void validateAvailabilityDates(LocalDate arrivalDate, LocalDate departureDate) {
        if (departureDate.compareTo(arrivalDate) < 0) {
            throw new InvalidRequestException("End date cannot be earlier than start date");
        }
    }

    public static void validateCreateReservationRequest(final CreateReservationRequest createReservationRequest) {
        if (createReservationRequest.getDepartureDate().compareTo(createReservationRequest.getArrivalDate()) < 0) {
            throw new InvalidRequestException("End date cannot be earlier than start date");
        }
        if (DAYS.between(createReservationRequest.getArrivalDate(), createReservationRequest.getDepartureDate()) > 3) {
            throw new InvalidRequestException("Maximum 3 days reservation allowed");
        }
        // TODO Verify email
    }

    public static void validateUpdateReservationRequest(final UpdateReservationRequest updateReservationRequest) {
        if (updateReservationRequest.getDepartureDate().compareTo(updateReservationRequest.getArrivalDate()) < 0) {
            throw new InvalidRequestException("End date cannot be earlier than start date");
        }
        if (DAYS.between(updateReservationRequest.getDepartureDate(), updateReservationRequest.getArrivalDate()) > 3) {
            throw new InvalidRequestException("Maximum 3 days reservation allowed");
        }
        // TODO Verify email
    }
}
