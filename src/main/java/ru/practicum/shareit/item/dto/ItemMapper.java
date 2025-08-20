package ru.practicum.shareit.item.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(source = "owner.id", target = "ownerId")
    ItemDto toDto(Item item);

    Item toItem(ItemDto itemDto);

    @Mapping(source = "item.id", target = "id")
    @Mapping(target = "lastBooking", source = "lastBooking")
    @Mapping(target = "nextBooking", source = "nextBooking")
    @Mapping(target = "comments", source = "comments")
    @Mapping(source = "item.owner.id", target = "ownerId")
    ItemWithBookingsAndCommentsDto toItemWithBookingsAndCommentsDto(Item item, Booking lastBooking, Booking nextBooking, List<Comment> comments);
}