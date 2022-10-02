package com.example.islandreservation.repository;

import com.example.islandreservation.exception.AlreadyReservedDateException;
import com.example.islandreservation.exception.ResourceNotFoundException;
import com.example.islandreservation.repository.model.ReservationEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class InMemoryReservationRepositoryTests {

    InMemoryReservationRepository systemUnderTest = new InMemoryReservationRepository();

    @Test
    public void getThrowsExceptionWhenReservationDoesNotExist() {
        assertThatThrownBy(() -> {
            systemUnderTest.getReservation("testEmail", "testId");
        }).isInstanceOf(ResourceNotFoundException.class).hasMessage("testEmail does not have testId reservation");
    }

    @Test
    public void updatesCreatedReservationWithLessDays() {
        final String reservationId = "testId";
        ReservationEntity reservation = new ReservationEntity(reservationId, "testName", "testEmail",
                LocalDate.parse("2022-09-10"), LocalDate.parse("2022-09-15"));
        systemUnderTest.createReservationIfAvailable(reservation);
        assertThat(systemUnderTest.getAvailability(LocalDate.parse("2022-09-10"), LocalDate.parse("2022-09-15"))).isEmpty();

        ReservationEntity updatedReservation = new ReservationEntity(reservationId, "testName", "testEmail",
                LocalDate.parse("2022-09-10"), LocalDate.parse("2022-09-12"));
        systemUnderTest.updateReservationIfAvailable(updatedReservation);
        assertThat(systemUnderTest.getAvailability(LocalDate.parse("2022-09-10"), LocalDate.parse("2022-09-15")).size()).isEqualTo(3);
    }

    @Test
    public void updatesCreatedReservationWithMoreDays() {
        final String reservationId = "testId";
        ReservationEntity reservation = new ReservationEntity(reservationId, "testName", "testEmail",
                LocalDate.parse("2022-09-10"), LocalDate.parse("2022-09-12"));
        systemUnderTest.createReservationIfAvailable(reservation);
        assertThat(systemUnderTest.getAvailability(LocalDate.parse("2022-09-10"), LocalDate.parse("2022-09-15")).size()).isEqualTo(3);

        ReservationEntity updatedReservation = new ReservationEntity(reservationId, "testName", "testEmail",
                LocalDate.parse("2022-09-10"), LocalDate.parse("2022-09-15"));
        systemUnderTest.updateReservationIfAvailable(updatedReservation);
        assertThat(systemUnderTest.getAvailability(LocalDate.parse("2022-09-10"), LocalDate.parse("2022-09-15"))).isEmpty();
    }

    @Test
    public void updateThrowsExceptionWhenEmailDoesNotHaveRervation() {
        final String reservationId2 = "testId2";
        ReservationEntity updatedReservation = new ReservationEntity(reservationId2, "testName2", "testEmail2",
                LocalDate.parse("2022-09-09"), LocalDate.parse("2022-09-11"));
        assertThatThrownBy(() -> {
            systemUnderTest.updateReservationIfAvailable(updatedReservation);
        }).isInstanceOf(ResourceNotFoundException.class).hasMessage("testEmail2 does not have testId2 reservation");
    }

    @Test
    public void updateThrowsExceptionWhenDatesNotAvailable() {
        final String reservationId1 = "testId1";
        ReservationEntity reservation = new ReservationEntity(reservationId1, "testName", "testEmail",
                LocalDate.parse("2022-09-10"), LocalDate.parse("2022-09-15"));
        systemUnderTest.createReservationIfAvailable(reservation);
        assertThat(systemUnderTest.getAvailability(LocalDate.parse("2022-09-10"), LocalDate.parse("2022-09-15"))).isEmpty();

        final String reservationId2 = "testId2";
        ReservationEntity reservation2 = new ReservationEntity(reservationId2, "testName2", "testEmail2",
                LocalDate.parse("2022-09-09"), LocalDate.parse("2022-09-10"));
        systemUnderTest.createReservationIfAvailable(reservation2);
        assertThat(systemUnderTest.getAvailability(LocalDate.parse("2022-09-09"), LocalDate.parse("2022-09-10"))).isEmpty();

        ReservationEntity updatedReservation = new ReservationEntity(reservationId2, "testName2", "testEmail2",
                LocalDate.parse("2022-09-09"), LocalDate.parse("2022-09-11"));
        assertThatThrownBy(() -> {
            systemUnderTest.updateReservationIfAvailable(updatedReservation);
        }).isInstanceOf(AlreadyReservedDateException.class).hasMessageEndingWith("date has already been reserved");
    }
}
