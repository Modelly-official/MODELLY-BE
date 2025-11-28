package modelly.modelly_be.domain.recruitment.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.recruitment.repository.RecruitmentRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private final RecruitmentRepository recruitmentRepository;

    public void save(Recruitment recruitment) {
        recruitmentRepository.save(recruitment);
    }

}
