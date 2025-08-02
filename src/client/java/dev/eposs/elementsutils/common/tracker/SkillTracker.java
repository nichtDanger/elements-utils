package dev.eposs.elementsutils.common.tracker;

import dev.eposs.elementsutils.ElementsUtils;
import dev.eposs.elementsutils.util.Util;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkillTracker {
    
    private static final Pattern PATTERN = Pattern.compile(Skill.regexGroup() + ": ([\\d.]+)/([\\d.]+) XP");

    public static final PlayerSkills playerData = new PlayerSkills();
    
    /**
     * Updates the current XP and next level XP for a player's skills based on the provided text.
     *
     * @param text the text containing skill XP information from the actionbar.
     */
    public static void update(@NotNull Text text) {
        Matcher matcher = PATTERN.matcher(text.getString());
        
        while (matcher.find()) {
            Skill skillName = Skill.fromString(matcher.group(1));
            int currentXp = Util.parseXp(matcher.group(2));
            int nextLevelXp = Util.parseXp(matcher.group(3));

            switch (skillName) {
                case MINING -> playerData.mining.updateXp(currentXp, nextLevelXp);
                case FARMING -> playerData.farming.updateXp(currentXp, nextLevelXp);
                case FORAGING -> playerData.foraging.updateXp(currentXp, nextLevelXp);
                case FISHING -> playerData.fishing.updateXp(currentXp, nextLevelXp);
                case COMBAT -> playerData.combat.updateXp(currentXp, nextLevelXp);
                case MAGIC -> playerData.magic.updateXp(currentXp, nextLevelXp);
                case null, default -> ElementsUtils.LOGGER.warn("Unknown skill: {}", matcher.group(1));
            }
        }
    }
}
