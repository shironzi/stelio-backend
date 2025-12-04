package com.aaronjosh.real_estate_app.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {
    private DateTimeUtils() {
    }

    // converts localDateTime to readable date and time.
    public static String formatLongDateTime(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a");
        return date.format(formatter);
    }
}
