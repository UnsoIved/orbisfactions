package io.github.definitlyevil.orbisfactions;

import java.util.HashMap;
import java.util.Map;

public enum OBFRole {

    OWNER(0, "Owner"),
    COOWNER(20, "Co-owner"),
    MOD(50, "Mod"),
    MEMBER(100, "Member"),
    OUTSIDER(Integer.MAX_VALUE, "OUTSIDER");

    public static final Map<Integer, OBFRole> roles = new HashMap<>();
    static {
        for(OBFRole k : values()) {
            roles.put(k.roleNumber, k);
        }
    }

    private final int roleNumber;
    private final String displayText;

    OBFRole(int roleNumber, String displayText) {
        this.roleNumber = roleNumber;
        this.displayText = displayText;
    }

    public int getRoleNumber() {
        return roleNumber;
    }

    public String getDisplayText() {
        return displayText;
    }

    public boolean checkRole(OBFRole includes) {
        return roleNumber <= includes.roleNumber;
    }

    public boolean isMember() {
        return roleNumber <= MEMBER.roleNumber;
    }

    public static OBFRole from(int number) {
        if(!roles.containsKey(number)) return OUTSIDER;
        return roles.get(number);
    }

}
