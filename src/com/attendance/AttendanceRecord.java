package com.attendance;

public class AttendanceRecord {
    private String name;
    private String dateTime;

    public AttendanceRecord(String name, String dateTime) {
        this.name = name;
        this.dateTime = dateTime;
    }

    public String getName() { return name; }
    public String getDateTime() { return dateTime; }

    @Override
    public String toString() {
        return name + " marked present on " + dateTime;
    }
}
