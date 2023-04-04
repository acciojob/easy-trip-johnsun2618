package com.driver.controllers;


import com.driver.model.Airport;
import com.driver.model.City;
import com.driver.model.Flight;
import com.driver.model.Passenger;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@RestController
public class AirportController {



    HashMap<Integer, Airport> airportDb = new HashMap<>();
    List<Flight> flights = new ArrayList<>();
    HashMap<Integer, Integer> bookedSeats = new HashMap<>();

    HashMap<Integer, List<Integer>> bookings = new HashMap<>();
    List<Passenger> passengers = new ArrayList<>();



    @PostMapping("/add_airport")
    public String addAirport(@RequestBody Airport airport){

        //Simply add airport details to your database
        //Return a String message "SUCCESS"

        int key = airport.getNoOfTerminals();
        airportDb.put(key, airport);
        return "SUCCESS";

    }

    @GetMapping("/get-largest-aiport")
    public String getLargestAirportName(){

        //Largest airport is in terms of terminals. 3 terminal airport is larger than 2 terminal airport
        //Incase of a tie return the Lexicographically smallest airportName

        String largestAirportName = "";
        int largestTerminalsCount = 0;
        for (Airport airport : airportDb.values()) {
            if (airport.getNoOfTerminals() > largestTerminalsCount) {
                largestTerminalsCount = airport.getNoOfTerminals();
                largestAirportName = airport.getAirportName();
            } else if (airport.getNoOfTerminals() == largestTerminalsCount
                    && airport.getAirportName().
                    compareTo(largestAirportName) < 0) {
                largestAirportName = airport.getAirportName();
            }
        }
        return largestAirportName;

    }

    @GetMapping("/get-shortest-time-travel-between-cities")
    public double getShortestDurationOfPossibleBetweenTwoCities(@RequestParam("fromCity") City fromCity, @RequestParam("toCity")City toCity){

        //Find the duration by finding the shortest flight that connects these 2 cities directly
        //If there is no direct flight between 2 cities return -1.

        double shortestDuration = -1;

        for (Flight flight : flights) {
            if (flight.getFromCity() == fromCity && flight.getToCity() == toCity) {
                if (shortestDuration == -1 || flight.getDuration()
                        < shortestDuration) {
                    shortestDuration = flight.getDuration();
                }
            }
        }

       return shortestDuration;

    }

    @GetMapping("/get-number-of-people-on-airport-on/{date}")
    public int getNumberOfPeopleOn(@PathVariable("date") Date date,@RequestParam("airportName")String airportName){

        //Calculate the total number of people who have flights on that day on a particular airport
        //This includes both the people who have come for a flight and who have landed on an airport after their flight

        if (date == null || airportName == null || airportName.isEmpty()) {
            throw new IllegalArgumentException("Invalid date or airport name.");
        }

        int totalPeople = 0;

        for (Flight flight : flights) {
            if (flight.getFlightDate().equals(date)) {
                totalPeople += flight.getMaxCapacity();
            }
        }

        return totalPeople;

    }

    @GetMapping("/calculate-fare")
    public int calculateFlightFare(@RequestParam("flightId")Integer flightId){

        //Calculation of flight prices is a function of number of people who have booked the flight already.
        //Price for any flight will be : 3000 + noOfPeopleWhoHaveAlreadyBooked*50
        //Suppose if 2 people have booked the flight already : the price of flight for the third person will be 3000 + 2*50 = 3100
        //This will not include the current person who is trying to book, he might also be just checking price

        final int MAX_CAPACITY = 100;
        final int BASE_PRICE = 3000;
        final int PRICE_PER_BOOKING = 50;

        int noOfPeopleWhoHaveAlreadyBooked = bookedSeats.
                getOrDefault(flightId, 0);
        int fare = BASE_PRICE + noOfPeopleWhoHaveAlreadyBooked *
                PRICE_PER_BOOKING;
        return fare;

    }


