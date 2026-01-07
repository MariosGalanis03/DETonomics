
package com.detonomics.budgettuner.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class PlotlyHelperTest {

    @Test
    void testGetHtml() {
        String html = PlotlyHelper.getHtml("chart1", "{}");
        assertNotNull(html);
        assertTrue(html.contains("id='chart1'"));
        assertTrue(html.contains("var config = {};"));
    }

    @Test
    void testCreateTrace() {
        List<String> x = Arrays.asList("2020", "2021");
        List<Double> y = Arrays.asList(100.0, 200.0);
        String trace = PlotlyHelper.createTrace("Series 1", x, y, "red");

        assertTrue(trace.contains("name: 'Series 1'"));
        assertTrue(trace.contains("x: ['2020','2021']"));
        assertTrue(trace.contains("y: [100.0,200.0]"));
        assertTrue(trace.contains("color: 'red'"));
    }

    @Test
    void testCreateLayout() {
        String layout = PlotlyHelper.createLayout("Main Chart", "Amount");
        assertTrue(layout.contains("title: { text: 'Main Chart'"));
        assertTrue(layout.contains("title: 'Amount'"));
    }

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<PlotlyHelper> constructor = PlotlyHelper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertNotNull(exception.getCause());
        assertEquals(AssertionError.class, exception.getCause().getClass());
    }
}
