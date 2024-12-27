package antifraud.Model;

import antifraud.Enums.Roles;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "USERS")
public class User {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column
    @NotBlank
    private String name;
    @Column(unique = true)
    @NotBlank
    private String username;
    @Column
    @NotBlank
    private String password;
    @Column
    private boolean isLocked;

    @Enumerated(EnumType.STRING)
    private Roles role;

    public User(String name, String username, String password, Roles role) {
        this.username = username.toLowerCase();
        this.name = name;
        this.password = password;
        this.role = role;
        this.isLocked = !role.getRole().equals("ADMINISTRATOR");
    }

    public User(String name, String username, String password) {
        this.username = username.toLowerCase();
        this.name = name;
        this.password = password;
        this.role = Roles.MERCHANT;
        this.isLocked = true;
    }

    public String getRoleText() {
        return role.getRole();
    }

}
