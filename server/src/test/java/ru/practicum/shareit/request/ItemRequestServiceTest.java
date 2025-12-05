package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.request.service.ItemRequestService;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ItemRequestServiceTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Test
    void itemRequestServiceIsImplemented() {
        assertNotNull(itemRequestService);
    }
}