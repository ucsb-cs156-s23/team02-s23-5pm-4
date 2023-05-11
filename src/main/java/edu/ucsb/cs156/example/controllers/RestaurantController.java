package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Restaurant;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RestaurantRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.time.LocalDateTime;

@Api(description = "Restaurant")
@RequestMapping("/api/restaurant")
@RestController
@Slf4j
public class RestaurantController extends ApiController {

    @Autowired
    RestaurantRepository restaurantsRepository;

    @ApiOperation(value = "List all restaurants")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Restaurant> allRestaurants() {
        Iterable<Restaurant> restaurants = restaurantsRepository.findAll();
        return restaurants;
    }

    @ApiOperation(value = "Get a single restaurant")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Restaurant getById(
            @ApiParam("id") @RequestParam Long id) {
        Restaurant restaurant = restaurantsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Restaurant.class, id));

        return restaurant;
    }

    @ApiOperation(value = "Create a new restaurant")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Restaurant postRestaurant(
            @ApiParam("name") @RequestParam String name,
            @ApiParam("description") @RequestParam String description,
            @ApiParam("details") @RequestParam String details)
            throws JsonProcessingException {

        // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        // See: https://www.baeldung.com/spring-date-parameters

        //log.info("localDateTime={}", localDateTime);

        Restaurant restaurants = new Restaurant();
        restaurants.setDetails(details);
        restaurants.setName(name);
        restaurants.setDescription(description);

        Restaurant savedRestaurant = restaurantsRepository.save(restaurants);

        return savedRestaurant;
    }

    @ApiOperation(value = "Delete a restaurant")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteRestaurant(
            @ApiParam("id") @RequestParam Long id) {
        Restaurant restaurant = restaurantsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Restaurant.class, id));

        restaurantsRepository.delete(restaurant);
        return genericMessage("Restaurant with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single restaurant")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Restaurant updateRestaurant(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Restaurant incoming) {

        Restaurant restaurant = restaurantsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Restaurant.class, id));

        restaurant.setDetails(incoming.getDetails());
        restaurant.setName(incoming.getName());
        restaurant.setDescription(incoming.getDescription());

        restaurantsRepository.save(restaurant);

        return restaurant;
    }
}
