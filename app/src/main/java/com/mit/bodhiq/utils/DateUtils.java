package com.mit.bodhiq.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Utility class for date and time operations throughout the application.
 * Provides consistent date formatting, parsing, and manipulation methods.
 */
@Singleton
public class DateUtils {
    
    // Standard date formats used in the application
    public static final String FORMAT_DISPLAY_DATE = "MMM dd, yyyy";
    public static final String FORMAT_DISPLAY_DATETIME = "MMM dd, yyyy HH:mm";
    public static final String FORMAT_ISO_DATE = "yyyy-MM-dd";
    public static final String FORMAT_ISO_DATETIME = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String FORMAT_TIMESTAMP = "yyyyMMdd_HHmmss";
    public static final String FORMAT_REPORT_DATE = "MMMM dd, yyyy";
    public static final String FORMAT_SHORT_DATE = "MM/dd/yyyy";
    
    private final SimpleDateFormat displayDateFormat;
    private final SimpleDateFormat displayDateTimeFormat;
    private final SimpleDateFormat isoDateFormat;
    private final SimpleDateFormat isoDateTimeFormat;
    private final SimpleDateFormat timestampFormat;
    private final SimpleDateFormat reportDateFormat;
    private final SimpleDateFormat shortDateFormat;
    
    @Inject
    public DateUtils() {
        Locale defaultLocale = Locale.getDefault();
        
        this.displayDateFormat = new SimpleDateFormat(FORMAT_DISPLAY_DATE, defaultLocale);
        this.displayDateTimeFormat = new SimpleDateFormat(FORMAT_DISPLAY_DATETIME, defaultLocale);
        this.isoDateFormat = new SimpleDateFormat(FORMAT_ISO_DATE, defaultLocale);
        this.isoDateTimeFormat = new SimpleDateFormat(FORMAT_ISO_DATETIME, defaultLocale);
        this.timestampFormat = new SimpleDateFormat(FORMAT_TIMESTAMP, defaultLocale);
        this.reportDateFormat = new SimpleDateFormat(FORMAT_REPORT_DATE, defaultLocale);
        this.shortDateFormat = new SimpleDateFormat(FORMAT_SHORT_DATE, defaultLocale);
        
        // Set UTC timezone for ISO formats
        this.isoDateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    /**
     * Gets current timestamp in milliseconds.
     */
    public long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }
    
    /**
     * Gets current date as Date object.
     */
    public Date getCurrentDate() {
        return new Date();
    }
    
    /**
     * Formats timestamp to display date string (e.g., "Jan 15, 2024").
     */
    public String formatDisplayDate(long timestamp) {
        return displayDateFormat.format(new Date(timestamp));
    }
    
    /**
     * Formats timestamp to display datetime string (e.g., "Jan 15, 2024 14:30").
     */
    public String formatDisplayDateTime(long timestamp) {
        return displayDateTimeFormat.format(new Date(timestamp));
    }
    
    /**
     * Formats Date to display date string.
     */
    public String formatDisplayDate(Date date) {
        return displayDateFormat.format(date);
    }
    
    /**
     * Formats Date to display datetime string.
     */
    public String formatDisplayDateTime(Date date) {
        return displayDateTimeFormat.format(date);
    }    

    /**
     * Formats timestamp to ISO date string (e.g., "2024-01-15").
     */
    public String formatIsoDate(long timestamp) {
        return isoDateFormat.format(new Date(timestamp));
    }
    
    /**
     * Formats timestamp to ISO datetime string (e.g., "2024-01-15T14:30:00Z").
     */
    public String formatIsoDateTime(long timestamp) {
        return isoDateTimeFormat.format(new Date(timestamp));
    }
    
    /**
     * Formats timestamp to filename-safe timestamp (e.g., "20240115_143000").
     */
    public String formatTimestamp(long timestamp) {
        return timestampFormat.format(new Date(timestamp));
    }
    
    /**
     * Formats timestamp to report date format (e.g., "January 15, 2024").
     */
    public String formatReportDate(long timestamp) {
        return reportDateFormat.format(new Date(timestamp));
    }
    
