package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Attractions;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.AttractionsRepository;
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

@Api(description = "Attractions")
@RequestMapping("/api/attractions")
@RestController
@Slf4j
public class AttractionsController extends ApiController {

    @Autowired
    AttractionsRepository attractionsRepository;

    @ApiOperation(value = "List all attractions")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Attractions> allAttractions() {
        Iterable<Attractions> dates = attractionsRepository.findAll();
        return dates;
    }

    @ApiOperation(value = "Get a single attraction")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Attractions getById(
            @ApiParam("id") @RequestParam Long id) {
        Attractions attractions = attractionsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Attractions.class, id));

        return attractions;
    }

    @ApiOperation(value = "Create a new attraction")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Attractions postAttractions(
            @ApiParam("name") @RequestParam String name,
            @ApiParam("description") @RequestParam String description,
            @ApiParam("address") @RequestParam String address)
            throws JsonProcessingException {

        // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        // See: https://www.baeldung.com/spring-date-parameters

        //log.info("localDateTime={}", localDateTime);

        Attractions attractions = new Attractions();
        attractions.setAddress(address);
        attractions.setName(name);
        attractions.setDescription(description);

        Attractions savedAttractions = attractionsRepository.save(attractions);

        return savedAttractions;
    }

    @ApiOperation(value = "Delete an Attraction")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteAttractions(
            @ApiParam("id") @RequestParam Long id) {
        Attractions attractions = attractionsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Attractions.class, id));

        attractionsRepository.delete(attractions);
        return genericMessage("Attractions with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single attraction")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Attractions updateAttractions(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Attractions incoming) {

        Attractions attractions = attractionsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Attractions.class, id));

        attractions.setAddress(incoming.getAddress());
        attractions.setName(incoming.getName());
        attractions.setDescription(incoming.getDescription());

        attractionsRepository.save(attractions);

        return attractions;
    }
}
