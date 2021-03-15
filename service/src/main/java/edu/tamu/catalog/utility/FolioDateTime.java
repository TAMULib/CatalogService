package edu.tamu.catalog.utility;

import java.text.ParseException;
import java.util.Date;

import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;

public class FolioDateTime {

    /**
     * Parse the Folio Date, converting it to a Java Date.
     *
     * @param value the Folio date string.
     *
     * @return The parsed Java Date.
     * @throws ParseException
     */
    public static Date parse(String value) {
        return ISODateTimeFormat.dateTimeParser()
            .withZoneUTC()
            .parseDateTime(value)
            .toDate();
    }

    /**
     * Convert the Java Date to a Folio Date string.
     * The date is convert to "yyyy-MM-dd'T'HH:mm:ss.SSSZ".
     *
     * @param Date date to convert.
     *
     * @return
     * @throws ParseException
     */
    public static String convert(Date date) {
        return DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .print(Instant.ofEpochMilli(date.toInstant().toEpochMilli()));
    }

}
