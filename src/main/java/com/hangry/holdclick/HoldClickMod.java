package com.hangry.holdclick;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

/**
 * HoldClick — continuously mines the block at your crosshair while active.
 * Toggle on/off with a keybind (default: K).
 *
 * Built for Minecraft 1.16.5 + Fabric.
 *
 * Unlike a held mouse button, this calls the attack action directly in the
 * client tick loop, so it keeps mining even when the window is NOT focused
 * (tabbed out). Point at a fixed gen block, press K, tab away.
 */
public class HoldClickMod implements ClientModInitializer {

    public static boolean holding = false;

    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.holdclick.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "HoldClick"));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        HudRenderCallback.EVENT.register(this::renderHud);
    }

    private void onTick(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;

        while (toggleKey.wasPressed()) {
            holding = !holding;
            String msg = holding ? "\u00a7a[HoldClick] \u00a7fMining (works tabbed out)..."
                                 : "\u00a7c[HoldClick] \u00a7fStopped.";
            client.player.sendMessage(new LiteralText(msg), true);
        }

        if (!holding) return;

        // Directly attack the block at the crosshair. This runs in the tick
        // loop and does NOT depend on window focus, so it keeps mining while
        // tabbed out — unlike simulating a held mouse button.
        HitResult target = client.crosshairTarget;
        if (target != null && target.getType() == HitResult.Type.BLOCK) {
            BlockHitResult bhr = (BlockHitResult) target;
            client.interactionManager.updateBlockBreakingProgress(bhr.getBlockPos(), bhr.getSide());
            client.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private void renderHud(MatrixStack matrices, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        String status = holding ? "\u00a7a\u25cf MINING" : "\u00a77\u25cb idle";
        client.textRenderer.drawWithShadow(matrices,
            "\u00a7fHoldClick \u00a78| " + status + " \u00a78| \u00a7eK\u00a7f=toggle",
            10, 10, 0xFFFFFF);
    }
}
