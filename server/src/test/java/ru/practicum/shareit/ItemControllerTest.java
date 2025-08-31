package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.CommentService;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @MockBean
    private ItemService itemService;
    @MockBean
    private ItemMapper itemMapper;
    @MockBean
    private CommentMapper commentMapper;
    @MockBean
    private CommentService commentService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private ItemDto itemDto;
    private Item item;
    private User user;
    private ItemWithBookingsAndCommentsDto itemWithBookingsDto;
    private CommentRequestDto commentRequestDto;
    private CommentDto commentDto;
    private Comment comment;


    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Owner");
        user.setEmail("owner@example.com");

        item = new Item();
        item.setId(1L);
        item.setName("Drill");
        item.setDescription("A powerful drill");
        item.setAvailable(true);
        item.setOwner(user);

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Drill");
        itemDto.setDescription("A powerful drill");
        itemDto.setAvailable(true);

        itemWithBookingsDto = new ItemWithBookingsAndCommentsDto();
        itemWithBookingsDto.setId(1L);
        itemWithBookingsDto.setName("Drill");

        commentRequestDto = new CommentRequestDto();
        commentRequestDto.setText("Great drill!");

        comment = new Comment();
        comment.setId(1L);
        comment.setText("Great drill!");
        comment.setAuthor(user);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());

        commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Great drill!");
        commentDto.setAuthorName(user.getName());
        commentDto.setCreated(comment.getCreated());
    }

    @Test
    void addItem_shouldReturnItemDto() throws Exception {
        when(itemService.addItem(anyLong(), any(ItemDto.class))).thenReturn(item);
        when(itemMapper.toDto(any(Item.class))).thenReturn(itemDto);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Drill")));
    }

    @Test
    void updateItem_shouldReturnUpdatedItem() throws Exception {
        when(itemService.updateItem(anyLong(), anyLong(), any(Item.class))).thenReturn(item);
        when(itemMapper.toDto(any(Item.class))).thenReturn(itemDto);

        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId().intValue())));
    }

    @Test
    void getItemById_shouldReturnItemWithBookings() throws Exception {
        when(itemService.getItemById(1L, 1L)).thenReturn(itemWithBookingsDto);

        mvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemWithBookingsDto.getId().intValue())));
    }

    @Test
    void getOwnersItems_shouldReturnListOfItems() throws Exception {
        when(itemService.getItemsByOwner(1L)).thenReturn(List.of(item));
        when(itemMapper.toDto(any(Item.class))).thenReturn(itemDto);

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDto.getId().intValue())));
    }

    @Test
    void searchItems_shouldReturnListOfFoundItems() throws Exception {
        when(itemService.searchItems("drill")).thenReturn(List.of(item));
        when(itemMapper.toDto(any(Item.class))).thenReturn(itemDto);

        mvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Drill")));
    }

    @Test
    void searchItems_whenTextIsBlank_shouldReturnEmptyList() throws Exception {
        when(itemService.searchItems("")).thenReturn(Collections.emptyList());

        mvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void addComment_shouldReturnCommentDto() throws Exception {
        when(commentService.addComment(anyLong(), anyLong(), any(CommentRequestDto.class))).thenReturn(comment);
        when(commentMapper.toCommentDto(any(Comment.class))).thenReturn(commentDto);

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(commentRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is("Great drill!")));
    }
}

