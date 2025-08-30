package ru.practicum.shareit.request.dto;

import org.mapstruct.Mapper;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.request.ItemRequest;

@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface ItemRequestMapper {

    ItemRequest toItem(ItemRequestDto itemRequestDto);

    ItemRequestDto toDto(ItemRequest request);
}