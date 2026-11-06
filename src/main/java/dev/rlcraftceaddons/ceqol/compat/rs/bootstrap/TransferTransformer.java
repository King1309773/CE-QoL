package dev.rlcraftceaddons.ceqol.compat.rs.bootstrap;

import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class TransferTransformer implements IClassTransformer {
    private static final String PACKAGE = "com/raoulvdberge/refinedstorage/apiimpl/network/grid/";
    private static final String TARGET = (PACKAGE + "CraftingGridBehavior").replace('/', '.');
    private static final String HELPER = "dev/rlcraftceaddons/ceqol/compat/rs/QualityRecipeTransfer";
    private static final String DESCRIPTOR = "(Lcom/raoulvdberge/refinedstorage/api/network/grid/IGridNetworkAware;"
            + "Lnet/minecraft/entity/player/EntityPlayer;[[Lnet/minecraft/item/ItemStack;)V";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null || !TARGET.equals(transformedName)) return bytes;
        try {
            return addFallback(bytes);
        } catch (IllegalStateException e) {
            LogManager.getLogger("RS Quality Transfer").warn("Skipped incompatible RS transfer method: {}", e.getMessage());
            return bytes;
        }
    }

    public static byte[] addFallback(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        MethodNode transfer = node.methods.stream()
                .filter(method -> method.name.equals("onRecipeTransfer") && method.desc.equals(DESCRIPTOR))
                .findFirst().orElseThrow(() -> new IllegalStateException("Transfer method not found"));

        AbstractInsnNode lastReturn = null;
        int returns = 0;
        for (AbstractInsnNode instruction : transfer.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (HELPER.equals(call.owner) && "fillMissing".equals(call.name)) return bytes;
            }
            if (instruction.getOpcode() == Opcodes.RETURN) {
                returns++;
                lastReturn = instruction;
            }
        }
        // The earlier return is the permission/network guard. Leave it intact.
        if (returns != 2) throw new IllegalStateException("Unexpected transfer control flow");

        InsnList hook = new InsnList();
        hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
        hook.add(new VarInsnNode(Opcodes.ALOAD, 2));
        hook.add(new VarInsnNode(Opcodes.ALOAD, 3));
        hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, "fillMissing", DESCRIPTOR, false));
        transfer.instructions.insertBefore(lastReturn, hook);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
