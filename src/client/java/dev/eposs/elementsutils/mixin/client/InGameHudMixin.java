package dev.eposs.elementsutils.mixin.client;

import dev.eposs.elementsutils.feature.pet.PetDisplay;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow
    private Text overlayMessage;
    @Shadow
    private int overlayRemaining;

    @Inject(at = @At("HEAD"), method = "renderOverlayMessage")
    private void renderOverlayMessage(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (this.overlayMessage != null && this.overlayRemaining > 0) {
            PetDisplay.updateCurrentPetXP(this.overlayMessage);
        }
    }
}
