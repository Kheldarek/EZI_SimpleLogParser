import org.joda.time.DateTime;
import org.joda.time.Minutes;

import java.util.*;
import java.util.stream.Collectors;

public class SessionRegister {
    private List<Session> sessions = new ArrayList<>();

    public void addSession(String IP, DateTime start, String request) {
        sessions.add(new Session(IP,start,request));
    }

    public void endSession(String IP, DateTime end) {
        Optional<Session> toEnd = sessions.stream()
                .filter(session -> session.getIP().equals(IP))
                .filter(session -> session.getEnd() == null)
                .findFirst();
        toEnd.ifPresent(session-> session.setEnd(end));
    }

    public void updateSession(String IP, String request){
        Optional<Session> toUpdate = sessions.stream()
                .filter(session -> session.getIP().equals(IP))
                .filter(session -> session.getEnd() == null)
                .findFirst();
        toUpdate.ifPresent(session -> session.setLength(session.getLength()+1));
        toUpdate.ifPresent(session -> session.setLast(request));

    }

    public void closeFreeSessions(Map<String, DateTime> lastTimesToIp) {
        sessions.stream()
                .filter(session -> session.getEnd() == null)
                .forEach(session -> session.setEnd(lastTimesToIp.get(session.getIP())));
    }

    public Map<Integer,Integer> getNumberOfRequestsMap(){
        Map<Integer,Integer> lengthMap = new HashMap<>();
        for(Session session : sessions){
            Integer length = session.getLength();
            if(!lengthMap.containsKey(length)){
                lengthMap.put(length,1);
            } else {
                lengthMap.put(length,lengthMap.get(length)+1);
            }
        }
        return lengthMap;
    }

    public Map<Integer,Integer> getMinutesMap() {
        Map<Integer,Integer> minuteMap = new HashMap<>();
        for(Session session : sessions){
            Integer minutes = Minutes.minutesBetween(session.getStart(), session.getEnd()).getMinutes();
            if(!minuteMap.containsKey(minutes)){
                minuteMap.put(minutes,1);
            } else {
                minuteMap.put(minutes,minuteMap.get(minutes)+1);
            }
        }
        return minuteMap;
    }

    public Map<String,Integer> getEntryPointMap() {
        Map<String,Integer> entryPoints = new HashMap<>();
        for(Session session : sessions){
            String request = session.getEntry();
            if(!entryPoints.containsKey(request)){
                entryPoints.put(request,1);
            } else {
                entryPoints.put(request,entryPoints.get(request)+1);
            }
        }
        return entryPoints;
    }

    public Map<String,Integer> getDeparturePointMap() {
        Map<String,Integer> departurePoint = new HashMap<>();
        for(Session session : sessions){
            String request = session.getLast();
            if(!departurePoint.containsKey(request)){
                departurePoint.put(request,1);
            } else {
                departurePoint.put(request,departurePoint.get(request)+1);
            }
        }
        return departurePoint;
    }

    public void printFirstTenPaths() {
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println("PATHS:");
        System.out.println("---------------------------------------------------------------------------------");
        for(int i =0; i<10; i++){
            Session session = sessions.get(i);
            List<String> path = session.getPath();
            System.out.println("Session " + session.getIP() + " started " + session.getStart() + " ended " + session.getEnd() + " request count: " + session.getLength());
            System.out.println("Path: " + String.join("-->", path));
            System.out.println("---------------------------------------------------------------------------------");
        }
    }
}
