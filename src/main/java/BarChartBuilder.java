import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.Styler;

import java.util.ArrayList;
import java.util.Map;

public class BarChartBuilder {

    public static CategoryChart getHoursChart(Map<Integer,Integer> dataMap){
        CategoryChart chart = new CategoryChartBuilder().width(800).height(600).title("Session start times").xAxisTitle("Hours").yAxisTitle("Started session count").build();
        return fillChart(dataMap, chart);

    }

    public static CategoryChart getMinutesChart(Map<Integer,Integer> dataMap){
        CategoryChart chart = new CategoryChartBuilder().width(1400).height(600).title("Session duration").xAxisTitle("Minutes").yAxisTitle("Session count").build();
        return fillChart(dataMap, chart);

    }

    public static CategoryChart getRequestNumberChart(Map<Integer,Integer> dataMap){
        CategoryChart chart = new CategoryChartBuilder().width(800).height(600).title("Session request number").xAxisTitle("Request number").yAxisTitle("Session count").build();
        return fillChart(dataMap, chart);

    }

    private static CategoryChart fillChart(Map<Integer, Integer> dataMap, CategoryChart chart) {
        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
        chart.getStyler().setHasAnnotations(true);

        chart.addSeries("log.txt", new ArrayList<>(dataMap.keySet()), new ArrayList<>(dataMap.values()));
        return chart;
    }
}
