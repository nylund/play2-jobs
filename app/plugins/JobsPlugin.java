package plugins;

import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import play.Logger;
import play.Plugin;
import play.Application;
import play.libs.Akka;
import play.libs.Time.CronExpression;
import plugins.jobs.DurationParser;
import plugins.jobs.Every;
import plugins.jobs.Job;
import plugins.jobs.On;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 *
 *
 * @author Niklas Nylund
 */
public class JobsPlugin extends Plugin {

    Application app;

    public JobsPlugin(Application app) {
        this.app = app;
    }

    @SuppressWarnings("rawtypes")
    public void onStart() {
        List<String> packages = new LinkedList<String>();
        packages.add("controllers");
        packages.add("jobs");
        Set<URL> p = new HashSet<URL>();
        for (String pp: packages)
            p.addAll(ClasspathHelper.forPackage(pp));

        Reflections reflections = new Reflections(new ConfigurationBuilder().addUrls(p));
        Set<Class<? extends Job>> classes = reflections.getSubTypesOf(Job.class);
        parseAnnotations(classes);
    }

    @SuppressWarnings("rawtypes")
    private void parseAnnotations(Set<Class<? extends Job>> classes) {
        for (Class<? extends Job> j : classes) {
            Job<?> job = null;
            try {
                job = j.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            if (j.isAnnotationPresent(On.class)) {
                On onAnnotation = (On) j.getAnnotation(On.class);
                String cronExpression = onAnnotation.value();

                try {
                    new OnJob(job, new CronExpression(cronExpression));
                } catch (ParseException e) {
                    Logger.error("JobsPlugin: Failed to parse cron expression " + cronExpression
                            + " for " + j.getCanonicalName() + ". " + e.getMessage());
                }
            } else if (j.isAnnotationPresent(Every.class)) {
                Every everyAnnotation = (Every) j.getAnnotation(Every.class);
                String every = everyAnnotation.value();

                new EveryJob(job, every);
            }
        }
    }

    public void onStop() {
    }

    public boolean enabled() {
        return true;
    }

    private static abstract class RepeatableJob {
        Job<?> job;

        public RepeatableJob(Job<?> job)
        {
            this.job = job;
        }

        protected abstract long getNextDelay();

        protected Runnable asRunnable()
        {
            return new Runnable()
            {
                public void run()
                {
                    try
                    {
                        job.run();

                        long wait = getNextDelay();

                        if (wait > 0)
                            JobsPlugin.scheduleRunnable(asRunnable(), wait);
                    }
                    catch (Exception e)
                    {
                        Logger.error("Jobs: " + e.getMessage());
                    }
                }
            };
        }
    }

    private static class OnJob extends RepeatableJob {
        CronExpression cron;

        OnJob(Job<?> job, CronExpression cron) {
            super(job);
            this.cron = cron;

            scheduleRunnable(this.asRunnable(), this.getNextDelay());
        }

        @Override
        protected long getNextDelay()
        {
            return cron.getNextInterval(new Date());
        }
    }

    private static class EveryJob extends RepeatableJob {
        long nextDelay;

        EveryJob(Job<?> job, String delay) {
            super(job);
            this.nextDelay = DurationParser.parse(delay);

            scheduleRunnable(this.asRunnable(), this.getNextDelay());
        }

        @Override
        protected long getNextDelay()
        {
            return nextDelay;
        }
    }

    protected static void scheduleRunnable(Runnable runnable, long delay)
    {
        Logger.debug("Jobs: Scheduling job in " + delay + "s");
        FiniteDuration delay_ = (FiniteDuration) Duration.create(delay, TimeUnit.MILLISECONDS);
        Akka.system().scheduler().scheduleOnce(delay_,
                runnable, Akka.system().dispatcher());
    }
}