package modelly.modelly_be.domain.like.repository.designerLikeRepository;

import modelly.modelly_be.domain.like.entity.DesignerLike;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.global.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DesignerLikeRepository extends JpaRepository<DesignerLike, Long>, DesignerLikeRepositoryCustom {

    boolean existsByModelAndDesigner(Model model, Designer designer);

    void deleteByModelAndDesigner(Model model, Designer designer);

    @Query("SELECT COUNT(dl.id) FROM DesignerLike dl " +
            "WHERE dl.model = :model AND (:category IS NULL OR dl.designer.category = :category)")
    Long countByModelAndCategory(Model model, Category category);

    // 해당 디자이너가 포함된 찜 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM DesignerLike d WHERE d.designer.id = :designerId")
    void deleteByDesignerId(@Param("designerId") Long designerId);
}
