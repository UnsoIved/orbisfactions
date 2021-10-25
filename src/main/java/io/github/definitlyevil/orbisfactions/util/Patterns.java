package io.github.definitlyevil.orbisfactions.util;

import java.util.regex.Pattern;

public class Patterns {

    public static final Pattern USERNAME = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    public static final Pattern FACTION_NAME = Pattern.compile("^[a-zA-Z_]+[a-zA-Z0-9_]*$");

}
