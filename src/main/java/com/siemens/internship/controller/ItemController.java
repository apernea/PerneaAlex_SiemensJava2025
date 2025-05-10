package com.siemens.internship.controller;

import com.siemens.internship.model.Item;
import com.siemens.internship.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Item> createItem(@Valid @RequestBody Item item) {
        Item savedItem = itemService.save(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @Valid @RequestBody Item item) {
        Optional<Item> existing = itemService.findById(id);

        if (existing.isPresent()) {
            Item toUpdate = existing.get();
            toUpdate.setName(item.getName());
            toUpdate.setDescription(item.getDescription());
            toUpdate.setStatus(item.getStatus());
            toUpdate.setEmail(item.getEmail());

            return ResponseEntity.ok(itemService.save(toUpdate));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
            if (itemService.findById(id).isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            itemService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

    @GetMapping("/process")
    public ResponseEntity<List<Item>> processItems() {
        try{
            List<Item> processed = itemService.processItemsAsync().get();
            return new ResponseEntity<>(processed, HttpStatus.OK);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ExecutionException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
