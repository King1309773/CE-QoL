package dev.rlcraftceaddons.visualcontrols.binding;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.junit.Test;

import static dev.rlcraftceaddons.visualcontrols.binding.BindingIndex.Filter.*;
import static dev.rlcraftceaddons.visualcontrols.binding.BindingIndex.State.*;
import static org.junit.Assert.*;

public class BindingIndexTest {
    @Test public void repeatedSearchesReuseTranslatedNames() {
        AtomicInteger translations = new AtomicInteger();
        KeyBinding binding = key(R, KeyConflictContext.IN_GAME, KeyModifier.NONE);
        BindingIndex index = new BindingIndex(new KeyBinding[]{binding}, text -> {
            translations.incrementAndGet(); return text;
        });
        index.find("missing", ALL, null);
        int afterFirst = translations.get();
        for (int i=0;i<10;i++) index.find("missing", ALL, null);
        assertEquals("Typing must reuse the translated search index", afterFirst, translations.get());
    }
    private static final AtomicInteger IDS = new AtomicInteger();
    private static final int R = 19;
    private static final int CTRL = 29;

    @Test
    public void indexesTheCompleteRuntimeArrayIncludingUnboundAndMouseBindings() {
        KeyBinding mouse = key(-100, KeyConflictContext.IN_GAME, KeyModifier.NONE);
        KeyBinding unbound = key(0, KeyConflictContext.IN_GAME, KeyModifier.NONE);
        KeyBinding[] runtime = {mouse, unbound};
        BindingIndex index = index(runtime);
        assertEquals(Arrays.asList(mouse, unbound), index.all());
        assertEquals(Collections.singletonList(mouse), index.onKey(-100));
        assertEquals(BOUND, index.state(-100));
        assertEquals(FREE, index.state(255));
        assertEquals(Collections.singletonList(unbound), index.find("", UNBOUND, null));
    }

    @Test
    public void independentGameAndGuiContextsShareAKeySafely() {
        KeyBinding game = key(R, KeyConflictContext.IN_GAME, KeyModifier.NONE);
        KeyBinding gui = key(R, KeyConflictContext.GUI, KeyModifier.NONE);
        BindingIndex index = index(game, gui);
        assertEquals(SHARED, index.state(R));
        assertFalse(index.isConflict(game));
        assertFalse(index.isConflict(gui));
    }

    @Test
    public void sameContextSameKeyIsAConflict() {
        KeyBinding first = key(R, KeyConflictContext.IN_GAME, KeyModifier.NONE);
        KeyBinding second = key(R, KeyConflictContext.IN_GAME, KeyModifier.NONE);
        BindingIndex index = index(first, second);
        assertEquals(CONFLICT, index.state(R));
        assertTrue(index.isConflict(first));
        assertTrue(index.isConflict(second));
    }

    @Test
    public void detectsPotentialConflictsWithoutReadingWhetherAContextIsActive() {
        IKeyConflictContext inactive = new IKeyConflictContext() {
            @Override public boolean isActive() { throw new AssertionError("Conflict analysis must not poll the current screen"); }
            @Override public boolean conflicts(IKeyConflictContext other) { return other == this; }
        };
        BindingIndex index = index(key(R, inactive, KeyModifier.NONE), key(R, inactive, KeyModifier.NONE));
        assertEquals(CONFLICT, index.state(R));
    }

    @Test
    public void twoGuiBindingsRemainPotentialConflicts() {
        BindingIndex index = index(key(R, KeyConflictContext.GUI, KeyModifier.NONE), key(R, KeyConflictContext.GUI, KeyModifier.NONE));
        assertEquals(CONFLICT, index.state(R));
    }

    @Test
    public void usesForgeRulesForModifiedAndBareLettersInGame() {
        KeyBinding modified = key(R, KeyConflictContext.IN_GAME, KeyModifier.CONTROL);
        KeyBinding bare = key(R, KeyConflictContext.IN_GAME, KeyModifier.NONE);
        assertTrue(modified.conflicts(bare));
        assertEquals(CONFLICT, index(modified, bare).state(R));
    }

    @Test
    public void differentGuiModifiersMayShareTheSameLetter() {
        KeyBinding modified = key(R, KeyConflictContext.GUI, KeyModifier.CONTROL);
        KeyBinding bare = key(R, KeyConflictContext.GUI, KeyModifier.NONE);
        assertFalse(modified.conflicts(bare));
        assertEquals(SHARED, index(modified, bare).state(R));
    }

    @Test
    public void modifierKeyConflictsAreDetectedAcrossDifferentKeyCodes() {
        KeyBinding modified = key(R, KeyConflictContext.IN_GAME, KeyModifier.CONTROL);
        KeyBinding control = key(CTRL, KeyConflictContext.IN_GAME, KeyModifier.NONE);
        BindingIndex index = index(modified, control);
        assertTrue(modified.hasKeyCodeModifierConflict(control));
        assertEquals(CONFLICT, index.state(R));
        assertEquals(CONFLICT, index.state(CTRL));
        assertTrue(index.isConflict(modified));
        assertTrue(index.isConflict(control));
    }

