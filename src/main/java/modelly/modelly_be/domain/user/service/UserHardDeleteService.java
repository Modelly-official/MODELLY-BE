package modelly.modelly_be.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modelly.modelly_be.domain.chat.repository.ChatRoomRepository;
import modelly.modelly_be.domain.like.repository.designerLikeRepository.DesignerLikeRepository;
import modelly.modelly_be.domain.like.repository.recruitmentLikeRepository.RecruitmentLikeRepository;
import modelly.modelly_be.domain.portfolio.entity.Portfolio;
import modelly.modelly_be.domain.portfolio.repository.PortfolioImageRepository;
import modelly.modelly_be.domain.portfolio.repository.PortfolioRepository;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.recruitment.repository.RecruitmentDateRepository;
import modelly.modelly_be.domain.recruitment.repository.RecruitmentImageRepository;
import modelly.modelly_be.domain.recruitment.repository.RecruitmentTimeRepository;
import modelly.modelly_be.domain.recruitment.repository.recruitmentRepository.RecruitmentRepository;
import modelly.modelly_be.domain.reservation.repository.ReservationChangeRepository;
import modelly.modelly_be.domain.reservation.repository.ReservationRepository;
import modelly.modelly_be.domain.review.entity.Review;
import modelly.modelly_be.domain.review.repository.ReplyRepository;
import modelly.modelly_be.domain.review.repository.ReviewImageRepository;
import modelly.modelly_be.domain.review.repository.reviewRepository.ReviewRepository;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserHardDeleteService {

    private final ReservationRepository reservationRepository;
    private final ReservationChangeRepository reservationChangeRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioImageRepository portfolioImageRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReplyRepository replyRepository;
    private final DesignerLikeRepository designerLikeRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final RecruitmentTimeRepository recruitmentTimeRepository;
    private final RecruitmentDateRepository recruitmentDateRepository;
    private final RecruitmentImageRepository recruitmentImageRepository;
    private final RecruitmentLikeRepository recruitmentLikeRepository;
    private final UserRepository userRepository;

    /**
     * 유저 탈퇴 통합 관리
     * 역할에 따라 연관 데이터를 정리한 후, 최종적으로 User 엔티티를 삭제
     */
    @Transactional
    public void deleteUser(User user) {
        Long userId = user.getId();

        // (공통) 예약 변경 요청 내역 삭제
        reservationChangeRepository.deleteAllByUserId(userId);

        // 디자이너인 경우의 연관 데이터 정리
        if (user.getDesigner() != null) {
            deleteDesignerData(user.getDesigner());
        }

        // 모델인 경우의 연관 데이터 정리
        if (user.getModel() != null) {
            deleteModelData(user.getModel());
        }

        // 유저 삭제
        userRepository.delete(user);
        log.info("유저 삭제 완료: userId={}", user.getId());
    }

    /**
     * 디자이너 탈퇴 시 연관 데이터 정리
     */
    @Transactional
    public void deleteDesignerData(Designer designer) {
        Long designerId = designer.getId();
        log.info("디자이너 데이터 DB 정리 시작: designerId={}", designerId);

        /* --- 포트폴리오 삭제 --- */
        // 포트폴리오 이미지 삭제
        portfolioImageRepository.deleteByDesignerId(designerId);

        // 포트폴리오 삭제
        portfolioRepository.deleteByDesignerId(designerId);

        /* --- 공고 삭제 전 예약과의 연결 끊기 --- */
        reservationRepository.setRecruitmentNullByDesignerId(designerId);

        /* --- 공고 삭제 (연관관계 순으로 삭제) --- */
        recruitmentTimeRepository.deleteByDesignerId(designerId);
        recruitmentDateRepository.deleteByDesignerId(designerId);
        recruitmentImageRepository.deleteByDesignerId(designerId);
        recruitmentLikeRepository.deleteByDesignerId(designerId);
        recruitmentRepository.deleteByDesignerId(designerId);

        /* --- 리뷰 및 답글 삭제 --- */
        // 디자이너가 쓴 답글(Reply) 삭제
        replyRepository.deleteByDesignerId(designerId);

        // 디자이너에게 달린 리뷰 이미지 -> 리뷰 삭제
        reviewImageRepository.deleteByDesignerId(designerId);
        reviewRepository.deleteByDesignerId(designerId);

        /* --- 디자이너찜(DesignerLike) - 해당 디자이너를 찜한 내역 삭제 --- */
        designerLikeRepository.deleteByDesignerId(designerId);

        /* --- 예약 처리--- */
        // PENDING(대기) 상태의 예약은 삭제
        reservationRepository.deletePendingByDesignerId(designerId);

        // 남은 예약(CONFIRMED, CANCELLED 등)은 익명 처리 (NULL)
        reservationRepository.setDesignerNull(designerId);

        /* ---채팅방 (Designer -> NULL) --- */
        chatRoomRepository.setDesignerNull(designerId);

        log.info("디자이너 연관 데이터 DB 정리 완료");
    }

    /**
     * 모델 탈퇴 시 연관 데이터 정리
     */
    @Transactional
    public void deleteModelData(Model model) {
        Long modelId = model.getId();
        log.info("모델 데이터 DB 정리 시작: modelId={}", modelId);

        /* --- 찜 내역 삭제(DesignerLike, RecruitmentLike) --- */
        designerLikeRepository.deleteByModelId(modelId);
        recruitmentLikeRepository.deleteByModelId(modelId);

        /* --- 리뷰 (Review -> NULL) --- */
        reviewRepository.setModelNull(modelId);

        /* --- 예약 처리 --- */
        // PENDING(대기) 상태의 예약은 삭제
        reservationRepository.deletePendingByModelId(modelId);

        // 남은 예약(CONFIRMED, CANCELLED 등)은 익명 처리 (NULL)
        reservationRepository.setModelNull(modelId);

        /* --- 채팅방 (ChatRoom -> NULL) --- */
        chatRoomRepository.setModelNull(modelId);

        log.info("모델 연관 데이터 DB 정리 완료");
    }
}