package dev.rlcraftceaddons.visualcontrols.client;

import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.input.Keyboard;

/** The input lifecycle is separate from drawing and from saving bindings. */
public final class CaptureSession {
    public enum Kind { WAIT, CANCEL, APPLY }
    public static final class Result {
        public final Kind kind;
        public final int keyCode;
        public final KeyModifier modifier;
        private Result(Kind kind, int keyCode, KeyModifier modifier) {
            this.kind = kind; this.keyCode = keyCode; this.modifier = modifier;
        }
    }
    private boolean recording;
    private int pendingModifier;
    public void begin() { recording = true; pendingModifier = 0; }
    public void cancel() { recording = false; pendingModifier = 0; }
    public boolean isRecording() { return recording; }
    public Result keyDown(int code, KeyModifier active) {
        if (!recording || code == 0) return result(Kind.WAIT, 0, KeyModifier.NONE);
        if (code == Keyboard.KEY_ESCAPE) {
            cancel(); return result(Kind.CANCEL, 0, KeyModifier.NONE);
        }
        if (KeyModifier.isKeyCodeModifier(code)) {
            pendingModifier = code; return result(Kind.WAIT, 0, KeyModifier.NONE);
        }
        return assign(code, active);
    }
    public Result keyUp(int code) {
        return recording && pendingModifier != 0 && code == pendingModifier
                ? assign(code, KeyModifier.NONE) : result(Kind.WAIT, 0, KeyModifier.NONE);
    }
    public Result mouseDown(int button, KeyModifier active) {
        if (!recording || button < 0 || button >= 16) return result(Kind.WAIT, 0, KeyModifier.NONE);
        return assign(button - 100, active);
    }
    public Result virtualKey(int code, KeyModifier active) {
        if (!recording) return result(Kind.WAIT, 0, KeyModifier.NONE);
        return assign(code, KeyModifier.isKeyCodeModifier(code) ? KeyModifier.NONE : active);
    }
    private Result assign(int code, KeyModifier modifier) {
        cancel(); return result(Kind.APPLY, code, modifier == null ? KeyModifier.NONE : modifier);
    }
    private static Result result(Kind kind, int code, KeyModifier modifier) { return new Result(kind, code, modifier); }
}
