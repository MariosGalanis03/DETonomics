package com.detonomics.budgettuner.util;

import java.util.List;
import java.util.stream.Collectors;

public final class PlotlyHelper {

        private PlotlyHelper() {
                throw new AssertionError("Utility class");
        }

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
