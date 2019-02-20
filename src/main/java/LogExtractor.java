import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.knowm.xchart.SwingWrapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogExtractor {

    private final static String FILE_NAME = "log.txt";

    private final String ipRegex = "\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b";
    private final String dateRegex = "(\\d{2})/([a-zA-Z]{3})/(\\d{4}):(\\d{2}):(\\d{2}):(\\d{2})\\s([+-]\\d{4})";
    private final String requestRegex = "([^\\s]+).html";
    private final Pattern ipPattern = Pattern.compile(ipRegex);
    private final Pattern datePattern = Pattern.compile(dateRegex);
    private final Pattern requestPattern = Pattern.compile(requestRegex);

    private Map<String, Integer> ipCount = new HashMap<>();
    private Map<String, Integer> sessionCountForIP = new HashMap<>();
    private Map<String, DateTime> lastTimestampForIp = new HashMap<>();
    private Map<String, String> lastRequestForIP = new HashMap<>();
    private Map<Integer, Integer> sessionStartingTimes = new HashMap<>();
    private SessionRegister sessionRegister = new SessionRegister();


    public void start() throws IOException {
        processLogs();
        new SwingWrapper<>(BarChartBuilder.getHoursChart(sessionStartingTimes)).displayChart();
        new SwingWrapper<>(BarChartBuilder.getRequestNumberChart(sessionRegister.getNumberOfRequestsMap())).displayChart();
        new SwingWrapper<>(BarChartBuilder.getMinutesChart(sessionRegister.getMinutesMap())).displayChart();
    }

    private void processLogs() throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        parse(lines);
    }

    private void parse(ArrayList<String> records) {
        records.forEach(this::parseRecord);
        sessionRegister.closeFreeSessions(lastTimestampForIp);
        printOutput();
        printDepartureAndEntries();
        sessionRegister.printFirstTenPaths();
    }

    private void parseRecord(String record) {
        final Matcher ipMatcher = ipPattern.matcher(record);
        final Matcher dateMatcher = datePattern.matcher(record);
        final Matcher requestMatcher = requestPattern.matcher(record);

        if (ipMatcher.find()) {
            String IP = handleIP(ipMatcher);
            if (dateMatcher.find()) {
                DateTime dateTime = getDate(dateMatcher);
                if (requestMatcher.find()) {
                    handleRequest(requestMatcher, IP, dateTime);
                }
            }
        }
    }

    private String handleIP(Matcher ipMatcher) {
        String IP = ipMatcher.group();
        if (ipCount.containsKey(IP)) {
            ipCount.put(IP, ipCount.get(IP) + 1);
        } else {
            ipCount.put(IP, 1);
        }
        return IP;
    }

    private void handleRequest(Matcher requestMatcher, String IP, DateTime dateTime) {
        String request = requestMatcher.group();
        handleCurrentRequest(IP, dateTime, request);
        lastRequestForIP.put(IP, request);
        lastTimestampForIp.put(IP, dateTime);
    }

    private DateTime getDate(Matcher dateMatcher) {
        String date = dateMatcher.group();
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MMM/yyyy:HH:mm:ss Z").withLocale(Locale.ROOT);
        return formatter.parseDateTime(date);
    }

    private void handleCurrentRequest(String IP, DateTime dateTime, String request) {
        if (sessionCountForIP.containsKey(IP)) {
            if (isNewSession(IP, dateTime, request)) {
                sessionCountForIP.put(IP, sessionCountForIP.get(IP) + 1);
                storeSessionStartHour(dateTime);
                sessionRegister.endSession(IP, lastTimestampForIp.get(IP));
                sessionRegister.addSession(IP,dateTime,request);
                return;
            }
        } else {
            sessionCountForIP.put(IP, 1);
            storeSessionStartHour(dateTime);
            sessionRegister.addSession(IP,dateTime,request);
            return;
        }
        sessionRegister.updateSession(IP, request);
    }

    private boolean isNewSession(String IP, DateTime dateTime, String request) {
        return Minutes.minutesBetween(dateTime, lastTimestampForIp.get(IP)).getMinutes() > 15 || isNewSession(IP, request);
    }

    private boolean isNewSession(String IP, String request) {
        if ("Shop.html".equals(request)) {
            return true;
        }
        if ("AddProduct.html".equals(request) && !lastRequestForIP.get(IP).equals("Shop.html") && !lastRequestForIP.get(IP).equals("About.html")) {
            return true;
        }
        if ("Summary.html".equals(request) && !lastRequestForIP.get(IP).equals("AddProduct.html")) {
            return true;
        }
        if ("Payment.html".equals(request) && !lastRequestForIP.get(IP).equals("Summary.html")) {
            return true;
        }
        if ("About.html".equals(request) && !lastRequestForIP.get(IP).equals("Shop.html") && !lastRequestForIP.get(IP).equals("AddProduct.html")) {
            return true;
        }
        if ("Contact.html".equals(request)) {
            return !lastRequestForIP.get(IP).equals("About.html");
        }
        return false;
    }

    private void storeSessionStartHour(DateTime dateTime) {
        Integer hour = Integer.parseInt(dateTime.hourOfDay().getAsString());
        if(sessionStartingTimes.containsKey(hour)){
            sessionStartingTimes.put(hour,sessionStartingTimes.get(hour)+1);
        } else {
            sessionStartingTimes.put(hour,1);
        }
    }

    private void printOutput() {
        int numberOfSessions = 0;
        for (Map.Entry<String, Integer> entry : sessionCountForIP.entrySet()) {
            System.out.println("User : " + entry.getKey() + " session count : " + entry.getValue());
            Integer numberOfSessionOfOneUser = entry.getValue();
            numberOfSessions = numberOfSessions + numberOfSessionOfOneUser;
        }
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println("Total users : " + ipCount.entrySet().size());
        System.out.println("Total sessions count : " + numberOfSessions);
        System.out.println("Average user session count : " + numberOfSessions / (float) ipCount.entrySet().size());
    }

    private void printDepartureAndEntries(){
        Map<String, Integer> entryPointMap = sessionRegister.getEntryPointMap();
        Map.Entry<String, Integer> maxEntries = entryPointMap.entrySet().stream().max(Comparator.comparingInt(Map.Entry::getValue)).get();
        Map<String, Integer> departurePointMap = sessionRegister.getDeparturePointMap();
        Map.Entry<String, Integer> maxExits = departurePointMap.entrySet().stream().max(Comparator.comparingInt(Map.Entry::getValue)).get();

        System.out.println("Most entries: " + maxEntries.getKey() + " with number: " +  maxEntries.getValue());
        System.out.println("Most departures: " + maxExits.getKey() + " with number: " + maxExits.getValue());
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println("All entries:");
        for(Map.Entry<String, Integer> entry : entryPointMap.entrySet()){
            System.out.println("Entries: " + entry.getKey() + " with number: " +  entry.getValue());
        }
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println("All departures:");
        for(Map.Entry<String, Integer> entry : departurePointMap.entrySet()){
            System.out.println("Departure: " + entry.getKey() + " with number: " +  entry.getValue());
        }
    }
}
