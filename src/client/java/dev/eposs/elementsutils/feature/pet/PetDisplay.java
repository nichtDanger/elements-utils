package dev.eposs.elementsutils.feature.pet;

import dev.eposs.elementsutils.config.ModConfig;
import net.minecraft.text.Text;

public class PetDisplay {
    private static int currentXP;
    private static int nextLvlXP;

    public static void updateCurrentPetXP(Text text) {
        String msg = text.getString();
        if (!msg.contains("Pet: ")) return;

        int start = msg.indexOf("Pet: ");
        int slash = msg.indexOf("/", start);
        int end = msg.indexOf(" XP", slash);

        currentXP = Integer.parseInt(msg.substring(start + 5, slash));
        nextLvlXP = Integer.parseInt(msg.substring(slash + 1, end));
    }

    public static void render() {
        if (!ModConfig.getConfig().showPetDisplay) return;
    }
}
