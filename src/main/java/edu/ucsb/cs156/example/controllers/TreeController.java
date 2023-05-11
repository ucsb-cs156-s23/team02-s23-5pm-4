package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Tree;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.TreeRepository;
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

@Api(description = "Trees")
@RequestMapping("/api/tree")
@RestController
@Slf4j
public class TreeController extends ApiController {

    @Autowired
    TreeRepository treeRepository;

    @ApiOperation(value = "List all trees")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Tree> allTrees() {
        Iterable<Tree> trees = treeRepository.findAll();
        return trees;
    }

    @ApiOperation(value = "Get a single tree")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Tree getById(
            @ApiParam("id") @RequestParam Long id) {
        Tree tree = treeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Tree.class, id));

        return tree;
    }

    @ApiOperation(value = "Create a new tree")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Tree postTree(
            @ApiParam("category") @RequestParam String category,
            @ApiParam("name") @RequestParam String name)
            throws JsonProcessingException {

        log.info("name={}", name);

        Tree tree = new Tree();
        tree.setCategory(category);
        tree.setName(name);

        Tree savedTree = treeRepository.save(tree);

        return savedTree;
    }

    @ApiOperation(value = "Delete a Tree")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteTree(
            @ApiParam("id") @RequestParam Long id) {
        Tree tree = treeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Tree.class, id));

        treeRepository.delete(tree);
        return genericMessage("Tree with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single tree")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Tree updateTree(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Tree incoming) {

        Tree tree = treeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Tree.class, id));

        tree.setCategory(incoming.getCategory());
        tree.setName(incoming.getName());

        treeRepository.save(tree);

        return tree;
    }
}
