import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BulkheadExample {

    // Create thread pools for each service/component
    private final ExecutorService serviceAThreadPool = Executors.newFixedThreadPool(10); // Limit to 10 concurrent tasks
    private final ExecutorService serviceBThreadPool = Executors.newFixedThreadPool(5);  // Limit to 5 concurrent tasks

    public Future<String> callServiceA() {
        return serviceAThreadPool.submit(() -> {
            // Simulate service A work
            Thread.sleep(2000); // Simulate a delay
            return "Service A response";
        });
    }

    public Future<String> callServiceB() {
        return serviceBThreadPool.submit(() -> {
            // Simulate service B work
            Thread.sleep(1000); // Simulate a delay
            return "Service B response";
        });
    }

    public void shutdown() {
        serviceAThreadPool.shutdown();
        serviceBThreadPool.shutdown();
    }

    public static void main(String[] args) throws Exception {
        BulkheadExample bulkheadExample = new BulkheadExample();

        // Submit tasks to each service
        Future<String> responseA = bulkheadExample.callServiceA();
        Future<String> responseB = bulkheadExample.callServiceB();

        // Get the responses
        System.out.println("Response from Service A: " + responseA.get());
        System.out.println("Response from Service B: " + responseB.get());

        // Shutdown the thread pools
        bulkheadExample.shutdown();
    }
}
