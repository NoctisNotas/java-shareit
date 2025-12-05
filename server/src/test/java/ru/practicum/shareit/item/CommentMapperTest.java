package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;

import static org.junit.jupiter.api.Assertions.*;

class CommentMapperTest {

    @Test
    void toCommentShouldMapDtoToEntity() {
        CommentDto dto = new CommentDto("Great item!");

        Comment comment = CommentMapper.toComment(dto);

        assertNotNull(comment);
        assertEquals("Great item!", comment.getText());
        assertNull(comment.getId());
        assertNull(comment.getItem());
        assertNull(comment.getAuthor());
        assertNull(comment.getCreated());
    }
}