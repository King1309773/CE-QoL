package dev.rlcraftceaddons.visualcontrols.binding;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyModifier;

/** Applies edits only to controls in the supplied runtime registry. */
public final class BindingEditor {
    private final KeyBinding[] runtimeBindings;
    private final Runnable save;

    public BindingEditor(KeyBinding[] bindings, Runnable save) {
        this.runtimeBindings = Objects.requireNonNull(bindings, "bindings");
        this.save = Objects.requireNonNull(save, "save");
    }

    public void bind(KeyBinding binding, int keyCode, KeyModifier modifier) {
        requireRegistered(binding);
        if (change(binding, keyCode, modifier)) {
            finishEdit();
        }
    }

    public void clear(KeyBinding binding) {
        bind(binding, 0, KeyModifier.NONE);
    }

    public void reset(KeyBinding binding) {
        requireRegistered(binding);
        bind(binding, binding.getKeyCodeDefault(), binding.getKeyModifierDefault());
    }

    public void resetAll() {
        boolean changed = false;
        Set<KeyBinding> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (KeyBinding binding : runtimeBindings) {
            if (binding != null && seen.add(binding)) {
                changed |= change(binding, binding.getKeyCodeDefault(), binding.getKeyModifierDefault());
            }
        }
        if (changed) {
            finishEdit();
        }
    }

    private static boolean change(KeyBinding binding, int keyCode, KeyModifier modifier) {
        if (keyCode < -100 || keyCode > 255) {
            throw new IllegalArgumentException("Unsupported key code: " + keyCode);
        }
        Objects.requireNonNull(modifier, "modifier");
        KeyModifier effectiveModifier = keyCode == 0 || modifier.matches(keyCode) ? KeyModifier.NONE : modifier;
        if (binding.getKeyCode() == keyCode && binding.getKeyModifier() == effectiveModifier) {
            return false;
        }
        binding.setKeyModifierAndCode(effectiveModifier, keyCode);
        return true;
    }

    private void finishEdit() {
        KeyBinding.resetKeyBindingArrayAndHash();
        save.run();
    }

    private void requireRegistered(KeyBinding binding) {
        if (binding != null) {
            for (KeyBinding registered : runtimeBindings) {
                if (registered == binding) {
                    return;
                }
            }
        }
        throw new IllegalArgumentException("Binding is not in the runtime controls registry");
    }
}
