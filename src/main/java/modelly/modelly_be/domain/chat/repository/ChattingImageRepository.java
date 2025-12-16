package modelly.modelly_be.domain.chat.repository;

import modelly.modelly_be.domain.chat.entity.Chatting;
import modelly.modelly_be.domain.chat.entity.ChattingImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChattingImageRepository extends JpaRepository<ChattingImage, Long> {

    // 채팅 이미지들 전부 다 조회
    List<ChattingImage> findAllByChattingIn(List<Chatting> chattings);
}