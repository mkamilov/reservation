package com.example.islandreservation.controller;

import com.example.islandreservation.exception.AlreadyReservedDateException;
import com.example.islandreservation.exception.InvalidRequestException;
import com.example.islandreservation.exception.ResourceNotFoundException;
import com.example.islandreservation.model.CreateReservationRequest;
import com.example.islandreservation.model.GetReservationResponse;
import com.example.islandreservation.model.UpdateReservationRequest;
import com.example.islandreservation.service.ReservationService;
import com.example.islandreservation.utils.RequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
public class ReservationController {

    // TODO Add logging

    @Autowired
    private ReservationService reservationService;

    /**
     * Retrieve available dates for reservation.
     * @param arrivalDate optional value to start checking availability. Use current date if not set
     * @param departureDate optional value to stop checking availability(excluded). Use 1 month if not set
     * @return ResponseEntity<List<LocalDate>> list of available dates
     * @throws InvalidRequestException if departureDate is earlier than arrivalDate
     */
    @GetMapping(value = "/v1/reservation/availability")
    public ResponseEntity<List<LocalDate>> getAvailability(@RequestParam(value = "arrivalDate", required = false)
                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrivalDate,
                                                           @RequestParam(value = "departureDate", required = false)
                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate) {
        if (arrivalDate == null) {
            arrivalDate = LocalDate.now();
        }
        if (departureDate == null) {
            departureDate = LocalDate.now().plusMonths(1);
        }
        RequestValidator.validateAvailabilityDates(arrivalDate, departureDate);
        return ResponseEntity.ok(reservationService.getAvailability(arrivalDate, departureDate));
    }

    /**
     * Create reservation.
     * @param createReservationRequest parameters to create a reservation that includes email, full name, arrival and departure dates
     * @return ResponseEntity<String> newly created reservation ID
     * @throws InvalidRequestException if provided input values are invalid
     * @throws AlreadyReservedDateException if requested dates are already reserved
     */
    @PostMapping(value = "/v1/reservation")
    public ResponseEntity<String> createReservation(@RequestBody @Validated final CreateReservationRequest createReservationRequest) {
        // TODO Use Aspect for validation
        RequestValidator.validateCreateReservationRequest(createReservationRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.createReservation(createReservationRequest));
    }

    /**
     * Retrieve specific reservation information.
     * @param email customer email whom the reservation belongs to
     * @param id reservation ID to retrieve information about
     * @return ResponseEntity<GetReservationResponse> reservation details for the given ID
     * @throws ResourceNotFoundException if provided email does not have provided id
     */
    @GetMapping(value = "/v1/reservation/{id}")
    public ResponseEntity<GetReservationResponse> getReservation(@RequestParam(value = "email") final String email,
                                                                 @PathVariable(value = "id") final String id) {
        return ResponseEntity.ok(reservationService.getReservation(email, id));
    }

    /**
     * Updates a reservation for given ID and params.
     * @params id, updateReservationRequest
     * @return ResponseEntity<String>
     * @throws ResourceNotFoundException if email does not have reservation ID
     */
    @PatchMapping(value = "/v1/reservation/{id}")
    public ResponseEntity<String> updateReservation(@PathVariable(value = "id") String reservationId,
                                                    @RequestBody @Validated UpdateReservationRequest updateReservationRequest) {
        RequestValidator.validateUpdateReservationRequest(updateReservationRequest);
        return ResponseEntity.ok(reservationService.updateReservation(reservationId, updateReservationRequest));
    }

    /**
     * Cancels a reservation for given ID and email.
     * @params email, id
     * @return ResponseEntity<Void>
     * @throws ResourceNotFoundException if email does not have reservation ID
     */
    @DeleteMapping(value = "/v1/reservation/{id}")
    public ResponseEntity<Void> cancelReservation(@RequestParam(value = "email") String email,
                                               @PathVariable(value = "id") String reservationId) {
        reservationService.cancelReservation(email, reservationId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
