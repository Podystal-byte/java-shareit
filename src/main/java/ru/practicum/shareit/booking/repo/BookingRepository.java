package ru.practicum.shareit.booking.repo;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.Status;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByBookerIdAndStatus(Long bookerId, Status status, Sort sort);

    List<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime now, Sort sort);

    List<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfter(Long bookerId, LocalDateTime now, LocalDateTime nowAgain, Sort sort);

    List<Booking> findByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime now, Sort sort);

    List<Booking> findByItemOwnerId(Long ownerId, Sort sort);

    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, Status status, Sort sort);

    List<Booking> findByItemOwnerIdAndEndIsBefore(Long ownerId, LocalDateTime now, Sort sort);

    List<Booking> findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(Long ownerId, LocalDateTime now, LocalDateTime nowAgain, Sort sort);

    List<Booking> findByItemOwnerIdAndStartIsAfter(Long ownerId, LocalDateTime now, Sort sort);

    List<Booking> findByBookerIdAndItem_IdAndEndIsBefore(Long userId, Long itemId, LocalDateTime now);

    List<Booking> findByItem_IdAndStartIsBeforeAndStatus(Long itemId, LocalDateTime now, Status status, Sort sort);

    List<Booking> findByItem_IdAndStartIsAfterAndStatus(Long itemId, LocalDateTime now, Status status, Sort sort);
}