package plugins.jobs;

import play.Logger;
import play.libs.F.Function0;
import play.libs.F.Promise;

/**
 * Generic job class.
 *
 * @author Niklas Nylund
 *
 * @param <T> Result type of the job.
 */
public abstract class Job<T>
{
    public abstract T run() throws Exception;

    /**
     * Run job now.
     * @return Result of run().
     */
    public Promise<T> now() {
        return in(null);
    }

    /**
     * Run job with delay ("1h", "30min", "15s", "1h 15min"...)
     * @param delay
     * @return
     */
    public Promise<T> in(final String delay) {
        return Promise.promise(new Function0<T>() {
            public T apply() throws Exception {

                Logger.debug("Running job via now()");

                if (delay != null)
                    Thread.sleep(DurationParser.parse(delay));
                return run();
            }
        });
    }
}
