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
import org.lwjgl.glfw.GLFW;

/**
 * HoldClick — holds the attack (left-click / destroy block) key down
 * continuously while active. Toggle on/off with a keybind (default: K).
 *
 * Built for Minecraft 1.16.5 + Fabric.
 *
 * Use case: standing still at a fixed spot mining a regenerating gen block —
 * no scanning, no movement, just a held mouse button.
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
        if (client.player == null) {
            if (holding) setAttack(client, false); // don't leave the key stuck
            return;
        }

        while (toggleKey.wasPressed()) {
            holding = !holding;
            String msg = holding ? "\u00a7a[HoldClick] \u00a7fHolding attack..."
                                 : "\u00a7c[HoldClick] \u00a7fStopped.";
            client.player.sendMessage(new LiteralText(msg), true);
            if (!holding) setAttack(client, false);
        }

        if (holding) setAttack(client, true);
    }

    /** Presses/releases whatever key is currently bound to "Attack/Destroy". */
    private void setAttack(MinecraftClient client, boolean pressed) {
        KeyBinding key = client.options.keyAttack;
        KeyBinding.setKeyPressed(
            InputUtil.fromTranslationKey(key.getBoundKeyTranslationKey()), pressed);
        key.setPressed(pressed);
    }

    private void renderHud(MatrixStack matrices, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        String status = holding ? "\u00a7a\u25cf HOLDING" : "\u00a77\u25cb idle";
        client.textRenderer.drawWithShadow(matrices,
            "\u00a7fHoldClick \u00a78| " + status + " \u00a78| \u00a7eK\u00a7f=toggle",
            10, 10, 0xFFFFFF);
    }
}
