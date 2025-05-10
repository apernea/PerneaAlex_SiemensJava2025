package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class InternshipApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private ItemService itemService;

	@Autowired
	private ItemRepository itemRepository;

	@Test
	void testCreateItem() throws Exception {
		Item item = new Item("test item", "description", "NEW", "apernea31@gmail.com");

		mockMvc.perform(post("/api/items")
						.contentType(MediaType.APPLICATION_JSON)
						.content(mapper.writeValueAsString(item)))
				.andExpect(status().isCreated());
	}

	@Test
	void testGetAllItems() throws Exception {
		mockMvc.perform(get("/api/items"))
				.andExpect(status().isOk());
	}

	@Test
	void testGetItemById() throws Exception {
		Item item = new Item("test item", "description",
				"UNPROCESSED","apernea31@gmail.com");
		item = itemRepository.save(item);

		mockMvc.perform(get("/api/items/" + item.getId()))
				.andExpect(status().isOk());
	}

	@Test
	void testUpdateItem() throws Exception {
		Item existingItem = new Item("existing item", "description",
				"NEW", "apernea31@gmail.com");
		existingItem = itemRepository.save(existingItem);

		Item updatedItem = new Item("updated item", "new description",
				"PROCESSED", "apernea31@gmail.com");

		mockMvc.perform(put("/api/items/" + existingItem.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(mapper.writeValueAsString(updatedItem)))
				.andExpect(status().isOk());
	}

	@Test
	void testDeleteItem() throws Exception {
		Item item = new Item("test item", "description",
				"UNPROCESSED","apernea31@gmail.com");
		item = itemRepository.save(item);

		mockMvc.perform(delete("/api/items/" + item.getId()))
				.andExpect(status().isNoContent());
	}

	@Test
	void testProcessedItems() throws Exception {
		Item item1 = new Item("item1","test item 1",
				"UNPROCESSED","apernea31@gmail.com");
		Item item2 = new Item("item2","test item 2",
				"UNPROCESSED","apernea31@gmail.com");

		itemRepository.save(item1);
		itemRepository.save(item2);

		CompletableFuture<List<Item>> future = itemService.processItemsAsync();
		List<Item> processedItems = future.get();
		assertEquals(2, processedItems.size());
		assertTrue(processedItems.stream().allMatch(item ->"PROCESSED".equals(item.getStatus())));

		List<Item> recordedItems = itemRepository.findAll();
		assertTrue(recordedItems.stream().allMatch(item -> "PROCESSED".equals(item.getStatus())));

	}

	@AfterEach
	void clearDatabase() {
		itemRepository.deleteAll();
	}
}
