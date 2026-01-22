package modelly.modelly_be.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modelly.modelly_be.domain.chat.repository.ChatRoomRepository;
import modelly.modelly_be.domain.like.repository.designerLikeRepository.DesignerLikeRepository;
import modelly.modelly_be.domain.like.repository.recruitmentLikeRepository.RecruitmentLikeRepository;
import modelly.modelly_be.domain.portfolio.entity.Portfolio;
import modelly.modelly_be.domain.portfolio.repository.PortfolioRepository;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.recruitment.repository.recruitmentRepository.RecruitmentRepository;
import modelly.modelly_be.domain.reservation.repository.ReservationRepository;
import modelly.modelly_be.domain.review.entity.Review;
import modelly.modelly_be.domain.review.repository.ReplyRepository;
import modelly.modelly_be.domain.review.repository.reviewRepository.ReviewRepository;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserHardDeleteService {

    private final ReservationRepository reservationRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final PortfolioRepository portfolioRepository;
    private final ReviewRepository reviewRepository;
    private final ReplyRepository replyRepository;
    private final DesignerLikeRepository designerLikeRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final RecruitmentLikeRepository recruitmentLikeRepository;

    /**
     * 디자이너 탈퇴 시 연관 데이터 정리
     */
    @Transactional
    public void deleteDesignerData(Designer designer) {
        Long designerId = designer.getId();
        log.info("디자이너 데이터 DB 정리 시작: designerId={}", designerId);

        /* --- 포트폴리오 삭제(PortfolioImage는 Entity의 CascadeType.ALL 설정에 의해 자동 삭제) --- */
        List<Portfolio> portfolios = portfolioRepository.findAllByDesignerId(designerId);
        portfolioRepository.deleteAll(portfolios);

        /* --- 공고 삭제(image, time 등 cascade 설정에 의해 자동 삭제) --- */
        List<Recruitment> recruitments = recruitmentRepository.findAllByDesignerId(designerId);
        recruitmentRepository.deleteAll(recruitments);

        /* --- 리뷰 및 답글 삭제 --- */
        // 디자이너가 쓴 답글(Reply) 삭제
        replyRepository.deleteByDesignerId(designerId);

        // 디자이너에게 달린 리뷰(Review) 삭제(ReviewImage는 Cascade에 의해 자동 삭제)
        List<Review> reviews = reviewRepository.findAllByDesignerId(designerId);
        reviewRepository.deleteAll(reviews);

        /* --- 디자이너찜(DesignerLike) - 해당 디자이너를 찜한 내역 삭제 --- */
        designerLikeRepository.deleteByDesignerId(designerId);

        /* --- 예약 내역 (Designer -> NULL) --- */
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

        /* --- 예약 내역 (Reservation -> NULL) --- */
        reservationRepository.setModelNull(modelId);

        /* --- 채팅방 (ChatRoom -> NULL) --- */
        chatRoomRepository.setModelNull(modelId);

        log.info("모델 연관 데이터 DB 정리 완료");
    }
}