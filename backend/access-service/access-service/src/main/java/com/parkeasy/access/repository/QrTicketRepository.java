package com.parkeasy.access.repository;

import com.parkeasy.access.entity.QrTicket;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QrTicketRepository extends JpaRepository<QrTicket, Long> {

    Optional<QrTicket> findByReservationId(Long reservationId);

    Optional<QrTicket> findByToken(String token);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from QrTicket t where t.token = :token")
    Optional<QrTicket> findByTokenForUpdate(@Param("token") String token);
}