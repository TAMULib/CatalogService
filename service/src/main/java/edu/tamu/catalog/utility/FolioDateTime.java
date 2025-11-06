package edu.tamu.catalog.utility;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MILLI_OF_SECOND;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.SimpleTimeZone;

/**
 * Provide the ability to convert to or parse from dates and times.
 */
public class FolioDateTime {

    /**
     * The date string format to use.
     */
    public static final String DATE_STRING_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /**
     * The format string representing an HTTP Cookie expire date.
     */
    public static final DateTimeFormatter COOKIE_DATE_TIME_FORMAT = DateTimeFormatter
        .ofPattern("EEE, dd MMM yyyy HH:mm:ss z").withZone(ZoneId.of("UTC"));

    /**
     * A variation of ISO_LOCAL_TIME down to minutes.
     */
    public static final DateTimeFormatter TIME_MINUTES;
    static {
        TIME_MINUTES = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .toFormatter();
    }

    /**
     * A variation of ISO_LOCAL_TIME down to seconds.
     */
    public static final DateTimeFormatter TIME_SECONDS;
    static {
        TIME_SECONDS = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(TIME_MINUTES)
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .toFormatter();
    }

    /**
     * A variation of ISO_LOCAL_TIME down to milliseconds.
     */
    public static final DateTimeFormatter TIME;
    static {
        TIME = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(TIME_SECONDS)
            .optionalStart()
            .appendFraction(MILLI_OF_SECOND, 3, 3, true)
            .parseLenient()
            .appendOffset("+HHMM", "Z")
            .parseStrict()
            .toFormatter();
    }

    /**
     * A variation of ISO_LOCAL_TIME down to nanoseconds.
     */
    public static final DateTimeFormatter TIME_NANOSECONDS;
    static {
        TIME_NANOSECONDS = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(TIME_SECONDS)
            .optionalStart()
            .appendFraction(NANO_OF_SECOND, 0, 9, true)
            .parseLenient()
            .appendOffset("+HHMM", "Z")
            .parseStrict()
            .toFormatter();
    }

    /**
     * A variation of ISO_OFFSET_DATE_TIME down to milliseconds.
     */
    public static final DateTimeFormatter DATE_TIME;
    static {
        DATE_TIME = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_DATE)
            .appendLiteral('T')
            .append(TIME)
            .toFormatter();
    }

    /**
     * A variation of ISO_OFFSET_DATE_TIME down to nanoseconds.
     */
    public static final DateTimeFormatter DATE_TIME_NANOSECONDS;
    static {
        DATE_TIME_NANOSECONDS = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_DATE)
            .appendLiteral('T')
            .append(TIME_NANOSECONDS)
            .toFormatter();
    }

    /**
     * Get standard dateTime formatters.
     *
     * @return A list of standard formatters.
     */
    public static List<DateTimeFormatter> getDateTimeFormatters() {
        DateTimeFormatter startOfDay = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .parseDefaulting(ChronoField.NANO_OF_DAY, 0)
            .parseDefaulting(ChronoField.OFFSET_SECONDS, 0)
            .toFormatter();

        return List.of(
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_ZONED_DATE_TIME,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneOffset.UTC),
            COOKIE_DATE_TIME_FORMAT,
            DATE_TIME,
            DATE_TIME_NANOSECONDS,
            startOfDay
        );
    }

    /**
     * Convert the Java Date to a Folio Date string.
     *
     * The date is converted into "yyyy-MM-dd'T'HH:mm:ss.SSSZ".
     *
     * @param date The date to convert.
     *
     * @return The converted string.
     */
    public static String convert(Date date) {
        SimpleDateFormat simple = new SimpleDateFormat();
        simple.setTimeZone(new SimpleTimeZone(0, "UTC"));
        simple.applyPattern(DATE_STRING_FORMAT);

        return simple.format(date);
    }

    /**
     * Parse the Folio Date, converting it to a Java Date.
     *
     * @param value the Folio date string.
     *
     * @return The parsed Java Date.
     */
    public static Date parse(String value) {
        return Date.from(parseZonedDateTime(value).toInstant());
    }

    /**
     * Convert the string to a zoned date and time.
     *
     * Multiple common formatters are attempted due to DateTimeFormatter.
     *
     * @param value The date string.
     *
     * @return The converted zoned date and time.
     */
    public static ZonedDateTime parseZonedDateTime(String value) {
        List<DateTimeFormatter> formatters = getDateTimeFormatters();

        for (int i = 0; i < formatters.size(); i++) {
            try {
                DateTimeFormatter formatter = formatters.get(i);

                return ZonedDateTime.parse(value, formatter).truncatedTo(ChronoUnit.MILLIS);
            } catch (DateTimeParseException e) {
                if (i == formatters.size() - 1) {
                    throw e;
                }
            }
        }

        return ZonedDateTime.parse(value).truncatedTo(ChronoUnit.MILLIS);
    }

}
