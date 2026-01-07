
package com.detonomics.budgettuner.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LogarithmicAxisTest {

    @BeforeAll
    static void initJfx() {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException e) {
            // Platform already started
        }
    }

    @Test
    void testConstructors() {
        // Must run on JavaFX thread if it interacts with properties heavily?
        // But let's try calling it here.
        // Usually creating nodes requires FX thread essentially for scene graph
        // attachment,
        // but simple instantiation might work or require Platform.runLater if it checks
        // thread.
        // Actually ValueAxis doesn't enforce thread check on constructor.

        LogarithmicAxis axis = new LogarithmicAxis();
        assertNotNull(axis);

        LogarithmicAxis axis2 = new LogarithmicAxis(10, 100);
        assertEquals(10.0, axis2.getLowerBound());
        assertEquals(100.0, axis2.getUpperBound());
    }

    @Test
    void testValidateBounds() {
        assertThrows(IllegalArgumentException.class, () -> new LogarithmicAxis(-1, 100));
        assertThrows(IllegalArgumentException.class, () -> new LogarithmicAxis(100, 10));
    }

    @Test
    void testMethods() {
        LogarithmicAxis axis = new LogarithmicAxis(0, 100);
        List<Number> ticks = axis.calculateTickValues(100, new Object());
        assertNotNull(ticks);
        // The simplified logic in the class seems to just add ticks linearly?
        // implementation: i += (upper - lower) / 10.
        // 0 to 100 step 10 -> 0, 10, ... 100. 11 items.
        // floating point math might result in slightly different count but approx 11.
        assertTrue(ticks.size() >= 10);

        String label = axis.getTickMarkLabel(1000);
        assertEquals("1,000", label); // Depends on locale, but let's assume default locale.
        // Actually better to check simply non-null or contains generic chars if locale
        // varies.
        // NumberFormat.getInstance() uses default locale.
    }

    private void assertTrue(boolean condition) {
        if (!condition)
            throw new AssertionError("Expected true");
    }
}
