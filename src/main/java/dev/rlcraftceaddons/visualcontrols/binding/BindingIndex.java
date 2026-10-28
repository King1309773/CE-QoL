package dev.rlcraftceaddons.visualcontrols.binding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.settings.KeyBinding;

/** A screen-independent view of the complete runtime controls registry. */
public final class BindingIndex {
    public enum State { FREE, BOUND, SHARED, CONFLICT }
    public enum Filter { ALL, CONFLICTS, UNBOUND }

    private final KeyBinding[] runtimeBindings;
    private final Function<String, String> translate;
    private List<KeyBinding> all = Collections.emptyList();
    private final Map<Integer, List<KeyBinding>> byKey = new HashMap<>();
    private final Set<KeyBinding> conflicts = Collections.newSetFromMap(new IdentityHashMap<>());

    public BindingIndex(KeyBinding[] bindings, Function<String, String> translate) {
        this.runtimeBindings = Objects.requireNonNull(bindings, "bindings");
        this.translate = Objects.requireNonNull(translate, "translate");
        refresh();
    }

    public List<KeyBinding> all() {
        return all;
    }

    public List<KeyBinding> find(String query, Filter filter, Integer selectedKey) {
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<KeyBinding> result = new ArrayList<>();
        for (KeyBinding binding : all) {
            if (selectedKey != null && binding.getKeyCode() != selectedKey.intValue()) {
                continue;
            }
            if (filter == Filter.CONFLICTS && !isConflict(binding)) {
                continue;
            }
            if (filter == Filter.UNBOUND && binding.getKeyCode() != 0) {
                continue;
            }
            if (needle.isEmpty() || contains(name(binding), needle) || contains(category(binding), needle)
                    || contains(binding.getKeyDescription(), needle) || contains(binding.getKeyCategory(), needle)) {
                result.add(binding);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public List<KeyBinding> onKey(int keyCode) {
        List<KeyBinding> bindings = byKey.get(keyCode);
        return bindings == null ? Collections.emptyList() : bindings;
    }

    public State state(int keyCode) {
        List<KeyBinding> bindings = onKey(keyCode);
        if (bindings.isEmpty()) {
            return State.FREE;
        }
        for (KeyBinding binding : bindings) {
            if (isConflict(binding)) {
                return State.CONFLICT;
            }
        }
        return bindings.size() > 1 ? State.SHARED : State.BOUND;
    }

    public boolean isConflict(KeyBinding binding) {
        return conflicts.contains(binding);
    }

    public String name(KeyBinding binding) {
        return translated(binding.getKeyDescription());
    }

    public String category(KeyBinding binding) {
        return translated(binding.getKeyCategory());
    }

    /** Recompute before displaying controls after an edit or a registry change. */
    public void refresh() {
        List<KeyBinding> current = new ArrayList<>();
        Set<KeyBinding> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        byKey.clear();
        conflicts.clear();
        for (KeyBinding binding : runtimeBindings) {
            if (binding != null && seen.add(binding)) {
                current.add(binding);
                if (binding.getKeyCode() != 0) {
                    byKey.computeIfAbsent(binding.getKeyCode(), ignored -> new ArrayList<>()).add(binding);
                }
            }
        }
        all = Collections.unmodifiableList(current);
        for (Map.Entry<Integer, List<KeyBinding>> entry : byKey.entrySet()) {
            entry.setValue(Collections.unmodifiableList(entry.getValue()));
        }
        // Modifier-vs-key conflicts can involve different physical keys, so the
        // comparison must cover all registered nonzero bindings, before filtering.
        for (int i = 0; i < current.size(); i++) {
            KeyBinding first = current.get(i);
            if (first.getKeyCode() == 0) {
                continue;
            }
            for (int j = i + 1; j < current.size(); j++) {
                KeyBinding second = current.get(j);
                if (second.getKeyCode() != 0 && reportedConflict(first, second)) {
                    conflicts.add(first);
                    conflicts.add(second);
                }
            }
        }
    }

    private static boolean reportedConflict(KeyBinding first, KeyBinding second) {
        return first.conflicts(second) || second.conflicts(first)
                || first.hasKeyCodeModifierConflict(second) || second.hasKeyCodeModifierConflict(first);
    }

    private String translated(String raw) {
        String value = translate.apply(raw);
        return value == null ? raw : value;
    }

    private static boolean contains(String value, String needle) {
        return value.toLowerCase(Locale.ROOT).contains(needle);
    }
}
