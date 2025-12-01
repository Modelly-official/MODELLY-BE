package modelly.modelly_be.domain.chat.repository;

import io.lettuce.core.dynamic.annotation.Param;
import modelly.modelly_be.domain.chat.entity.ChatRoom;
import modelly.modelly_be.domain.chat.entity.Chatting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChattingRepository extends JpaRepository<Chatting, Long> {

    Chatting findTopByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom);

    // 해당 채팅방의 모든 메세지를 오래된 순으로
    List<Chatting> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);

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

}