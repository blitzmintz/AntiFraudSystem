package antifraud.Services;

import antifraud.Config.UserDetailsConfig;
import antifraud.Errors.UserNotFoundException;
import antifraud.Model.User;
import antifraud.Repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserService implements UserDetailsService {

    private final UserRepository userRepository;
    public CustomUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username.toLowerCase());
        if (user.isEmpty()) {
            throw new UserNotFoundException();
        }
        return new UserDetailsConfig(user.get());

    }
}
