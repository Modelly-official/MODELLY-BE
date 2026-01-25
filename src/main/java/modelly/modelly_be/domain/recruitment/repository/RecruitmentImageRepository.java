package modelly.modelly_be.domain.recruitment.repository;

import modelly.modelly_be.domain.recruitment.entity.RecruitmentImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RecruitmentImageRepository extends JpaRepository<RecruitmentImage, Long> {
    // 해당 디자이너의 RecruitmentImage 삭제
    @Modifying
    @Query("""
        delete from RecruitmentImage ri
        where ri.recruitment.designer.id = :designerId
    """)
    void deleteByDesignerId(Long designerId);
}
