import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CrptApi {
    private final int requestLimit;
    private final long intervalInMillis;
    private final ScheduledExecutorService scheduler;
    private final Lock lock;
    private int requestCount;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestLimit = requestLimit;
        this.intervalInMillis = timeUnit.toMillis(1);
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.lock = new ReentrantLock();
        this.requestCount = 0;
        scheduleRequestCountReset();
    }

    public void createDocument(String document, String signature) {
        try (LockWrapper ignored = new LockWrapper(lock)) {
            if (requestCount >= requestLimit) {
                return;
            }
            System.out.println("Sending document: " + document + " with signature: " + signature);
            requestCount++;
        }
    }

    private void resetRequestCount() {
        try (LockWrapper ignored = new LockWrapper(lock)) {
            requestCount = 0;
        }
    }

    private void scheduleRequestCountReset() {
        scheduler.scheduleAtFixedRate(this::resetRequestCount, intervalInMillis, intervalInMillis, TimeUnit.MILLISECONDS);
    }

    private static class LockWrapper implements AutoCloseable {
        private final Lock lock;

        public LockWrapper(Lock lock) {
            this.lock = lock;
            this.lock.lock();
        }

        @Override
        public void close() {
            lock.unlock();
        }
    }
}
