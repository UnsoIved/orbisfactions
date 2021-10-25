package io.github.definitlyevil.orbisfactions.util.exceptions;

public class PlayerNotFoundException extends OBFException {

    private final String username;

    public PlayerNotFoundException(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String getMessage() {
        return String.format("Player <%s> not found! He/she has to join the server at least once! ", username);
    }
}
