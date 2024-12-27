package antifraud.Enums;

public enum Roles {
    ADMINISTRATOR("ADMINISTRATOR"),
    MERCHANT("MERCHANT"),
    SUPPORT("SUPPORT");
    private final String role;

    Roles(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }


}

