package dev.eposs.elementsutils.common.tracker;

import java.util.Arrays;

public enum Skill {
    MINING,
    FARMING,
    FORAGING,
    FISHING,
    COMBAT,
    MAGIC;

    /**
     * MINING -> Mining
     */
    public final String skillName = name().charAt(0) + name().substring(1).toLowerCase();

    public static String regexGroup() {
        StringBuilder regex = new StringBuilder("(");
        for (Skill value : Skill.values()) {
            regex.append(value.skillName).append("|");
        }
        return regex.substring(0, regex.length() - 1) + ")";
    }

    public static Skill fromString(String skillName) {
        return Arrays.stream(Skill.values()).filter(value -> value.skillName.equalsIgnoreCase(skillName)).findFirst().orElse(null);
    }
}
