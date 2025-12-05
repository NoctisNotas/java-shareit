package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.service.ItemService;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @Test
    void itemServiceIsImplemented() {
        assertNotNull(itemService);
    }
}