package ru.practicum.shareit.item.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(source = "owner.id", target = "ownerId")
    ItemDto toDto(Item item);

    Item toItem(ItemDto itemDto);
}