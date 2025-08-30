package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.repo.ItemRequestRepository;
import ru.practicum.shareit.user.repo.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // <--- РЕКОМЕНДУЕТСЯ ДЛЯ СЕРВИСОВ, ГДЕ В ОСНОВНОМ ЧТЕНИЕ
public class ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRequestMapper requestMapper;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Transactional // <--- Явно указываем, что метод изменяет данные
    public ItemRequestDto createRequest(Long userId, ItemRequestDto itemRequestDto) {
        // Ваша логика создания запроса почти верна, но можно сделать чуть проще
        var requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        ItemRequest itemRequest = requestMapper.toItem(itemRequestDto);
        itemRequest.setRequester(requester);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setItems(Collections.emptyList()); // Инициализируем пустым списком
        return requestMapper.toDto(requestRepository.save(itemRequest));
    }

    public List<ItemRequestDto> getRequestsByUserId(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        // Сортируем по дате создания, чтобы новые были первыми
        List<ItemRequest> requests = requestRepository.findByRequesterIdOrderByCreatedDesc(userId);
        return requests.stream()
                .map(requestMapper::toDto) // <--- Просто маппим, вся магия происходит в маппере
                .collect(Collectors.toList());
    }

    public List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("created").descending());
        List<ItemRequest> requests = requestRepository.findByRequesterIdNot(userId, pageable);
        return requests.stream()
                .map(requestMapper::toDto) // <--- Просто маппим
                .collect(Collectors.toList());
    }

    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        ItemRequest request = requestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("Request not found"));
        // Эта строка должна работать, если request_id корректно сохраняется
        List<ItemDto> items = itemRepository.findByRequestId(requestId).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
        ItemRequestDto dto = requestMapper.toDto(request);
        dto.setItems(items);
        return dto;
    }
}