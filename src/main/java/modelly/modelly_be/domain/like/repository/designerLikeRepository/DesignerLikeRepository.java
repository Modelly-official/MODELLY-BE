package modelly.modelly_be.domain.like.repository.designerLikeRepository;

import modelly.modelly_be.domain.like.entity.DesignerLike;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignerLikeRepository extends JpaRepository<DesignerLike, Long>, DesignerLikeRepositoryCustom {

    boolean existsByModelAndDesigner(Model model, Designer designer);

    void deleteByModelAndDesigner(Model model, Designer designer);
}