    @Test
    public void aReportedModifierConflictIsEnoughEvenWhenConflictsReturnsFalse() {
        KeyBinding first = new KeyBinding("test.override." + IDS.incrementAndGet(), R, "test.category") {
            @Override public boolean conflicts(KeyBinding other) { return false; }
            @Override public boolean hasKeyCodeModifierConflict(KeyBinding other) { return true; }
        };
        KeyBinding second = key(20, KeyConflictContext.GUI, KeyModifier.NONE);
        assertEquals(CONFLICT, index(first, second).state(R));
    }

    @Test
    public void zeroIsNeitherBoundNorConflictingEvenWithModifiers() {
        KeyBinding first = key(0, KeyConflictContext.UNIVERSAL, KeyModifier.CONTROL);
        KeyBinding second = key(0, KeyConflictContext.UNIVERSAL, KeyModifier.CONTROL);
        KeyBinding control = key(CTRL, KeyConflictContext.UNIVERSAL, KeyModifier.NONE);
        BindingIndex index = index(first, second, control);
        assertEquals(FREE, index.state(0));
        assertTrue(index.onKey(0).isEmpty());
        assertFalse(index.isConflict(first));
        assertFalse(index.isConflict(second));
        assertEquals(BOUND, index.state(CTRL));
    }

    @Test
    public void searchMatchesTranslatedNamesAndCategoriesAndRawTranslationKeys() {
        KeyBinding binding = new KeyBinding("key.mod.OPEN_INVENTORY", R, "category.mod.装备");
        Map<String, String> translations = new HashMap<>();
        translations.put(binding.getKeyDescription(), "打开装备袋");
        translations.put(binding.getKeyCategory(), "探险工具");
        BindingIndex index = new BindingIndex(new KeyBinding[] {binding}, key -> translations.getOrDefault(key, key));
        assertEquals("打开装备袋", index.name(binding));
        assertEquals("探险工具", index.category(binding));
        for (String query : Arrays.asList("装备袋", "探险", "open_inventory", "CATEGORY.MOD", "   OPEN_INVENTORY   ")) {
            assertEquals(query, Collections.singletonList(binding), index.find(query, ALL, null));
        }
        assertTrue(index.find("missing", ALL, null).isEmpty());
    }

    @Test
    public void searchCaseFoldingDoesNotDependOnTheSystemLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            KeyBinding binding = new KeyBinding("key.INVENTORY", R, "category.TEST");
            assertEquals(Collections.singletonList(binding), index(binding).find("inventory", ALL, null));
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    public void filteringNeverHidesAConflictWithABindingOutsideTheResult() {
        KeyBinding visible = new KeyBinding("key.visible", R, "category.test");
        KeyBinding hidden = new KeyBinding("key.hidden", R, "category.test");
        KeyBinding unbound = key(0, KeyConflictContext.IN_GAME, KeyModifier.NONE);
        BindingIndex index = index(visible, hidden, unbound);
        List<KeyBinding> result = index.find("visible", CONFLICTS, R);
        assertEquals(Collections.singletonList(visible), result);
        assertTrue(index.isConflict(visible));
        assertEquals(CONFLICT, index.state(R));
        assertTrue(index.find("visible", ALL, 20).isEmpty());
        assertEquals(Collections.singletonList(unbound), index.find(null, UNBOUND, null));
    }

    @Test
    public void refreshRebuildsFromCurrentValuesAndRuntimeArrayEntries() {
        KeyBinding first = key(R, KeyConflictContext.IN_GAME, KeyModifier.NONE);
        KeyBinding second = key(R, KeyConflictContext.IN_GAME, KeyModifier.NONE);
        KeyBinding[] runtime = {first, second};
        BindingIndex index = index(runtime);
        second.setKeyModifierAndCode(KeyModifier.NONE, -85);
        runtime[0] = key(21, KeyConflictContext.IN_GAME, KeyModifier.NONE);
        index.refresh();
        assertEquals(Arrays.asList(runtime[0], second), index.all());
        assertEquals(FREE, index.state(R));
        assertEquals(BOUND, index.state(-85));
        assertEquals(BOUND, index.state(21));
    }

    @Test
    public void duplicateRuntimeReferencesDoNotConflictWithThemselves() {
        KeyBinding binding = key(R, KeyConflictContext.IN_GAME, KeyModifier.NONE);
        BindingIndex index = index(binding, null, binding);
        assertEquals(Collections.singletonList(binding), index.all());
        assertEquals(BOUND, index.state(R));
    }

    private static BindingIndex index(KeyBinding... bindings) {
        return new BindingIndex(bindings, value -> value);
    }

    private static KeyBinding key(int code, IKeyConflictContext context, KeyModifier modifier) {
        return new KeyBinding("test.index." + IDS.incrementAndGet(), context, modifier, code, "test.category");
    }
}
