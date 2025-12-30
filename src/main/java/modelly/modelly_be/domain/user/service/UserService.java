package modelly.modelly_be.domain.user.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void save(User user) {
        userRepository.save(user);
    }
}
