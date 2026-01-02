package modelly.modelly_be.domain.profile.repository;

import modelly.modelly_be.domain.profile.dto.response.DesignerProfileResponse.*;

import java.util.List;

public interface ProfileRecruitmentQueryRepository {
    List<RecruitmentCard> findOpenRecruitmentsByDesigner(Long designerId);
}
