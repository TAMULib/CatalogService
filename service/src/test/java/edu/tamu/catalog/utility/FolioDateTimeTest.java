package edu.tamu.catalog.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class FolioDateTimeTest {

    @ParameterizedTest
    @MethodSource("argumentsParse")
    public void testParse(String input, Date output) {
        assertEquals(output.getTime(), FolioDateTime.parse(input).getTime());
    }

    @ParameterizedTest
    @MethodSource("argumentsParseWithException")
    public void testParseWithException(String input, Class<Exception> exception) {
        assertThrows(exception, () -> {
            FolioDateTime.parse(input);
        });
    }

    @ParameterizedTest
    @MethodSource("argumentsConvert")
    public void testConvert(Date input, String output) {
        assertEquals(output, FolioDateTime.convert(input));
    }

    @ParameterizedTest
    @MethodSource("argumentsConvertWithException")
    public void testConvertWithException(Date input, Class<Exception> exception) {
        assertThrows(exception, () -> {
            FolioDateTime.convert(input);
        });
    }

    private static Stream<? extends Arguments> argumentsParse() {
        Date date = Date.from(Instant.ofEpochMilli(1615569460770L));
        Date dateNoMilliseconds = Date.from(Instant.ofEpochMilli(1615569460770L).truncatedTo(ChronoUnit.SECONDS));

        return Stream.of(
          Arguments.of("2021-03-12T17:17:40.770+0000", date),
          Arguments.of("2021-03-12T17:17:40.770", date),
          Arguments.of("2021-03-12T17:17:40.770Z", date),
          Arguments.of("2021-03-12T17:17:40+0000", dateNoMilliseconds),
          Arguments.of("2021-03-12T17:17:40", dateNoMilliseconds),
          Arguments.of("2021-03-12T17:17:40Z", dateNoMilliseconds)
        );
    }

    private static Stream<? extends Arguments> argumentsParseWithException() {
        return Stream.of(
          Arguments.of((String) null, NullPointerException.class),
          Arguments.of("", IllegalArgumentException.class),
          Arguments.of(" ", IllegalArgumentException.class),
          Arguments.of("bad date", IllegalArgumentException.class)
        );
    }

    private static Stream<? extends Arguments> argumentsConvert() {
        Date date = Date.from(Instant.ofEpochMilli(1615569460770L));
        Date dateNoMilliseconds = Date.from(Instant.ofEpochMilli(1615569460770L).truncatedTo(ChronoUnit.SECONDS));

        return Stream.of(
          Arguments.of(date, "2021-03-12T17:17:40.770+0000"),
          Arguments.of(dateNoMilliseconds, "2021-03-12T17:17:40.000+0000")
        );
    }

    private static Stream<? extends Arguments> argumentsConvertWithException() {
        return Stream.of(
          Arguments.of((Date) null, NullPointerException.class)
        );
    }

}
