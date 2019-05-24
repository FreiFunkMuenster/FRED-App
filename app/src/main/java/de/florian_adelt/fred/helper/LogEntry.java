package de.florian_adelt.fred.helper;

public class LogEntry {

    protected long id;
    protected int level;
    protected String tag;
    protected String message;
    protected long time;

    public LogEntry(long id, int level, String tag, String message, long time) {
        this.id = id;
        this.level = level;
        this.tag = tag;
        this.message = message;
        this.time = time;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
