package modelly.modelly_be.domain.chat.repository;

import modelly.modelly_be.domain.chat.entity.ChatRoom;
import modelly.modelly_be.domain.chat.entity.Chatting;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChattingRepository extends JpaRepository<Chatting, Long> {


    // 채팅방들의 마지막 메세지를 리스트로 반환
    @Query("""
        SELECT c FROM Chatting c
        WHERE c.id IN (
            SELECT MAX(c2.id) FROM Chatting c2
            WHERE c2.chatRoom IN :rooms
            GROUP BY c2.chatRoom
        )
        """)
    List<Chatting> findLastMessagesByChatRooms(@Param("rooms") List<ChatRoom> rooms);

    // 최근 메시지부터 size개 (cursorID 없는 최초 요청 시 이용)
    List<Chatting> findByChatRoomOrderByIdDesc(ChatRoom chatRoom, Pageable pageable);

    // cursorMessageId 이전의 메시지들 size개 반환
    List<Chatting> findByChatRoomAndIdLessThanOrderByIdDesc(ChatRoom chatRoom, Long id, Pageable pageable);

    @Query("""
        SELECT c.chatRoom.id AS roomId, COUNT(c) AS unreadCount
        FROM Chatting c
        WHERE c.chatRoom IN :rooms
          AND c.isRead = false
          AND c.senderId <> :currentUserId
        GROUP BY c.chatRoom.id
    """)
    List<UnreadCountProjection> countUnreadByRooms(
            @Param("rooms") List<ChatRoom> rooms,
            @Param("currentUserId") Long currentUserId
    );

    interface UnreadCountProjection {
        Long getRoomId();
        long getUnreadCount();
    }

    /* 특정 채팅방의 안읽은 메세지 조회 후 read로 상태 변경(채팅방 입장 시 이용) */
    @Modifying
    @Query("""
        UPDATE Chatting c
        SET c.isRead = true
        WHERE c.chatRoom = :room
          AND c.isRead = false
          AND c.senderId <> :currentUserId
    """)
    int markUnreadMessagesAsReadInRoom(
            @Param("room") ChatRoom room,
            @Param("currentUserId") Long currentUserId
    );

    /* 특정 메세지까지 읽음 처리 */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
    UPDATE Chatting c
    SET c.isRead = true
    WHERE c.chatRoom = :room
      AND c.senderId <> :currentUserId
      AND c.id <= :lastMessageId
      AND c.isRead = false
    """)
    int readMessagesUpToId(
            @Param("room") ChatRoom room,
            @Param("currentUserId") Long currentUserId,
            @Param("lastMessageId") Long lastMessageId
    );

    /* 특정 유저가 안읽은 메세지 전체 조회 (홈화면에서 안읽은 메세지 보여줄 때 이용)*/
    @Query("""
        SELECT COUNT(c)
        FROM Chatting c
        JOIN c.chatRoom r
        WHERE c.isRead = false
          AND c.senderId <> :userId
          AND (
                r.model.user.id = :userId
             OR r.designer.user.id = :userId
          )
    """)
    long countTotalUnreadByUser(@Param("userId") Long userId);

    @Query("""
    SELECT MAX(c.id)
    FROM Chatting c
    WHERE c.chatRoom = :room
      AND c.senderId <> :currentUserId
      AND c.isRead = true
    """)
    Long findLastReadMessageIdByRoomAndReader(
            @org.springframework.data.repository.query.Param("room") ChatRoom room,
            @org.springframework.data.repository.query.Param("currentUserId") Long currentUserId
    );
}
