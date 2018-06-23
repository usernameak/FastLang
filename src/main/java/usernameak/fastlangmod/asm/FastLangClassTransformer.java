package usernameak.fastlangmod.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiLanguage;
import net.minecraft.launchwrapper.IClassTransformer;

public class FastLangClassTransformer implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!transformedName.equals("net.minecraft.client.gui.GuiLanguage$List"))
			return basicClass;
		boolean isObfuscated = !name.equals(transformedName);
		System.out.println("FastLang: transforming " + transformedName);
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		final String ELEMENT_CLICKED = isObfuscated ? "a" : "elementClicked";
		final String ELEMENT_CLICKED_DESC = "(IZII)V";
		for (MethodNode method : classNode.methods) {
			if (method.name.equals(ELEMENT_CLICKED) && method.desc.equals(ELEMENT_CLICKED_DESC)) {
				System.out.println("FastLang: found elementClicked, injecting...");
				for (int i = 0; i < method.instructions.size(); i++) {
					AbstractInsnNode insn = method.instructions.get(i);
					if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
						MethodInsnNode minsn = (MethodInsnNode) insn;
						final String MINECRAFT = isObfuscated ? "bao" : "net/minecraft/client/Minecraft";
						if (minsn.owner.equals(MINECRAFT) && minsn.desc.equals("()V")) {
							method.instructions.insertBefore(insn, new InsnNode(Opcodes.POP));
							method.instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC,
									"usernameak/fastlangmod/asm/FastLangClassTransformer", "inject", "()V", false));
							method.instructions.remove(insn);
						}
					}
				}
			}
		}
		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(classWriter);
		return classWriter.toByteArray();
	}

	public static void inject() {
		Minecraft.getMinecraft().getLanguageManager()
				.onResourceManagerReload(Minecraft.getMinecraft().getResourceManager());
	}
}
