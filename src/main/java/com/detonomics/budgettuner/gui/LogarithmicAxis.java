package com.detonomics.budgettuner.gui;

import javafx.scene.chart.ValueAxis;
import java.util.ArrayList;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * A logarithmic axis for JavaFX Charts.
 */
public class LogarithmicAxis extends ValueAxis<Number> {

    public LogarithmicAxis() {
        super();
    }

    public LogarithmicAxis(double lowerBound, double upperBound) {
        super(lowerBound, upperBound);
        try {
            validateBounds(lowerBound, upperBound);
        } catch (Exception e) {
            // Default to safe values if invalid
            setLowerBound(1);
            setUpperBound(100);
        }
    }

    private void validateBounds(double lowerBound, double upperBound) throws IllegalArgumentException {
        if (lowerBound < 0 || upperBound < 0 || lowerBound > upperBound) {
            // Basic sanity check, but allow correction in logic
        }
    }

    @Override
    protected List<Number> calculateMinorTickMarks() {
        List<Number> minorTickMarks = new ArrayList<>();
        double lowerBound = getLowerBound();
        double upperBound = getUpperBound();

        // Clamp to avoid log(0) = -Infinity
        if (lowerBound <= 0)
            lowerBound = 1;
        if (upperBound <= lowerBound)
            upperBound = lowerBound + 1;

        for (double i = Math.pow(10, Math.floor(Math.log10(lowerBound))); i < upperBound; i *= 10) {
            for (int j = 2; j <= 9; j++) {
                double value = i * j;
                if (value >= lowerBound && value <= upperBound) {
                    minorTickMarks.add(value);
                }
            }
        }
        return minorTickMarks;
    }

    @Override
    protected List<Number> calculateTickValues(double length, Object range) {
        List<Number> tickValues = new ArrayList<>();
        double lowerBound = getLowerBound();
        double upperBound = getUpperBound();

        // Clamp to avoid infinite loop where i stays 0
        if (lowerBound <= 0)
            lowerBound = 1;
        if (upperBound <= lowerBound)
            return tickValues;

        for (double i = Math.pow(10, Math.ceil(Math.log10(lowerBound))); i <= upperBound; i *= 10) {
            tickValues.add(i);
        }
        return tickValues;
    }

    @Override
    protected String getTickMarkLabel(Number value) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        formatter.setMaximumFractionDigits(0);
        return formatter.format(value);
    }

    @Override
    protected Object getRange() {
        return new Object[] { getLowerBound(), getUpperBound() };
    }

    @Override
    protected void setRange(Object range, boolean animate) {
        if (range != null) {
            Object[] rangeProps = (Object[]) range;
            double lower = (Double) rangeProps[0];
            double upper = (Double) rangeProps[1];

            // Auto-ranging often tries to start at 0. Clamp it.
            if (lower <= 0)
                lower = 1;
            if (upper <= lower)
                upper = lower + 100;

            setLowerBound(lower);
            setUpperBound(upper);
        }
    }

    @Override
    public Number getValueForDisplay(double displayPosition) {
        double min = getLowerBound();
        double max = getUpperBound();
        double delta = Math.log10(max) - Math.log10(min);
        if (getSide().isVertical()) {
            return Math.pow(10, ((1 - (displayPosition / getHeight())) * delta) + Math.log10(min));
        } else {
            return Math.pow(10, ((displayPosition / getWidth()) * delta) + Math.log10(min));
        }
    }

    @Override
    public double getDisplayPosition(Number value) {
        double min = getLowerBound();
        double max = getUpperBound();
        double delta = Math.log10(max) - Math.log10(min);
        double val = value.doubleValue();

        if (delta == 0 || val <= 0)
            return 0;

        double pos = (Math.log10(val) - Math.log10(min)) / delta;

        if (getSide().isVertical()) {
            return (1 - pos) * getHeight();
        } else {
            return pos * getWidth();
        }
    }

    /**
     * Override zero position to be the lower bound, since log(0) is undefined.
     * This ensures BarChart draws bars starting from lowerBound instead of trying
     * to find 0.
     */
    @Override
    public double getZeroPosition() {
        return getDisplayPosition(getLowerBound());
    }
}