    /**
     * Formats timestamp to short date format (e.g., "01/15/2024").
     */
    public String formatShortDate(long timestamp) {
        return shortDateFormat.format(new Date(timestamp));
    }
    
    /**
     * Parses display date string to timestamp.
     */
    public long parseDisplayDate(String dateString) throws ParseException {
        Date date = displayDateFormat.parse(dateString);
        return date != null ? date.getTime() : 0;
    }
    
    /**
     * Parses ISO date string to timestamp.
     */
    public long parseIsoDate(String dateString) throws ParseException {
        Date date = isoDateFormat.parse(dateString);
        return date != null ? date.getTime() : 0;
    }
    
    /**
     * Parses ISO datetime string to timestamp.
     */
    public long parseIsoDateTime(String dateTimeString) throws ParseException {
        Date date = isoDateTimeFormat.parse(dateTimeString);
        return date != null ? date.getTime() : 0;
    }
    
    /**
     * Safely parses a date string with multiple format attempts.
     */
    public long safeParseDateString(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return 0;
        }
        
        SimpleDateFormat[] formats = {
            displayDateFormat,
            isoDateFormat,
            shortDateFormat,
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
            new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
        };
        
        for (SimpleDateFormat format : formats) {
            try {
                Date date = format.parse(dateString.trim());
                if (date != null) {
                    return date.getTime();
                }
            } catch (ParseException e) {
                // Continue to next format
            }
        }
        
        return 0; // Return 0 if no format worked
    }   
 
    /**
     * Gets relative time string (e.g., "2 hours ago", "3 days ago").
     */
    public String getRelativeTimeString(long timestamp) {
        long now = getCurrentTimestamp();
        long diff = now - timestamp;
        
        if (diff < 0) {
            return "In the future";
        }
        
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        long months = days / 30;
        long years = days / 365;
        
        if (seconds < 60) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (hours < 24) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (days < 7) {
            return days + (days == 1 ? " day ago" : " days ago");
        } else if (weeks < 4) {
            return weeks + (weeks == 1 ? " week ago" : " weeks ago");
        } else if (months < 12) {
            return months + (months == 1 ? " month ago" : " months ago");
        } else {
            return years + (years == 1 ? " year ago" : " years ago");
        }
    }
    
    /**
     * Checks if a timestamp is today.
     */
    public boolean isToday(long timestamp) {
        Calendar today = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timestamp);
        
        return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
               today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR);
    }
    
    /**
     * Checks if a timestamp is yesterday.
     */
    public boolean isYesterday(long timestamp) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timestamp);
        
        return yesterday.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
               yesterday.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR);
    }
    
    /**
     * Checks if a timestamp is within the last week.
     */
    public boolean isWithinLastWeek(long timestamp) {
        long weekAgo = getCurrentTimestamp() - (7 * 24 * 60 * 60 * 1000L);
        return timestamp >= weekAgo;
    }
    
    /**
     * Gets the start of day timestamp for a given timestamp.
     */
    public long getStartOfDay(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    
    /**
     * Gets the end of day timestamp for a given timestamp.
     */
    public long getEndOfDay(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }
    
    /**
     * Adds days to a timestamp.
     */
    public long addDays(long timestamp, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTimeInMillis();
    }
    
    /**
     * Adds hours to a timestamp.
     */
    public long addHours(long timestamp, int hours) {
        return timestamp + (hours * 60 * 60 * 1000L);
    }
    
    /**
     * Adds minutes to a timestamp.
     */
    public long addMinutes(long timestamp, int minutes) {
        return timestamp + (minutes * 60 * 1000L);
    }
    
    /**
     * Gets a user-friendly date string based on recency.
     */
    public String getSmartDateString(long timestamp) {
        if (isToday(timestamp)) {
            return "Today";
        } else if (isYesterday(timestamp)) {
            return "Yesterday";
        } else if (isWithinLastWeek(timestamp)) {
            return getRelativeTimeString(timestamp);
        } else {
            return formatDisplayDate(timestamp);
        }
    }
    
    /**
     * Static method for formatting dates in adapters and other contexts.
     * Returns a user-friendly date string.
     */
    public static String formatDate(long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat(FORMAT_DISPLAY_DATE, Locale.getDefault());
        return format.format(new Date(timestamp));
    }
}