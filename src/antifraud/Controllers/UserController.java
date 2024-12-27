package antifraud.Controllers;

import antifraud.DTO.*;
import antifraud.Model.User;
import antifraud.Services.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("api/auth/list")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping("api/auth/user")
    public ResponseEntity<UserResponseDTO> registerUser(@RequestBody @Valid UserRequestDTO userRequestDTO) {
        User user = userService.createNewUser(userRequestDTO);
        UserResponseDTO response = new UserResponseDTO(user.getId(), user.getName(), user.getUsername().toLowerCase(), user.getRoleText());
        //returns 201 CREATED
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("api/auth/user/{username}")
    public ResponseEntity<UserDeleteDTO> deleteUser(@PathVariable String username) {
        User deletedUser = userService.deleteUserByUsername(username.toLowerCase());
        return new ResponseEntity<>(new UserDeleteDTO(deletedUser.getUsername(), "Deleted successfully!"), HttpStatus.OK);
    }

    @PutMapping("api/auth/role")
    public ResponseEntity<UserResponseDTO> updateUser(@RequestBody @Valid UpdateRoleRequestDTO updateRoleRequestDTO) {
        User user = userService.updateUserRole(updateRoleRequestDTO);
        return new ResponseEntity<>(new UserResponseDTO(user.getId(), user.getName(), user.getUsername().toLowerCase(), user.getRoleText()), HttpStatus.OK);

    }

    @PutMapping("api/auth/access")
    public ResponseEntity<StatusResponseDTO> updateAccountStatus(@RequestBody UpdateStatusRequest updateStatusRequest) {
        User user = userService.setAccountStatus(updateStatusRequest);
        String userLocked = user.isLocked() ? "locked" : "unlocked";
        return new ResponseEntity<>(new StatusResponseDTO("User %s %s!".formatted(user.getUsername(), userLocked)), HttpStatus.OK);
    }



}