    @PostMapping("/book-a-ticket")
    public String bookATicket(@RequestParam("flightId")Integer flightId,@RequestParam("passengerId")Integer passengerId){

        //If the numberOfPassengers who have booked the flight is greater than : maxCapacity, in that case :
        //return a String "FAILURE"
        //Also if the passenger has already booked a flight then also return "FAILURE".
        //else if you are able to book a ticket then return "SUCCESS"

        int MAX_CAPACITY = 100;

        if (!flights.contains(flightId)) {
            return "FAILURE"; // invalid flightId
        }
        if (bookings.getOrDefault(flightId,
                new ArrayList<>()).contains(passengerId)) {
            return "FAILURE"; // passenger has already booked this flight
        }
        List<Integer> passengers = bookings.
                getOrDefault(flightId, new ArrayList<>());
        if (passengers.size() >= MAX_CAPACITY) {
            return "FAILURE"; // flight is fully booked
        }
        passengers.add(passengerId);
        bookings.put(flightId, passengers);
        return "SUCCESS";
    }

    @PutMapping("/cancel-a-ticket")
    public String cancelATicket(@RequestParam("flightId")Integer flightId,@RequestParam("passengerId")Integer passengerId){

        //If the passenger has not booked a ticket for that flight or the flightId is invalid or in any other failure case
        // then return a "FAILURE" message
        // Otherwise return a "SUCCESS" message

        if (!flights.contains(flightId)) {
            return "FAILURE"; // invalid flightId
        }
        List<Integer> passengers = bookings.
                getOrDefault(flightId, new ArrayList<>());
        if (!passengers.contains(passengerId)) {
            return "FAILURE"; // passenger has not booked this flight
        }
        passengers.remove(passengerId);
        bookings.put(flightId, passengers);
        return "SUCCESS";
    }


    @GetMapping("/get-count-of-bookings-done-by-a-passenger/{passengerId}")
    public int countOfBookingsDoneByPassengerAllCombined(@PathVariable("passengerId")Integer passengerId){

        //Tell the count of flight bookings done by a passenger: This will tell the total count of flight bookings done by a passenger :
        return (int) bookings.values().stream()
                .flatMap(List::stream)
                .filter(id -> id.equals(passengerId))
                .count();
    }

    @PostMapping("/add-flight")
    public String addFlight(@RequestBody Flight flight){

        //Return a "SUCCESS" message string after adding a flight.
        flights.add(flight);
        return "SUCCESS";
    }


    @GetMapping("/get-aiportName-from-flight-takeoff/{flightId}")
    public String getAirportNameFromFlightId(@PathVariable("flightId")Integer flightId){

        //We need to get the starting airportName from where the flight will be taking off (Hint think of City variable if that can be of some use)
        //return null incase the flightId is invalid or you are not able to find the airportName

        if (!flights.contains(flightId)) {
            return null; // invalid flightId
        }
        return flights.toString();

    }


    @GetMapping("/calculate-revenue-collected/{flightId}")
    public int calculateRevenueOfAFlight(@PathVariable("flightId")Integer flightId){

        //Calculate the total revenue that a flight could have
        //That is of all the passengers that have booked a flight till now and then calculate the revenue
        //Revenue will also decrease if some passenger cancels the flight


        if (!flights.contains(flightId)) {
            return -1; // invalid flightId
        }
        List<Integer> passengers = bookings.getOrDefault(flightId, new ArrayList<>());
        int noOfPassengers = passengers.size();
        int farePerPassenger = calculateFlightFare(flightId);
        return noOfPassengers * farePerPassenger;

    }


    @PostMapping("/add-passenger")
    public String addPassenger(@RequestBody Passenger passenger){

        //Add a passenger to the database
        //And return a "SUCCESS" message if the passenger has been added successfully.

        int newPassengerId = passengers.size() + 1;
        passenger.setPassengerId(newPassengerId);
        //Add the passenger to the list of passengers
        passengers.add(passenger);
        return "SUCCESS";

    }


}
