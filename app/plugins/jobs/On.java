package plugins.jobs;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface On
{
    /**
     * Schedule job using cron expression.
     */
    String value();
}
