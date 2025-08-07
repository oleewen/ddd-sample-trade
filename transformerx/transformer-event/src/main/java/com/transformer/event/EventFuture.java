package com.transformer.event;

import com.google.common.base.Preconditions;

import java.util.concurrent.*;

/**
 * @author only
 *         Date 2015/8/18.
 */
public class EventFuture implements Future<String> {
    static final int NEW = 0;
    static final int RUNNING = 1;
    static final int COMPLETION = 2;
    static final int CANCELLED = 3;
    static final int EXCEPTIONAL = 4;
    private volatile int state = NEW;
    private volatile String message;

    @Override
    public boolean isCancelled() {
        return state == CANCELLED;
    }

    @Override
    public boolean isDone() {
        return state == COMPLETION;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        setCancel("event has bean cancelled");
        return true;
    }

    @Override
    public String get() throws InterruptedException, ExecutionException {
        int s = state;
        if (s <= RUNNING) {
            s = awaitDone(false, 0L);
        }
        return report(s);
    }

    @Override
    public String get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        Preconditions.checkNotNull(unit, "unit is required");

        int s = state;
        if (s <= RUNNING && (s = awaitDone(true, unit.toNanos(timeout))) <= RUNNING) {
            throw new TimeoutException("timeout:" + timeout);
        }
        return report(s);
    }

    private String report(int s) throws ExecutionException {
        if (s == COMPLETION) {
            return message;
        }
        if (s >= COMPLETION) {
            throw new ExecutionException(message, null);
        }

        throw new ExecutionException("event handle timeout", null);
    }

    private int awaitDone(boolean timed, long nanos) throws InterruptedException {
        final long deadline = System.nanoTime() + nanos;

        while (true) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            int s = state;
            // done
            if (s > RUNNING) {
                return s;
            }
            // wait for complete
            else if (s == RUNNING) {
                Thread.yield();
            }
            // wait for timeout
            else if (timed) {
                nanos = deadline - System.nanoTime();
                if (nanos <= 0L) {
                    return state;
                }
                Thread.yield();
            }
            // not timeout
            else {
                Thread.yield();
            }
        }
    }

    public void setFailure(String message) {
        setStatus(COMPLETION, message);
    }

    public void setSuccess(String message) {
        setStatus(COMPLETION, message);
    }

    public void setException(String message) {
        setStatus(EXCEPTIONAL, message);
    }

    public void setCancel(String message) {
        this.setStatus(CANCELLED, message);
    }

    private void setStatus(int state, String message) {
        this.state = state;
        this.message = message;
    }
}
