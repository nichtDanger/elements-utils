package dev.eposs.elementsutils.feature.luckydrop;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class LuckydropSound {
    public static boolean onGameMessage(Text text, boolean b) {
        if (text.contains(Text.literal("Loot:"))) playsound();
        // TODO: Add all other loot text options

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
