package dev.eposs.elementsutils.feature.luckydrop;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class LuckydropSound {
    public static boolean onGameMessage(Text text, boolean b) {
        if (text.contains(Text.literal("Loot:"))) playsound();
        // TODO: Add all other loot text options
        
        return true;
    }

    private static void playsound() {
        SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();
        soundManager.play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_CHIME, 1.0f));
        soundManager.play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_CHIME, 1.5f), 2);
        soundManager.play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_CHIME, 2.0f), 4);
    }
}
