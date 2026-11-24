package com.parkeasy.reservation.repository;

import com.parkeasy.reservation.entity.SpotLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpotLockRepository extends JpaRepository<SpotLock, Long> {

    @Query(value = "SELECT * FROM spot_locks WHERE spot_id = :spotId FOR UPDATE",
           nativeQuery = true)
    Optional<SpotLock> findForUpdate(@Param("spotId") Long spotId);
}