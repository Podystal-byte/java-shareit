package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.repo.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemRequestMapper requestMapper;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemRequestService itemRequestService;

    private User requester;
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setId(1L);
        requester.setName("testRequester");
        requester.setEmail("requester@user.ru");

        itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("description");
        itemRequest.setRequester(requester);

        itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(1L);
        itemRequestDto.setDescription("description");
    }

    @Test
    void createRequest_whenUserFound_thenSavesRequest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(requestMapper.toItem(any(ItemRequestDto.class))).thenReturn(itemRequest);
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);
        when(requestMapper.toDto(any(ItemRequest.class))).thenReturn(itemRequestDto);

        ItemRequestDto result = itemRequestService.createRequest(1L, itemRequestDto);

        assertNotNull(result);
        assertEquals(itemRequestDto.getDescription(), result.getDescription());
        verify(userRepository).findById(1L);
        verify(requestRepository).save(any(ItemRequest.class));
    }

    @Test
    void createRequest_whenUserNotFound_thenThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.createRequest(99L, itemRequestDto));
        verify(requestRepository, never()).save(any(ItemRequest.class));
    }

    @Test
    void getRequestsByUserId_whenUserFound_thenReturnsRequests() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(requestRepository.findByRequesterIdOrderByCreatedDesc(anyLong())).thenReturn(List.of(itemRequest));
        when(requestMapper.toDto(any(ItemRequest.class))).thenReturn(itemRequestDto);

        List<ItemRequestDto> result = itemRequestService.getRequestsByUserId(1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(userRepository).findById(1L);
    }

    @Test
    void getRequestsByUserId_whenUserNotFound_thenThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestsByUserId(99L));
        verify(requestRepository, never()).findByRequesterIdOrderByCreatedDesc(anyLong());
    }

    @Test
    void getAllRequests_whenRequestsExist_thenReturnsRequests() {
        List<ItemRequest> requests = List.of(itemRequest);
        Page<ItemRequest> page = new PageImpl<>(requests);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(requestRepository.findByRequesterIdNot(anyLong(), any(Pageable.class))).thenReturn(requests);
        when(requestMapper.toDto(any(ItemRequest.class))).thenReturn(itemRequestDto);

        List<ItemRequestDto> result = itemRequestService.getAllRequests(2L, 0, 10);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(userRepository).findById(2L);
        verify(requestRepository).findByRequesterIdNot(anyLong(), any(Pageable.class));
    }

    @Test
    void getRequestById_whenRequestFound_thenReturnsRequestWithItems() {
        Item item = new Item();
        item.setId(1L);
        item.setName("testItem");

        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("testItem");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(requestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findByRequestId(anyLong())).thenReturn(List.of(item));
        when(requestMapper.toDto(any(ItemRequest.class))).thenReturn(itemRequestDto);
        when(itemMapper.toDto(any(Item.class))).thenReturn(itemDto);

        ItemRequestDto result = itemRequestService.getRequestById(1L, 1L);

        assertNotNull(result);
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
        assertEquals(itemDto.getName(), result.getItems().get(0).getName());
        verify(userRepository).findById(1L);
        verify(requestRepository).findById(1L);
        verify(itemRepository).findByRequestId(1L);
    }

    @Test
    void getRequestById_whenUserNotFound_thenThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(99L, 1L));
        verify(requestRepository, never()).findById(anyLong());
    }

    @Test
    void getRequestById_whenRequestNotFound_thenThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(requestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(1L, 99L));
        verify(itemRepository, never()).findByRequestId(anyLong());
    }
}