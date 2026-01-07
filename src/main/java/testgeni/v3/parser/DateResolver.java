package testgeni.v3.parser;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to resolve relative date expressions and normalize absolute dates.
 */
public class DateResolver {

    /**
     * Resolve relative date expressions or normalize absolute dates into ISO format (yyyy-MM-dd).
     */
    public static String resolveDate(String expr) {
        if (expr == null || expr.trim().isEmpty()) return null;
        
        String cleanExpr = expr.toLowerCase().trim();
        LocalDate now = LocalDate.now();
        LocalDate target = null;
        
        // 1. Check for Absolute Date Formats first
        target = tryParseAbsoluteDate(expr);
        if (target != null) {
             return target.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        // 2. Pure number (e.g., "90")
        if (cleanExpr.matches("\\d+")) {
            try {
                // Only treat as date offset if it's a reasonably small number
                if (cleanExpr.length() <= 4) { 
                    int days = Integer.parseInt(cleanExpr);
                    if (days > 31) { // Likely offset rather than day of month
                        target = now.plusDays(days);
                    } else {
                        return null; // Let it be treated as a day number by the executor
                    }
                }
            } catch (NumberFormatException e) {
                // Too large for an int, definitely not a date offset
                return null;
            }
        }
        
        // 3. Named relatives
        else if (cleanExpr.contains("tomorrow")) target = now.plusDays(1);
        else if (cleanExpr.contains("yesterday")) target = now.minusDays(1);
        else if (cleanExpr.contains("next week")) target = now.plusWeeks(1);
        else if (cleanExpr.contains("last week")) target = now.minusWeeks(1);
        else if (cleanExpr.contains("next month")) target = now.plusMonths(1);
        
        // 4. Offset phrases (e.g., "10 days from now", "5 months later")
        else if (cleanExpr.matches("(\\d+)\\s+(day|week|month|year)s?\\s+(from now|later|ahead)")) {
            Pattern p = Pattern.compile("(\\d+)\\s+(day|week|month|year)");
            Matcher m = p.matcher(cleanExpr);
            if (m.find()) {
                int amount = Integer.parseInt(m.group(1));
                String unit = m.group(2);
                if (unit.startsWith("day")) target = now.plusDays(amount);
                else if (unit.startsWith("week")) target = now.plusWeeks(amount);
                else if (unit.startsWith("month")) target = now.plusMonths(amount);
                else if (unit.startsWith("year")) target = now.plusYears(amount);
            }
        }
        
        if (target != null) {
            return target.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        
        return null;
    }

    /**
     * Attempt to parse a date from various common user formats.
     */
    public static LocalDate tryParseAbsoluteDate(String input) {
        String[] formats = {
            "dd-MM-yyyy", "dd/MM/yyyy", "dd.MM.yyyy",
            "MM-dd-yyyy", "MM/dd/yyyy",
            "yyyy-MM-dd", "yyyy/MM/dd",
            "MMM dd, yyyy", "MMMM dd, yyyy",
            "dd MMM yyyy", "dd MMMM yyyy"
        };

        for (String format : formats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.ENGLISH);
                return LocalDate.parse(input, formatter);
            } catch (Exception e) {
                // Try next format
            }
        }
        return null;
    }
}
