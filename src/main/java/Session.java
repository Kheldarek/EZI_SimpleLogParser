import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class Session {

    private String IP;
    private DateTime start;
    private DateTime end;
    private Integer length;
    private String entry;
    private String last;
    private List<String> path = new ArrayList<>();


    public Session(String IP, DateTime start, String entry) {
        this.IP = IP;
        this.start = start;
        this.length = 1;
        this.entry = entry;
        this.last = entry;
        this.path.add(entry);
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public DateTime getStart() {
        return start;
    }

    public void setStart(DateTime start) {
        this.start = start;
    }

    public DateTime getEnd() {
        return end;
    }

    public void setEnd(DateTime end) {
        this.end = end;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.path.add(last);
        this.last = last;
    }

    public List<String> getPath() {
        return path;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }
}
