package dev.rlcraftceaddons.ceqol.compat.drops;

import java.lang.ref.WeakReference;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.JEIPlugin;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@JEIPlugin
public final class ScriptedDropPlugin implements IModPlugin {
    private static final Logger LOG = LogManager.getLogger("CE QoL Scripted Drops");
    private WeakReference<IRecipeRegistry> populated = new WeakReference<>(null);

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        if (!Loader.isModLoaded("crafttweaker") || !Loader.isModLoaded("jeresources")) return;
        IRecipeRegistry registry = runtime.getRecipeRegistry();
        if (populated.get() == registry) return;
        populated = new WeakReference<>(registry);
        try {
            CraftTweakerDrops.addTo(registry);
        } catch (RuntimeException | LinkageError failure) {
            LOG.warn("Could not read CraftTweaker's scripted mob drops", failure);
        }
    }
}
