package edu.tamu.catalog.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.time.DateUtils;

public class FolioDatetime {

    private static final String FOLIO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final TimeZone FOLIO_DATE_TIMEZONE = TimeZone.getTimeZone("UTC");

    // @formatter:off
    private static final String[] datePatterns = {
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ssZ",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss'Z'"
    };
    // @formatter:on

    /**
     * Parse the Folio Date, converting it to a Java Date.
     * @param value the Folio date string.
     * @return The parsed Java Date.
     * @throws ParseException
     */
    public static Date parse(String value) throws ParseException {
        return DateUtils.parseDate(value, datePatterns);
    }

    /**
     * Convert the Java Date to a Folio Date string.
     * The date is convert to "yyyy-MM-dd'T'HH:mm:ss.SSSZ".
     * @param Date date
     * @return
     * @throws ParseException
     */
    public static String convert(Date date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(FOLIO_DATE_FORMAT);
        formatter.setTimeZone(FOLIO_DATE_TIMEZONE);

        return formatter.format(date);
    }

}