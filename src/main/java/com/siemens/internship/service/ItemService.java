package com.siemens.internship.service;

import com.siemens.internship.repo.ItemRepository;
import com.siemens.internship.model.Item;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
public class ItemService {
    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {

        List<Long> itemIds = itemRepository.findAllIds();
        ConcurrentLinkedQueue<Item> processedItems = new ConcurrentLinkedQueue<>();
        List<CompletableFuture<Void>> futures = itemIds.stream()
                .map(id -> CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(100);

                        itemRepository.findById(id).ifPresent(item -> {
                            item.setStatus("PROCESSED");
                            itemRepository.save(item);
                            processedItems.add(item);
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Processing interrupted for item ID: " + id, e);
                    }
                }))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(v -> {
            List<Item> result = new ArrayList<>(processedItems);
            result.sort(Comparator.comparingLong(Item::getId));
            return result;
        });
    }

}

