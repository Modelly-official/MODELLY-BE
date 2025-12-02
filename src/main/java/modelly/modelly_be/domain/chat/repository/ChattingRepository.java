package modelly.modelly_be.domain.chat.repository;

import io.lettuce.core.dynamic.annotation.Param;
import modelly.modelly_be.domain.chat.entity.ChatRoom;
import modelly.modelly_be.domain.chat.entity.Chatting;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}