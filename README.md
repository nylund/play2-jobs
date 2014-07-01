play2-jobs
==========

Job helper for Play framework 2.2.x Java.

This plugin is inspired by the Job API in Play 1.2.x. The goal is to make it easy
to setup recurring jobs and manually start jobs when needed.

Usage
=====

To use jobs extend `plugins.jobs.Job<T>` in a class, you can use the `@On` or `@Every` annotations to
setup recurring jobs or start the jobs manually calling `Job.now()` or `Job.in("5min")`.

Annotations
-----------

1. `@On("0 0 0 * * ?")`
..* Use cron style expressions
2. `@Every("30min")`
..* Examples: "1h", "30min", "15s", "1h 15min"...

Example
-------

```
import plugins.jobs.Job;
import plugins.jobs.On;

@On("0 0 0 * * ?")
public class MyJob extends Job<Void>
{
    @Override
    public Void run() throws Exception
    {
        // Implement job here...
        return null;
    }
}
```

```
import plugins.jobs.Job;
import plugins.jobs.Every;

@Every("30min")
public class EveryJob extends Job<Void>
{
    @Override
    public Void run() throws Exception
    {
        // Implement job here...
        return null;
    }
}

```


Setup
=====

1. Local setup
..* Checkout the repository and publish locally (start play and run `compile` and `publishLocal`).
..* Add dependency to your app by adding `"default" % "play2-jobs_2.10" % "0.1"` to build.sbt.

