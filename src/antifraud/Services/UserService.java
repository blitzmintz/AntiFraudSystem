package antifraud.Services;

import antifraud.Config.UserDetailsConfig;
import antifraud.DTO.UpdateRoleRequestDTO;
import antifraud.DTO.UpdateStatusRequest;
import antifraud.DTO.UserRequestDTO;
import antifraud.DTO.UserResponseDTO;
import antifraud.Enums.Roles;
import antifraud.Errors.AlreadyExistsException;
import antifraud.Errors.InvalidRoleUpdateException;
import antifraud.Errors.UserNotFoundException;

import antifraud.Model.User;

import antifraud.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder bcryptEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.bcryptEncoder = encoder;

    }

    public List<UserResponseDTO> getAllUsers() {
        Iterable<User> users = userRepository.findAll();
        List<UserResponseDTO> userList = new ArrayList<>();
        for (User user : users) {
            userList.add(new UserResponseDTO(user.getId(), user.getName(), user.getUsername(), user.getRoleText()));
        }
        return userList;
    }


    public User createNewUser(UserRequestDTO userRequestDTO) {
        if (userRepository.findByUsername(userRequestDTO.username().toLowerCase()).isPresent()) {
            throw new AlreadyExistsException();
        }
        User user = new User(userRequestDTO.name()
                    , userRequestDTO.username().toLowerCase()
                    , bcryptEncoder.encode(userRequestDTO.password())
                    );
        setUserRole(user);
        userRepository.save(user);
        return user;
    }

    public void setUserRole(User user) {
        if (userRepository.findAll().isEmpty()) {
            user.setRole(Roles.ADMINISTRATOR);
            user.setLocked(false);
        } else {
            user.setRole(Roles.MERCHANT);
            user.setLocked(true);
        }
    }

    public User deleteUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username.toLowerCase());
        if (user.isEmpty()) {
            throw new UserNotFoundException();
        }
        User userToDelete = user.get();
        userRepository.delete(userToDelete);
        return userToDelete;
    }

    public User updateUserRole(UpdateRoleRequestDTO updateRoleRequestDTO) {
        if (!updateRoleRequestDTO.role().equals("SUPPORT") && !updateRoleRequestDTO.role().equals("MERCHANT")) {
            throw new InvalidRoleUpdateException();
        }
        Optional<User> userForUpdate = userRepository.findByUsername(updateRoleRequestDTO.username().toLowerCase());
        if (userForUpdate.isEmpty()) {
            throw new UserNotFoundException();
        }
        User existingUser = userForUpdate.get();
        if (existingUser.getRoleText().equals(updateRoleRequestDTO.role())) {
            throw new AlreadyExistsException();
        }

        existingUser.setRole(Roles.valueOf(updateRoleRequestDTO.role()));

        return userRepository.save(existingUser);
    }

    public User setAccountStatus(UpdateStatusRequest updateStatusRequest) {
        Optional<User> userForUpdate = userRepository.findByUsername(updateStatusRequest.username().toLowerCase());
        if (userForUpdate.isEmpty()) {
            throw new UserNotFoundException();
        }
        User existingUser = userForUpdate.get();
        boolean toBeLocked = updateStatusRequest.operation().equals("LOCK");
        if (existingUser.getRoleText().equals("ADMINISTRATOR") && toBeLocked) {
            throw new InvalidRoleUpdateException();
        }
        existingUser.setLocked(toBeLocked);

        return userRepository.save(existingUser);

    }



}
