package com.northwestwind.forgeautofish.keybind;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class KeyBinds {
    public static KeyBinding autofish;

    private KeyBinds() { }

    public static synchronized void register() {
        if (autofish != null) return;
        autofish = new KeyBinding(new TextComponentTranslation("key.forgeautofish.autofish").getFormattedText(),
                43, "key.categories.forgeautofish");
        ClientRegistry.registerKeyBinding(autofish);
    }
}
