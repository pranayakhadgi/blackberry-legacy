package com.projectblackberry;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class WeekUtils {
    private static final WeekFields WEEK_FIELDS = WeekFields.of(Locale.getDefault());
    
    public static String getCurrentWeekId() {
        LocalDate now = LocalDate.now();
        int year = now.get(WEEK_FIELDS.weekBasedYear());
        int week = now.get(WEEK_FIELDS.weekOfWeekBasedYear());
        return String.format("%d-W%d", year, week);
    }
    
    public static String formatWeekId(String weekId) {
        try {
            String[] parts = weekId.split("-W");
            if (parts.length == 2) {
                return "Week " + parts[1] + ", " + parts[0];
            }
        } catch (Exception e) {
            // Fall through
        }
        return weekId;
    }
}

