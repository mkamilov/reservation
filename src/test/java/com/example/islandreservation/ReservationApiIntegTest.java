package com.example.islandreservation;

import com.example.islandreservation.model.CreateReservationRequest;
import com.example.islandreservation.model.GetReservationResponse;
import com.example.islandreservation.model.UpdateReservationRequest;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReservationApiIntegTest {

    @Autowired
    private TestRestTemplate template;

    @Before
    public void setup() {
        template.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    @Test
    public void getAvailabilityReturns1MonthByDefault() {
        ResponseEntity<List> response = template.getForEntity("/v1/reservation/availability", List.class);
        List<String> monthlyDates = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (LocalDate date = now; date.compareTo(now.plusMonths(1)) < 0; date = date.plusDays(1)) {
            monthlyDates.add(date.toString());
        }
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<String> responseBody = response.getBody();
        assertThat(responseBody.size()).isEqualTo(monthlyDates.size());
        assertThat(response.getBody()).containsAll(monthlyDates);
    }

    @Test
    public void getAvailabilityReturnsAvailabilityForGivenDate() {
        getAndAssertAvailability(LocalDate.parse("2022-03-12"), LocalDate.parse("2022-03-13"));
    }

    @Test
    public void createReservationRequestMakesDateUnavailable() {
        final String arrivalDate = "2021-03-12";
        final String departureDate = "2021-03-14";
        createAndAssertReservation("johnsmith@mail.com", arrivalDate, departureDate);

        // Only 1 day is available
        ResponseEntity<List> getAvailabilityResponse = template.getForEntity(String.format(
                "/v1/reservation/availability?arrivalDate=%s&departureDate=%s", arrivalDate, departureDate), List.class);
        assertThat(getAvailabilityResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getAvailabilityResponse.getBody().size()).isEqualTo(0);
    }

    @Test
    public void createReservationRequestThrowsExceptionWhenDatesAreInvalid() {
        CreateReservationRequest createReservationRequest = new CreateReservationRequest("John Smith", "johnsmith@mail.com",
                LocalDate.parse("2021-01-12"), LocalDate.parse("2021-01-16"));
        ResponseEntity<String> createReservationResponse = template.postForEntity("/v1/reservation", createReservationRequest, String.class);

        assertThat(createReservationResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(createReservationResponse.getBody()).isNotEmpty();
    }

    @Test
    public void createReservationRequestThrowsExceptionWhenDatesAreUnavailable() {
        CreateReservationRequest createReservationRequest = new CreateReservationRequest("John Smith", "johnsmith@mail.com",
                LocalDate.parse("2021-02-12"), LocalDate.parse("2021-02-15"));
        ResponseEntity<String> createReservationResponse = template.postForEntity("/v1/reservation", createReservationRequest, String.class);

        assertThat(createReservationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createReservationResponse.getBody()).isNotEmpty();

        CreateReservationRequest createReservationRequest2 = new CreateReservationRequest("Michael Jonson", "michael@mail.com",
                LocalDate.parse("2021-02-12"), LocalDate.parse("2021-02-13"));
        ResponseEntity<String> createReservationResponse2 = template.postForEntity("/v1/reservation", createReservationRequest2, String.class);

        assertThat(createReservationResponse2.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(createReservationResponse2.getBody()).isNotEmpty();
    }

    @Test
    public void getNewlyCreatedReservation() {
        final String email = "johnsmith@mail.com";
        final String arrivalDate = "2021-04-12";
        final String departureDate = "2021-04-13";
        String reservationId = createAndAssertReservation(email, arrivalDate, departureDate);
        getAndAssertReservation(reservationId, email, arrivalDate, departureDate);
    }

    @Test
    public void updateCreatedReservation() {
        final String email = "johnsmith@mail.com";
        final String arrivalDate = "2021-05-12";
        final String departureDate = "2021-05-14";
        String reservationId = createAndAssertReservation(email, arrivalDate, departureDate);
        UpdateReservationRequest updateReservationRequest = new UpdateReservationRequest("John Smith", email,
                LocalDate.parse(arrivalDate), LocalDate.parse(departureDate));
        HttpEntity<UpdateReservationRequest> updateHttpEntity = new HttpEntity<>(updateReservationRequest);

        ResponseEntity<String> updateReservationResponse = template.exchange(String.format("/v1/reservation/%s",
                reservationId), HttpMethod.PATCH, updateHttpEntity, String.class);

        assertThat(updateReservationResponse.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        getAndAssertReservation(reservationId, email, arrivalDate, departureDate);
    }

    @Test
    public void deleteCreatedReservation() {
        final String email = "johnsmith@mail.com";
        final String arrivalDate = "2021-06-12";
        final String departureDate = "2021-06-14";
        String reservationId = createAndAssertReservation(email, arrivalDate, departureDate);

        HttpEntity<String> deleteHttpEntity = new HttpEntity<>(email);
        ResponseEntity<String> deleteReservationResponse = template.exchange(String.format("/v1/reservation/%s?email=%s", reservationId, email),
                HttpMethod.DELETE, deleteHttpEntity, String.class);

        assertThat(deleteReservationResponse.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        getAndAssertAvailability(LocalDate.parse(arrivalDate), LocalDate.parse(departureDate));
    }

    @Test
    public void createReservationWhenMultipleRequestsForTheSameDate() throws InterruptedException {
        int numberOfThreads = 100;
        AtomicInteger okResponseCount = new AtomicInteger();
        AtomicInteger conflictResponseCount = new AtomicInteger();
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            final String email = String.format("johnsmith%s@mail.com", i);
            service.execute(() -> {
                CreateReservationRequest createReservationRequest = new CreateReservationRequest("John Smith", email,
                        LocalDate.parse("2021-10-01"), LocalDate.parse("2021-10-03"));
                ResponseEntity<String> createReservationResponse = template.postForEntity("/v1/reservation", createReservationRequest, String.class);
                if (createReservationResponse.getStatusCode() == HttpStatus.OK) {
                    okResponseCount.getAndIncrement();
                } else if (createReservationResponse.getStatusCode() == HttpStatus.CONFLICT) {
                    conflictResponseCount.getAndIncrement();
                }
                latch.countDown();
            });
        }
        latch.await();
        assertThat(okResponseCount.get()).isEqualTo(1);
        assertThat(conflictResponseCount.get()).isEqualTo(numberOfThreads - 1);
    }

    private String createAndAssertReservation(final String email, final String arrivalDate, final String departureDate) {
        CreateReservationRequest createReservationRequest = new CreateReservationRequest("John Smith", email,
                LocalDate.parse(arrivalDate), LocalDate.parse(departureDate));
        ResponseEntity<String> createReservationResponse = template.postForEntity("/v1/reservation", createReservationRequest, String.class);
        assertThat(createReservationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createReservationResponse.getBody()).isNotEmpty();
        return createReservationResponse.getBody();
    }

    private void getAndAssertReservation(final String reservationId, final String email, final String arrivalDate,
                                         final String departureDate) {
        ResponseEntity<GetReservationResponse> getReservationResponseEntity = template.getForEntity(
                String.format("/v1/reservation/%s?email=%s", reservationId, email), GetReservationResponse.class);
        assertThat(getReservationResponseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        GetReservationResponse getReservationResponse = getReservationResponseEntity.getBody();
        assertThat(getReservationResponse.getReservationId()).isEqualTo(reservationId);
        assertThat(getReservationResponse.getArrivalDate()).isEqualTo(arrivalDate);
        assertThat(getReservationResponse.getDepartureDate()).isEqualTo(departureDate);
    }

    private void getAndAssertAvailability(final LocalDate arrivalDate, final LocalDate departureDate) {
        ResponseEntity<List> response = template.getForEntity(String.format(
                "/v1/reservation/availability?arrivalDate=%s&departureDate=%s", arrivalDate, departureDate), List.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<String> responseBody = response.getBody();
        long totalAvailabilityDays = DAYS.between(arrivalDate, departureDate);
        assertThat(responseBody.size()).isEqualTo(totalAvailabilityDays);
        int i = 0;
        for (LocalDate date = arrivalDate; date.compareTo(departureDate) < 0; date = date.plusDays(1)) {
            assertThat(response.getBody().get(i++)).isEqualTo(date.toString());
        }
    }
}