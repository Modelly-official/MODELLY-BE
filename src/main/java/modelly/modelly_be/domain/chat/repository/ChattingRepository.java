package modelly.modelly_be.domain.chat.repository;

import modelly.modelly_be.domain.chat.entity.ChatRoom;
import modelly.modelly_be.domain.chat.entity.Chatting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChattingRepository extends JpaRepository<Chatting, Long> {

    Chatting findTopByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom);

    // 해당 채팅방의 모든 메세지를 오래된 순으로
    List<Chatting> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);
}