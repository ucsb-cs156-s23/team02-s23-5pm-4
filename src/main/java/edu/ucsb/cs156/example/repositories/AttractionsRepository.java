package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Attractions;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AttractionsRepository extends CrudRepository<Attractions, Long> {
}