package plugins.jobs;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Every
{
    /**
     * Run job repeatedly ("1h", "30min", "15s", "1h 15min"...)
     */
    String value();
}
