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

    public static void main(String[] args) {
        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 5);
        Document document = new Document("Sample document");
        crptApi.createDocument(document, "sampleSignature");
    }

    public void createDocument(Document document, String signature) {
        try (LockWrapper ignored = new LockWrapper(lock)) {
            if (requestCount >= requestLimit) {
                return;
            }
            System.out.println("Sending document: " + document.getDescription() + " with signature: " + signature);
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

    private static class Document {
        private final String description;

        public Document(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
