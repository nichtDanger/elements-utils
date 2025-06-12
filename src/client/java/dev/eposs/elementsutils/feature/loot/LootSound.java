package dev.eposs.elementsutils.feature.loot;

import dev.eposs.elementsutils.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class LootSound {
    public static boolean onGameMessage(Text text, boolean b) {
        if (ModConfig.getConfig().playLootSound) {
            String string = text.getString();
            // TODO: check actual messages
            if (string.startsWith("Loot:")) playsound();
            if (string.startsWith("Sb-Loot:")) playsound();
            if (string.startsWith("Pb-Loot:")) playsound();
        }

        return true;
    }

    private static void playsound() {
        SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();
        float pitch = 1.0F;
        float pitchIncrease = 0.5F;
        float volume = 1.0F;
        int delay = 2;

        soundManager.play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), pitch, volume));
        soundManager.play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), pitch + pitchIncrease, volume), delay);
        soundManager.play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), pitch + pitchIncrease * 2, volume), delay * 2);
    }
}
