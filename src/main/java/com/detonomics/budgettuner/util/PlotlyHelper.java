package com.detonomics.budgettuner.util;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility for generating HTML and JavaScript snippets to render Plotly.js
 * charts in embedded WebViews.
 */
public final class PlotlyHelper {

        private PlotlyHelper() {
                throw new AssertionError("Utility class");
        }

        /**
         * Wrap a Plotly configuration in a complete, self-contained HTML document.
         *
         * @param chartId    Target DOM element ID
         * @param jsonConfig Serialized Plotly chart configuration
         * @return Complete HTML string ready for WebView loading
         */
        public static String getHtml(final String chartId,
                        final String jsonConfig) {
                return "<!DOCTYPE html>"
                                + "<html>"
                                + "<head>"
                                + "    <script src='https://cdn.plot.ly/"
                                + "plotly-2.27.0.min.js'>"
                                + "</script>"
                                + "    <style>"
                                + "        body { margin: 0; padding: 0; "
                                + "overflow: hidden; "
                                + "font-family: 'Segoe UI', sans-serif; }"
                                + "        .chart-container { width: 100vw; "
                                + "height: 100vh; }"
                                + "    </style>"
                                + "</head>"
                                + "<body>"
                                + "    <div id='" + chartId
                                + "' class='chart-container'></div>"
                                + "    <script>"
                                + "        var config = " + jsonConfig + ";"
                                + "        Plotly.newPlot('" + chartId
                                + "', config.data, config.layout, "
                                + "{responsive: true, displayModeBar: false});"
                                + "    </script>"
                                + "</body>"
                                + "</html>";
        }

        /**
         * Construct a JSON representation of a single Plotly data series (trace).
         *
         * @param name  Display name for the series
         * @param x     List of X-axis categories
         * @param y     List of Y-axis numeric values
         * @param color CSS color for the line and markers
         * @return Trace object snippet in JSON format
         */
        public static String createTrace(final String name,
                        final List<String> x, final List<Double> y,
                        final String color) {
                final String xData = x.stream()
                                .map(s -> "'" + s + "'")
                                .collect(Collectors.joining(","));
                final String yData = y.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(","));

                return "{"
                                + "  type: 'scatter',"
                                + "  mode: 'lines+markers',"
                                + "  name: '" + name + "',"
                                + "  x: [" + xData + "],"
                                + "  y: [" + yData + "],"
                                + "  line: {color: '" + color + "', width: 3},"
                                + "  marker: {size: 6}"
                                + "}";
        }

        /**
         * Construct a JSON representation of the Plotly chart layout and aesthetics.
         *
         * @param title      Main chart title
         * @param yAxisTitle Label for the vertical value axis
         * @return Layout object snippet in JSON format
         */
        public static String createLayout(final String title,
                        final String yAxisTitle) {
                return "{"
                                + "  title: { text: '" + title
                                + "', font: { size: 16 } },"
                                + "  margin: { t: 40, r: 20, l: 60, b: 40 },"
                                + "  paper_bgcolor: 'rgba(0,0,0,0)',"
                                + "  plot_bgcolor: 'rgba(0,0,0,0)',"
                                + "  xaxis: { title: 'Έτος', showgrid: false },"
                                + "  yaxis: { title: '" + yAxisTitle
                                + "', gridcolor: '#eee' },"
                                + "  showlegend: true,"
                                + "  legend: { orientation: 'h', y: -0.2 }"
                                + "}";
        }
}
