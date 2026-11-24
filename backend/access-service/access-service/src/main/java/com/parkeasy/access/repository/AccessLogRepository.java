package com.parkeasy.access.repository;

import com.parkeasy.access.entity.AccessLog;
import com.parkeasy.access.entity.EventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

    Optional<AccessLog> findFirstByTicketIdAndEventTypeOrderByEventTimeDesc(Long ticketId, EventType eventType);
}