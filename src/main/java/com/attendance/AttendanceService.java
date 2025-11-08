package com.attendance;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AttendanceService {
    private static final String FILE_NAME = "attendance.txt";
    private static final String KNOWN_FACES_DIR = "known_faces";

    public static String markAttendanceByName(String name) {
        try (FileWriter fw = new FileWriter(FILE_NAME, true)) {
            String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
            AttendanceRecord record = new AttendanceRecord(name, dateTime);
            fw.write(record.toString() + System.lineSeparator());
            return "✅ Attendance marked for " + name;
        } catch (IOException e) {
            e.printStackTrace();
            return "❌ Error saving attendance: " + e.getMessage();
        }
    }

    public static boolean isRegistered(String name) {
        File f = new File(KNOWN_FACES_DIR, name + ".jpg");
        return f.exists();
    }
}
