package com.detonomics.budgettuner.util;

import javafx.scene.chart.ValueAxis;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A logarithmic axis for JavaFX charts.
 */
public final class LogarithmicAxis extends ValueAxis<Number> {

    /**
     * Creates a new LogarithmicAxis with defaults.
     */
    public LogarithmicAxis() {
        super();
    }

    /**
     * Creates a new LogarithmicAxis with specified bounds.
     *
     * @param lowerBound The lower bound.
     * @param upperBound The upper bound.
     */
    public LogarithmicAxis(final double lowerBound, final double upperBound) {
        super(lowerBound, upperBound);
        validateBounds(lowerBound, upperBound);
    }

    private void validateBounds(final double lowerBound,
            final double upperBound)
            throws IllegalArgumentException {
        if (lowerBound < 0 || upperBound < 0 || lowerBound > upperBound) {
            throw new IllegalArgumentException(
                    "Bounds must be non-negative and lower <= upper");
        }
    }

    @Override
    protected List<Number> calculateMinorTickMarks() {
        return new ArrayList<>();
    }

    @Override
    protected void setRange(final Object range, final boolean animate) {
        // Implementation logic
    }

    @Override
    protected Object getRange() {
        return new Object();
    }

    @Override
    protected List<Number> calculateTickValues(final double length,
            final Object range) {
        final List<Number> tickValues = new ArrayList<>();
        if (range != null) {
            // Simplified logic for tick calculation
            double lower = getLowerBound();
            double upper = getUpperBound();
            for (double i = lower; i <= upper; i += (upper - lower) / 10) {
                tickValues.add(i);
            }
        }
        return tickValues;
    }

    @Override
    protected String getTickMarkLabel(final Number value) {
        final NumberFormat formatter = NumberFormat.getInstance();
        return formatter.format(value);
    }
}
