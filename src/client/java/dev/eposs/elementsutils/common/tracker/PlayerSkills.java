package dev.eposs.elementsutils.common.tracker;

import dev.eposs.elementsutils.ElementsUtils;

public class PlayerSkills {
    public final SkillData mining = new SkillData(Skill.MINING);
    public final SkillData farming = new SkillData(Skill.FARMING);
    public final SkillData foraging = new SkillData(Skill.FORAGING);
    public final SkillData fishing = new SkillData(Skill.FISHING);
    public final SkillData combat = new SkillData(Skill.COMBAT);
    public final SkillData magic = new SkillData(Skill.MAGIC);

    protected PlayerSkills() {
    }
    
    public static class SkillData {
        public final Skill skill;
        private int currentXp;
        private int nextLevelXp;

        private SkillData(Skill skill) {
            this.skill = skill;
        }

        public int getCurrentXp() {
            return currentXp;
        }

        public int getNextLevelXp() {
            return nextLevelXp;
        }

        public void updateXp(int currentXp, int nextLevelXp) {
            this.currentXp = currentXp;
            this.nextLevelXp = nextLevelXp;
        }

        public int getCurrentLevel() {
            // TODO: Calculate current level based on nextLevelXp
            return 0;
        }
    }
}
