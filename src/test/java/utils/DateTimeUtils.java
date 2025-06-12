package utils;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.TimeZone;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DateTimeUtils {

    public static LocalDateTime parseTimeToCurrentTimeZone(String dateTime) {
        return ZonedDateTime.parse(dateTime)
                .withZoneSameInstant(TimeZone.getDefault().toZoneId()).toLocalDateTime();
    }

    public static void verifyDateTime(LocalDateTime before, LocalDateTime after, LocalDateTime actual) {
        assertThat(actual.isAfter(before), equalTo(true));
        assertThat(actual.isBefore(after), equalTo(true));
    }
}
