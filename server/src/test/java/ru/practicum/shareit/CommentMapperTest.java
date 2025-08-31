package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.CommentMapperImpl;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {CommentMapperImpl.class})
class CommentMapperTest {

    @Autowired
    private CommentMapper commentMapper;

    @Test
    void toCommentDto() {
        User author = new User();
        author.setName("Test Author");

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("This is a test comment.");
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        CommentDto commentDto = commentMapper.toCommentDto(comment);

        assertNotNull(commentDto);
        assertEquals(comment.getId(), commentDto.getId());
        assertEquals(comment.getText(), commentDto.getText());
        assertEquals(comment.getAuthor().getName(), commentDto.getAuthorName());
        assertEquals(comment.getCreated(), commentDto.getCreated());
    }

    @Test
    void toCommentDto_whenCommentIsNull_shouldReturnNull() {
        assertNull(commentMapper.toCommentDto(null));
    }

    @Test
    void toComment() {
        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setText("New comment text");

        User author = new User();
        author.setId(1L);

        Item item = new Item();
        item.setId(1L);

        Comment comment = commentMapper.toComment(requestDto, author, item);

        assertNotNull(comment);
        assertNull(comment.getId());
        assertEquals(requestDto.getText(), comment.getText());
        assertEquals(author, comment.getAuthor());
        assertEquals(item, comment.getItem());
        assertNull(comment.getCreated());
    }
}
