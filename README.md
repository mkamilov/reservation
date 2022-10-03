# README
This service allows to reserve the campsite located in an island. 
It is a single campsite, so it can be reserved only once for a given date(s) 

- The campsite will be free for all.
- The campsite can be reserved for max 3 days.
- The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.
- Reservations can be cancelled anytime.
- For sake of simplicity assume the check-in & check-out time is 12:00 AM

### Build and run the service
To build and start the service execute following cmd:
```
mvn test spring-boot:run 
```


### APIs
Check ReservationController.java file for available API endpoints

Once the service is running use following CURL commands to interact with it

Retrieve available dates for 1 month
```
curl localhost:8080/v1/reservation/availability
```
Retrieve available dates for specific dates
```
curl 'localhost:8080/v1/reservation/availability?arrivalDate=2022-03-12&departureDate=2022-03-13'
```

Create reservation
```
curl -H "Content-Type: application/json" -d '{"fullName":"John Smith","email":"johnsmith@mail.com","arrivalDate":"2022-03-12","departureDate":"2022-03-13"}' -X POST http://localhost:8080/v1/reservation
```

Retrieve created reservation
```
curl 'localhost:8080/v1/reservation/f7d0288f-e1cd-44ba-b964-d90f678afbd6?email=johnsmith@mail.com'
```

Update reservation
```
curl -H "Content-Type: application/json" -d '{"email":"johnsmith@mail.com","arrivalDate":"2022-03-12","departureDate":"2022-03-14"}' -X PATCH http://localhost:8080/v1/reservation/f7d0288f-e1cd-44ba-b964-d90f678afbd6
```

Delete reservation
```
curl -X DELETE 'localhost:8080/v1/reservation/f7d0288f-e1cd-44ba-b964-d90f678afbd6?email=johnsmith@mail.com'
```