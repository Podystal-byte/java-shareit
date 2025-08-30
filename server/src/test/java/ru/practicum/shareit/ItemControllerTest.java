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
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemWithBookingsAndCommentsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.CommentService;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;


import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @MockBean
    private ItemMapper itemMapper;

    @MockBean
    private CommentMapper commentMapper;

    @MockBean
    private CommentService commentService;

    private ItemDto itemDto;
    private Item item;
    private ItemWithBookingsAndCommentsDto itemWithBookingsAndCommentsDto;
    private CommentRequestDto commentRequestDto;
    private Comment comment;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        User owner = new User();
        owner.setId(1L);

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);

        itemWithBookingsAndCommentsDto = new ItemWithBookingsAndCommentsDto();
        itemWithBookingsAndCommentsDto.setId(1L);
        itemWithBookingsAndCommentsDto.setName("Test Item");

        commentRequestDto = new CommentRequestDto();
        commentRequestDto.setText("Test comment");

        comment = new Comment();
        comment.setId(1L);
        comment.setText("Test comment");

        commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Test comment");
    }

    @Test
    void addItem_whenItemIsValid_thenReturnsCreatedItem() throws Exception {
        when(itemService.addItem(anyLong(), any(ItemDto.class))).thenReturn(item);
        when(itemMapper.toDto(any(Item.class))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(itemDto.getName())));
    }

    @Test
    void addItem_whenHeaderIsMissing_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_whenItemIsValid_thenReturnsUpdatedItem() throws Exception {
        when(itemService.updateItem(anyLong(), anyLong(), any(Item.class))).thenReturn(item);
        when(itemMapper.toDto(any(Item.class))).thenReturn(itemDto);

        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(itemDto.getName())));
    }

    @Test
    void getItemById_whenItemExists_thenReturnsItem() throws Exception {
        when(itemService.getItemById(anyLong(), anyLong())).thenReturn(itemWithBookingsAndCommentsDto);

        mockMvc.perform(get("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(itemWithBookingsAndCommentsDto.getName())));
    }

    @Test
    void getOwnersItems_whenCalled_thenReturnsListOfItems() throws Exception {
        when(itemService.getItemsByOwner(anyLong())).thenReturn(List.of(item));
        when(itemMapper.toDto(any(Item.class))).thenReturn(itemDto);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())));
    }

    @Test
    void searchItems_whenTextIsProvided_thenReturnsListOfItems() throws Exception {
        when(itemService.searchItems(anyString())).thenReturn(List.of(item));
        when(itemMapper.toDto(any(Item.class))).thenReturn(itemDto);

        mockMvc.perform(get("/items/search")
                        .param("text", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())));
    }

    @Test
    void addComment_whenCommentIsValid_thenReturnsCreatedComment() throws Exception {
        when(commentService.addComment(anyLong(), anyLong(), any(CommentRequestDto.class))).thenReturn(comment);
        when(commentMapper.toCommentDto(any(Comment.class))).thenReturn(commentDto);

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(commentDto.getText())));
    }
}