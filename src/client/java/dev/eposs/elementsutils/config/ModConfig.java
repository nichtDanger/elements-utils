package dev.eposs.elementsutils.config;

import dev.eposs.elementsutils.ElementsUtils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = ElementsUtils.MOD_ID)
public class ModConfig implements ConfigData {
    public static ModConfig getConfig() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }


    @ConfigEntry.Gui.TransitiveObject
    public BaseDisplayConfig baseDisplay = new BaseDisplayConfig();

    @ConfigEntry.Gui.TransitiveObject
    public MoonPhaseDisplayConfig moonPhaseDisplay = new MoonPhaseDisplayConfig();
    
    @ConfigEntry.Gui.TransitiveObject
    public TimeDisplayConfig timeDisplay = new TimeDisplayConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public DevUtilsConfig devUtils = new DevUtilsConfig();


    public static class BaseDisplayConfig {
        public boolean show = false;
        // public List<String> playerNames = List.of();
    }

    public static class MoonPhaseDisplayConfig {
        public boolean show = true;
        public Position position = Position.BOTTOM_RIGHT;

    }
    
    public static class TimeDisplayConfig {
        public boolean show = true;
        public Position position = Position.TOP_RIGHT;
    }
    
    public static class DevUtilsConfig {
        public boolean enable = false;
    }

    public enum Position {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }
}
