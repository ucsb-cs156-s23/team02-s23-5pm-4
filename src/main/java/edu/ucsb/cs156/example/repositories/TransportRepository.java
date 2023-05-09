package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Transport;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TransportRepository extends CrudRepository<Transport, String> {
  
}