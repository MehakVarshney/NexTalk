package com.nextalk.chat.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nextalk.chat.entity.RoomMember;

public interface RoomMemberRepository extends JpaRepository<RoomMember, UUID> {

    boolean existsByRoomIdAndUserId(UUID roomId, UUID userId);

    Optional<RoomMember> findByRoomIdAndUserId(UUID roomId, UUID userId);

    List<RoomMember> findByRoomId(UUID roomId);

    List<RoomMember> findByUserId(UUID userId);

    @Query("""
            select rm.room.id
            from RoomMember rm
            where rm.userId = :firstUserId or rm.userId = :secondUserId
            group by rm.room.id
            having count(distinct rm.userId) = 2
            """)
    List<UUID> findCommonRoomIds(@Param("firstUserId") UUID firstUserId, @Param("secondUserId") UUID secondUserId);
}
