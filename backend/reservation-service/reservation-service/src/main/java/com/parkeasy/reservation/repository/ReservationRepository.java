package com.parkeasy.reservation.repository;

import com.parkeasy.reservation.entity.Reservation;
import com.parkeasy.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserIdOrderByStartTimeDesc(Long userId);

    /** Version SQL native : TiDB n'accepte pas le "FOR UPDATE OF alias" généré par Hibernate. */
    @Query(value = """
            SELECT * FROM reservations
            WHERE spot_id = :spotId
              AND status IN (:statuses)
              AND start_time < :end
              AND end_time > :start
            FOR UPDATE
            """, nativeQuery = true)
    List<Reservation> findOverlappingForUpdateNative(@Param("spotId") Long spotId,
                                                     @Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end,
                                                     @Param("statuses") Collection<String> statuses);

    /** Lecture verrouillante : même signature qu'avant, donc ReservationWriter ne change pas. */
    default List<Reservation> findOverlappingForUpdate(Long spotId,
                                                       LocalDateTime start,
                                                       LocalDateTime end,
                                                       Collection<ReservationStatus> statuses) {
        return findOverlappingForUpdateNative(spotId, start, end,
                statuses.stream().map(Enum::name).toList());
    }

    @Query("select distinct r.spotId from Reservation r where r.parkingId = :parkingId and r.status in :statuses "
         + "and r.startTime < :end and r.endTime > :start")
    List<Long> findBusySpotIds(@Param("parkingId") Long parkingId,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end,
                               @Param("statuses") Collection<ReservationStatus> statuses);

    List<Reservation> findBySpotIdAndStatusInAndEndTimeAfterOrderByStartTimeAsc(
            Long spotId, Collection<ReservationStatus> statuses, LocalDateTime after);
}