package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Tree;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TreeRepository extends CrudRepository<Tree, Long> {
  Iterable<Tree> findAllByName(String name);
}