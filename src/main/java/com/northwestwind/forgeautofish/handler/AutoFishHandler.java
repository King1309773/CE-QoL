package com.northwestwind.forgeautofish.handler;

import com.northwestwind.forgeautofish.keybind.KeyBinds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Client-only fishing actions driven by real bite sounds and acknowledged hook changes. */
public class AutoFishHandler {
    private static final int WAITING_FOR_BITE = 0;
    private static final int WAITING_FOR_REMOVAL = 1;
    private static final int WAITING_FOR_NEW_HOOK = 2;
    private static final int RECAST_DELAY_TICKS = 10;
    private static final int ACK_TIMEOUT_TICKS = 60;
    private static final double BITE_SOUND_RADIUS_SQUARED = 1.0;

    private boolean autofishenabled;
    private EntityPlayerSP sessionPlayer;
    private WorldClient sessionWorld;
    private NetHandlerPlayClient sessionConnection;
    private EntityFishHook pendingBite;
    private EntityFishHook processedHook;
    private int stage = WAITING_FOR_BITE;
    private int elapsedTicks;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public synchronized void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!bindSession(mc) || KeyBinds.autofish == null) return;
        if (KeyBinds.autofish.isPressed()) {
            autofishenabled = !autofishenabled;
            clearCycle();
            sessionPlayer.sendStatusMessage(new TextComponentTranslation(
                "toggle.forgeautofish", autofishenabled ? "\u00a7aEnabled" : "\u00a7cDisabled"), true);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public synchronized void onSound(PlaySoundEvent event) {
        if (!autofishenabled) return;
        ISound sound = event.getSound();
        if (sound == null || sound.getSoundLocation() == null
            || !"minecraft:entity.bobber.splash".equals(sound.getSoundLocation().toString())) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (!bindSession(mc) || !holdingRod()) return;
        EntityFishHook hook = sessionPlayer.fishEntity;
        if (!validHook(hook) || hook == processedHook) return;
        double dx = sound.getXPosF() - hook.posX;
        double dy = sound.getYPosF() - hook.posY;
        double dz = sound.getZPosF() - hook.posZ;
        if (dx * dx + dy * dy + dz * dz <= BITE_SOUND_RADIUS_SQUARED) {
            // Spawn and bite packets may be handled before the next player tick.
            // A different living hook already acknowledges the new cast; do not
            // drop its first real bite merely because that tick has not run yet.
            if (stage != WAITING_FOR_BITE) cancelPending();
            pendingBite = hook;
        }
        // Observe the event only. Muting/replacing the sound is left to other mods.
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public synchronized void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // A physical client also contains the single-player logical server.
        if (event.side != Side.CLIENT || event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null) {
            clearSession();
            return;
        }
        if (event.player != mc.player || !bindSession(mc)) return;
        if (!autofishenabled || !holdingRod()) {
            clearCycle();
            return;
        }

        EntityFishHook hook = sessionPlayer.fishEntity;
        if (stage == WAITING_FOR_REMOVAL) {
            elapsedTicks++;
            if (hook != null && hook != processedHook) {
                // A manual cast has already supplied a different hook.
                cancelPending();
            } else if (hook == null && elapsedTicks >= RECAST_DELAY_TICKS) {
                stage = WAITING_FOR_NEW_HOOK;
                elapsedTicks = 0;
                useRod();
            } else if (elapsedTicks >= ACK_TIMEOUT_TICKS) {
                // Never retry a reel or recast against an unacknowledged old hook.
                cancelPending();
            }
            return;
        }
        if (stage == WAITING_FOR_NEW_HOOK) {
            elapsedTicks++;
            if (hook != null || elapsedTicks >= ACK_TIMEOUT_TICKS) cancelPending();
            return;
        }

        if (!validHook(hook) || pendingBite != hook || processedHook == hook) {
            // A sound belonging to a removed/replaced hook cannot act on a new one.
            pendingBite = null;
            return;
        }
        processedHook = hook;
        pendingBite = null;
        stage = WAITING_FOR_REMOVAL;
        elapsedTicks = 0;
        useRod();
    }

    private boolean bindSession(Minecraft mc) {
        EntityPlayerSP player = mc.player;
        WorldClient world = mc.world;
        NetHandlerPlayClient connection = mc.getConnection();
        if (player == null || world == null || connection == null || player.isDead) {
            clearSession();
            return false;
        }
        if (player != sessionPlayer || world != sessionWorld || connection != sessionConnection) {
            clearCycle();
            sessionPlayer = player;
            sessionWorld = world;
            sessionConnection = connection;
        }
        return true;
    }

    private boolean holdingRod() {
        ItemStack held = sessionPlayer.getHeldItemMainhand();
        return !held.isEmpty() && held.getItem() instanceof ItemFishingRod;
    }

    private boolean validHook(EntityFishHook hook) {
        return hook != null && !hook.isDead && hook.getAngler() == sessionPlayer;
    }

    private void useRod() {
        sessionConnection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
    }

    private void cancelPending() {
        pendingBite = null;
        elapsedTicks = 0;
        stage = WAITING_FOR_BITE;
        // Keep processedHook locked even if its removal acknowledgment times out.
    }

    private void clearCycle() {
        cancelPending();
        processedHook = null;
    }

    private void clearSession() {
        clearCycle();
        sessionPlayer = null;
        sessionWorld = null;
        sessionConnection = null;
    }
}
