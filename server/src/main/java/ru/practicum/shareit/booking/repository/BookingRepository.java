package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status, Pageable pageable);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId " +
            "ORDER BY b.start DESC")
    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId AND b.status = :status " +
            "ORDER BY b.start DESC")
    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId AND b.end < :now " +
            "ORDER BY b.start DESC")
    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime now, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId AND b.start > :now " +
            "ORDER BY b.start DESC")
    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime now, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId AND b.start <= :now AND b.end >= :now " +
            "ORDER BY b.start DESC")
    List<Booking> findByItemOwnerIdCurrentOrderByStartDesc(Long ownerId, LocalDateTime now, Pageable pageable);

    List<Booking> findByItemIdAndBookerIdAndStatusAndEndBefore(
            Long itemId, Long bookerId, BookingStatus status, LocalDateTime now);

    List<Booking> findByItemIdAndEndBeforeOrderByEndDesc(Long itemId, LocalDateTime now);

    List<Booking> findByItemIdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime now);
}