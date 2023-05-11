package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Transport;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.TransportRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
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


@Api(description = "Transport")
@RequestMapping("/api/transport")
@RestController
@Slf4j
public class TransportController extends ApiController {

    @Autowired
    TransportRepository transportRepository;

    @ApiOperation(value = "List all transport")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Transport> allTransports() {
        Iterable<Transport> transport = transportRepository.findAll();
        return transport;
    }

    @ApiOperation(value = "Get a single transport")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Transport getById(
            @ApiParam("id") @RequestParam Long id) {
        Transport transport = transportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Transport.class, id));

        return transport;
    }

    @ApiOperation(value = "Create a new transport")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Transport postTransport(
        @ApiParam("name") @RequestParam String name,
        @ApiParam("mode") @RequestParam String mode,
        @ApiParam("cost") @RequestParam String cost
        )
        {

        Transport transport = new Transport();
        transport.setName(name);
        transport.setMode(mode);
        transport.setCost(cost);

        Transport savedTransport = transportRepository.save(transport);

        return savedTransport;
    }

    @ApiOperation(value = "Delete a Transport")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteTransport(
            @ApiParam("id") @RequestParam Long id) {
        Transport transport = transportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Transport.class, id));

        transportRepository.delete(transport);
        return genericMessage("Transport with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single transport")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Transport updateTransport(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Transport incoming) {

        Transport transport = transportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Transport.class, id));


        transport.setName(incoming.getName());  
        transport.setMode(incoming.getMode());
        transport.setCost(incoming.getCost());

        transportRepository.save(transport);

        return transport;
    }
}
