package dev.rlcraftceaddons.visualcontrols.client;

import net.minecraftforge.client.settings.KeyModifier;
import org.junit.Test;
import org.lwjgl.input.Keyboard;
import static org.junit.Assert.*;

public class CaptureSessionTest {
    @Test public void escapeCancelsWithoutAnAssignment() {
        CaptureSession capture = new CaptureSession(); capture.begin();
        assertEquals(CaptureSession.Kind.CANCEL, capture.keyDown(Keyboard.KEY_ESCAPE, KeyModifier.NONE).kind);
        assertFalse(capture.isRecording());
    }
    @Test public void physicalComboWaitsForTheMainKey() {
        CaptureSession capture = new CaptureSession(); capture.begin();
        assertEquals(CaptureSession.Kind.WAIT, capture.keyDown(Keyboard.KEY_LCONTROL, KeyModifier.CONTROL).kind);
        assertTrue(capture.isRecording());
        CaptureSession.Result assigned = capture.keyDown(Keyboard.KEY_R, KeyModifier.CONTROL);
        assertEquals(CaptureSession.Kind.APPLY, assigned.kind);
        assertEquals(Keyboard.KEY_R, assigned.keyCode);
        assertEquals(KeyModifier.CONTROL, assigned.modifier);
        assertFalse(capture.isRecording());
        assertEquals(CaptureSession.Kind.WAIT, capture.keyUp(Keyboard.KEY_LCONTROL).kind);
    }
    @Test public void modifierAloneBindsOnItsRelease() {
        CaptureSession capture = new CaptureSession(); capture.begin();
        capture.keyDown(Keyboard.KEY_RSHIFT, KeyModifier.SHIFT);
        CaptureSession.Result assigned = capture.keyUp(Keyboard.KEY_RSHIFT);
        assertEquals(CaptureSession.Kind.APPLY, assigned.kind);
        assertEquals(Keyboard.KEY_RSHIFT, assigned.keyCode);
        assertEquals(KeyModifier.NONE, assigned.modifier);
    }
    @Test public void mouseSideButtonKeepsForgeEncodingAndModifier() {
        CaptureSession capture = new CaptureSession(); capture.begin();
        CaptureSession.Result assigned = capture.mouseDown(4, KeyModifier.ALT);
        assertEquals(CaptureSession.Kind.APPLY, assigned.kind);
        assertEquals(-96, assigned.keyCode);
        assertEquals(KeyModifier.ALT, assigned.modifier);
    }
    @Test public void characterOnlyEventsCannotUnbindAnAction() {
        CaptureSession capture = new CaptureSession(); capture.begin();
        assertEquals(CaptureSession.Kind.WAIT, capture.keyDown(0, KeyModifier.NONE).kind);
        assertTrue(capture.isRecording());
    }
    @Test public void canceledModifierCannotLeakIntoTheNextRecording() {
        CaptureSession capture = new CaptureSession(); capture.begin();
        capture.keyDown(Keyboard.KEY_LCONTROL, KeyModifier.CONTROL); capture.cancel(); capture.begin();
        assertEquals(CaptureSession.Kind.WAIT, capture.keyUp(Keyboard.KEY_LCONTROL).kind);
        CaptureSession.Result assigned = capture.keyDown(Keyboard.KEY_W, KeyModifier.NONE);
        assertEquals(CaptureSession.Kind.APPLY, assigned.kind);
        assertEquals(KeyModifier.NONE, assigned.modifier);
    }
    @Test public void virtualModifierIsAnImmediateStandaloneBinding() {
        CaptureSession capture = new CaptureSession(); capture.begin();
        CaptureSession.Result assigned = capture.virtualKey(Keyboard.KEY_LCONTROL, KeyModifier.CONTROL);
        assertEquals(CaptureSession.Kind.APPLY, assigned.kind);
        assertEquals(KeyModifier.NONE, assigned.modifier);
    }
    @Test public void inactiveSessionIgnoresAllEvents() {
        CaptureSession capture = new CaptureSession();
        assertEquals(CaptureSession.Kind.WAIT, capture.keyDown(Keyboard.KEY_R, KeyModifier.NONE).kind);
        assertEquals(CaptureSession.Kind.WAIT, capture.mouseDown(0, KeyModifier.NONE).kind);
    }
}
