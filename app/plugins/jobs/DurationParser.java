package plugins.jobs;

import org.joda.time.DateTime;
import org.joda.time.format.PeriodFormatterBuilder;

public class DurationParser
{
    /**
     *
     * @param duration
     * @return Duration in milliseconds
     */
    public static long parse(String duration)
    {
        PeriodFormatterBuilder p = new PeriodFormatterBuilder();

        if (duration.contains("d"))
            p.appendDays().appendSuffix("d").appendSeparatorIfFieldsAfter(" ");

        if (duration.contains("h"))
            p.appendHours().appendSuffix("h").appendSeparatorIfFieldsAfter(" ");

        if (duration.contains("min"))
            p.appendMinutes().appendSuffix("min").appendSeparatorIfFieldsAfter(" ");

        if (duration.contains("s"))
            p.appendSeconds().appendSuffix("s").appendSeparatorIfFieldsAfter(" ");

        return p.toFormatter().parsePeriod(duration).toDurationFrom(new DateTime()).getMillis();
    }
}
