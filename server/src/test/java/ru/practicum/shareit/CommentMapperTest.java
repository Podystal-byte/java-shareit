package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class CommentMapperTest {

    private CommentMapper commentMapper;

    @BeforeEach
    public void setUp() {
        commentMapper = Mappers.getMapper(CommentMapper.class);
    }

    @Test
    public void toCommentDto_Success() {
        User author = new User();
        author.setId(1L);
        author.setName("John Doe");

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("This is a test comment.");
        comment.setCreated(LocalDateTime.of(2023, 1, 1, 10, 0));
        comment.setAuthor(author);

        CommentDto commentDto = commentMapper.toCommentDto(comment);

        assertNotNull(commentDto);
        assertEquals(comment.getId(), commentDto.getId());
        assertEquals(comment.getText(), commentDto.getText());
        assertEquals(comment.getCreated(), commentDto.getCreated());
        assertEquals(author.getName(), commentDto.getAuthorName());
    }

    @Test
    public void toCommentDto_WhenNullComment_ReturnsNull() {
        CommentDto commentDto = commentMapper.toCommentDto(null);

        assertNull(commentDto);
    }

    @Test
    public void toCommentDto_WhenNullAuthor_ReturnsNullAuthorName() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("This is a test comment.");
        comment.setCreated(LocalDateTime.of(2023, 1, 1, 10, 0));
        comment.setAuthor(null);

        CommentDto commentDto = commentMapper.toCommentDto(comment);

        assertNotNull(commentDto);
        assertNull(commentDto.getAuthorName());
    }

    @Test
    public void toComment_Success() {
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setText("This is a new comment.");

        User author = new User();
        author.setId(1L);

        Item item = new Item();
        item.setId(1L);

        Comment comment = commentMapper.toComment(commentRequestDto, author, item);

        assertNotNull(comment);
        assertEquals(commentRequestDto.getText(), comment.getText());
        assertEquals(author, comment.getAuthor());
        assertEquals(item, comment.getItem());
    }

    @Test
    public void toComment_WhenNullInputs_ReturnsNull() {
        Comment comment = commentMapper.toComment(null, null, null);


        assertNull(comment);
    }
}
