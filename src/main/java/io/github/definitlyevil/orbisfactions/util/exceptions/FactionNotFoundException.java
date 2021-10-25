package io.github.definitlyevil.orbisfactions.util.exceptions;

public class FactionNotFoundException extends OBFException {

    private final String name;

    public FactionNotFoundException(String name) {
        this.name = name;
    }

    public String getRequestedFactionName() {
        return name;
    }

}
