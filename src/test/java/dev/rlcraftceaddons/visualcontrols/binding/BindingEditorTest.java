package dev.rlcraftceaddons.visualcontrols.binding;

import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyBindingMap;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.junit.Test;

import static org.junit.Assert.*;

public class BindingEditorTest {
    private static final AtomicInteger IDS = new AtomicInteger();

    @Test
    public void bindingUpdatesModifierAndRebuildsGlobalMappingBeforeSaving() {
        KeyBinding binding = key(19, KeyModifier.NONE);
        AtomicInteger saves = new AtomicInteger();
        BindingEditor editor = new BindingEditor(new KeyBinding[] {binding}, () -> {
            assertEquals(KeyModifier.CONTROL, binding.getKeyModifier());
            assertFalse(new KeyBindingMap().lookupAll(19).contains(binding));
            assertEquals(1, new KeyBindingMap().lookupAll(20).stream().filter(value -> value == binding).count());
            saves.incrementAndGet();
        });
        editor.bind(binding, 20, KeyModifier.CONTROL);
        assertEquals(20, binding.getKeyCode());
        assertEquals(KeyModifier.CONTROL, binding.getKeyModifier());
        assertEquals(1, saves.get());
        editor.bind(binding, 20, KeyModifier.CONTROL);
        assertEquals(1, saves.get());
    }

    @Test
    public void clearRemovesModifierAndSavesOnlyForAChange() {
        KeyBinding binding = key(19, KeyModifier.CONTROL);
        AtomicInteger saves = new AtomicInteger();
        BindingEditor editor = new BindingEditor(new KeyBinding[] {binding}, saves::incrementAndGet);
        editor.clear(binding);
        assertEquals(0, binding.getKeyCode());
        assertEquals(KeyModifier.NONE, binding.getKeyModifier());
        assertEquals(1, saves.get());
        editor.clear(binding);
        assertEquals(1, saves.get());
    }

    @Test
    public void resetRestoresTheDefaultCodeAndDefaultModifier() {
        KeyBinding binding = key(19, KeyModifier.CONTROL);
        binding.setKeyModifierAndCode(KeyModifier.SHIFT, -99);
        AtomicInteger saves = new AtomicInteger();
        BindingEditor editor = new BindingEditor(new KeyBinding[] {binding}, saves::incrementAndGet);
        editor.reset(binding);
        assertEquals(19, binding.getKeyCode());
        assertEquals(KeyModifier.CONTROL, binding.getKeyModifier());
        assertEquals(1, saves.get());
        editor.reset(binding);
        assertEquals(1, saves.get());
    }

    @Test
    public void resetAllSavesOnceAcrossAllRegisteredBindings() {
        KeyBinding first = key(19, KeyModifier.CONTROL);
        KeyBinding second = key(20, KeyModifier.SHIFT);
        KeyBinding unchanged = key(-100, KeyModifier.NONE);
        first.setKeyModifierAndCode(KeyModifier.NONE, 21);
        second.setKeyModifierAndCode(KeyModifier.ALT, 22);
        AtomicInteger saves = new AtomicInteger();
        BindingEditor editor = new BindingEditor(new KeyBinding[] {first, second, unchanged, first, null}, saves::incrementAndGet);
        editor.resetAll();
        assertEquals(19, first.getKeyCode());
        assertEquals(KeyModifier.CONTROL, first.getKeyModifier());
        assertEquals(20, second.getKeyCode());
        assertEquals(KeyModifier.SHIFT, second.getKeyModifier());
        assertEquals(-100, unchanged.getKeyCode());
        assertEquals(1, saves.get());
        editor.resetAll();
        assertEquals(1, saves.get());
    }

    @Test
    public void acceptsExtendedMouseButtonsAndTheEntireLwjglKeyRange() {
        KeyBinding binding = key(19, KeyModifier.NONE);
        AtomicInteger saves = new AtomicInteger();
        BindingEditor editor = new BindingEditor(new KeyBinding[] {binding}, saves::incrementAndGet);
        for (int code : new int[] {-100, -85, -1, 255}) {
            editor.bind(binding, code, KeyModifier.NONE);
            assertEquals(code, binding.getKeyCode());
        }
        assertEquals(4, saves.get());
    }

    @Test
    public void rejectsInvalidCodesWithoutMutationOrSave() {
        KeyBinding binding = key(19, KeyModifier.NONE);
        AtomicInteger saves = new AtomicInteger();
        BindingEditor editor = new BindingEditor(new KeyBinding[] {binding}, saves::incrementAndGet);
        for (int code : new int[] {-101, 256, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
            try {
                editor.bind(binding, code, KeyModifier.NONE);
                fail("Expected invalid key code to be rejected: " + code);
            } catch (IllegalArgumentException expected) { }
        }
        assertEquals(19, binding.getKeyCode());
        assertEquals(0, saves.get());
    }

    @Test
    public void rejectsBindingsOutsideTheRuntimeArray() {
        KeyBinding registered = key(19, KeyModifier.NONE);
        KeyBinding foreign = key(20, KeyModifier.NONE);
        AtomicInteger saves = new AtomicInteger();
        BindingEditor editor = new BindingEditor(new KeyBinding[] {registered}, saves::incrementAndGet);
        try {
            editor.bind(foreign, 21, KeyModifier.SHIFT);
            fail("Expected foreign binding to be rejected");
        } catch (IllegalArgumentException expected) { }
        try {
            editor.clear(foreign);
            fail("Expected foreign binding to be rejected");
        } catch (IllegalArgumentException expected) { }
        try {
            editor.reset(foreign);
            fail("Expected foreign binding to be rejected");
        } catch (IllegalArgumentException expected) { }
        assertEquals(20, foreign.getKeyCode());
        assertEquals(0, saves.get());
    }

    @Test
    public void normalizesModifierKeysUsingForgeBeforeCheckingForChanges() {
        KeyBinding binding = key(29, KeyModifier.NONE);
        AtomicInteger saves = new AtomicInteger();
        BindingEditor editor = new BindingEditor(new KeyBinding[] {binding}, saves::incrementAndGet);
        editor.bind(binding, 29, KeyModifier.CONTROL);
        assertEquals(KeyModifier.NONE, binding.getKeyModifier());
        assertEquals(0, saves.get());
        editor.bind(binding, 0, KeyModifier.CONTROL);
        assertEquals(0, binding.getKeyCode());
        assertEquals(KeyModifier.NONE, binding.getKeyModifier());
        assertEquals(1, saves.get());
    }

    private static KeyBinding key(int code, KeyModifier modifier) {
        return new KeyBinding("test.editor." + IDS.incrementAndGet(), KeyConflictContext.IN_GAME, modifier, code, "test.category");
    }
}
