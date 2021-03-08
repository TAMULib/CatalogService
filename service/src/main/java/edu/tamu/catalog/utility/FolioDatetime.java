package edu.tamu.catalog.utility;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

public class FolioDatetime {

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

    public static Date parse(String value) throws ParseException {
        return DateUtils.parseDate(value, datePatterns);
    }

}