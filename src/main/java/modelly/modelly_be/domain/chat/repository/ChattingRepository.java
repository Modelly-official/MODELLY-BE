package modelly.modelly_be.domain.chat.repository;

import modelly.modelly_be.domain.chat.entity.ChatRoom;
import modelly.modelly_be.domain.chat.entity.Chatting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChattingRepository extends JpaRepository<Chatting, Long> {
}