package com.sal.loadtest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Load test for SAL Service using Virtual Threads (Java 21+).
 * 
 * Run with: java SalLoadTest.java [baseUrl] [concurrency] [totalRequests]
 */
public class SalLoadTest {

    private static final String DEFAULT_BASE_URL = "http://localhost:8081";
    private static final int DEFAULT_CONCURRENCY = 50;
    private static final int DEFAULT_TOTAL_REQUESTS = 1000;

    private final String baseUrl;
    private final int concurrency;
    private final int totalRequests;
    private final HttpClient httpClient;

    // Metrics
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final ConcurrentLinkedQueue<Long> latencies = new ConcurrentLinkedQueue<>();

    public SalLoadTest(String baseUrl, int concurrency, int totalRequests) {
        this.baseUrl = baseUrl;
        this.concurrency = concurrency;
        this.totalRequests = totalRequests;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    public static void main(String[] args) {
        String baseUrl = args.length > 0 ? args[0] : DEFAULT_BASE_URL;
        int concurrency = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_CONCURRENCY;
        int totalRequests = args.length > 2 ? Integer.parseInt(args[2]) : DEFAULT_TOTAL_REQUESTS;

        System.out.println("=".repeat(60));
        System.out.println("SAL Service Load Test (Virtual Threads)");
        System.out.println("=".repeat(60));
        System.out.printf("Base URL: %s%n", baseUrl);
        System.out.printf("Concurrency: %d virtual threads%n", concurrency);
        System.out.printf("Total Requests: %d%n", totalRequests);
        System.out.println("=".repeat(60));

        SalLoadTest loadTest = new SalLoadTest(baseUrl, concurrency, totalRequests);
        
        try {
            // Warmup
            System.out.println("\nWarming up...");
            loadTest.runTest("Health Check", loadTest::healthCheck, 100, 10);
            
            // Main tests
            System.out.println("\n" + "=".repeat(60));
            System.out.println("MAIN LOAD TESTS");
            System.out.println("=".repeat(60));

            loadTest.runTest("1. Health Check", loadTest::healthCheck, totalRequests, concurrency);
            loadTest.runTest("2. Upload (FileSystem)", loadTest::uploadFileSystem, totalRequests, concurrency);
            loadTest.runTest("3. Upload (Database)", loadTest::uploadDatabase, totalRequests / 2, concurrency);
            
            // Create sample object for read tests
            String sampleUuid = loadTest.createSampleObject();
            if (sampleUuid != null) {
                loadTest.runTest("4. Get Info", () -> loadTest.getInfo(sampleUuid), totalRequests, concurrency);
                loadTest.runTest("5. Download", () -> loadTest.download(sampleUuid), totalRequests, concurrency);
                loadTest.runTest("6. List Versions", () -> loadTest.listVersions(sampleUuid), totalRequests, concurrency);
            }
            
            loadTest.runTest("7. Search", loadTest::search, totalRequests, concurrency);

            // Mixed workload
            System.out.println("\n" + "=".repeat(60));
            System.out.println("MIXED WORKLOAD (70% read, 30% write)");
            System.out.println("=".repeat(60));
            loadTest.runMixedWorkload(sampleUuid, totalRequests, concurrency);

        } catch (Exception e) {
            System.err.println("Load test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void runTest(String name, Runnable task, int requests, int threads) {
        System.out.printf("%n%s%n", name);
        System.out.println("-".repeat(40));

        successCount.set(0);
        failureCount.set(0);
        latencies.clear();

        CountDownLatch latch = new CountDownLatch(requests);
        Instant start = Instant.now();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < requests; i++) {
                executor.submit(() -> {
                    try {
                        long startTime = System.nanoTime();
                        task.run();
                        long endTime = System.nanoTime();
                        latencies.add((endTime - startTime) / 1_000_000); // Convert to ms
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Duration elapsed = Duration.between(start, Instant.now());
        printMetrics(requests, elapsed);
    }

    private void runMixedWorkload(String sampleUuid, int requests, int threads) {
        successCount.set(0);
        failureCount.set(0);
        latencies.clear();

        AtomicInteger readOps = new AtomicInteger(0);
        AtomicInteger writeOps = new AtomicInteger(0);

        CountDownLatch latch = new CountDownLatch(requests);
        Instant start = Instant.now();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < requests; i++) {
                final int idx = i;
                executor.submit(() -> {
                    try {
                        long startTime = System.nanoTime();
                        
                        // 70% reads, 30% writes
                        if (idx % 10 < 7) {
                            // Read operations
                            switch (idx % 3) {
                                case 0 -> getInfo(sampleUuid);
                                case 1 -> download(sampleUuid);
                                case 2 -> search();
                            }
                            readOps.incrementAndGet();
                        } else {
                            // Write operations
                            uploadFileSystem();
                            writeOps.incrementAndGet();
                        }
                        
                        long endTime = System.nanoTime();
                        latencies.add((endTime - startTime) / 1_000_000);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Duration elapsed = Duration.between(start, Instant.now());
        System.out.printf("Read Operations: %d, Write Operations: %d%n", readOps.get(), writeOps.get());
        printMetrics(requests, elapsed);
    }

    private void printMetrics(int requests, Duration elapsed) {
        double elapsedSeconds = elapsed.toMillis() / 1000.0;
        double rps = successCount.get() / elapsedSeconds;

        List<Long> sortedLatencies = new ArrayList<>(latencies);
        sortedLatencies.sort(Long::compareTo);

        LongSummaryStatistics stats = sortedLatencies.stream()
            .mapToLong(Long::longValue)
            .summaryStatistics();

        long p50 = sortedLatencies.isEmpty() ? 0 : sortedLatencies.get((int)(sortedLatencies.size() * 0.50));
        long p90 = sortedLatencies.isEmpty() ? 0 : sortedLatencies.get((int)(sortedLatencies.size() * 0.90));
        long p95 = sortedLatencies.isEmpty() ? 0 : sortedLatencies.get((int)(sortedLatencies.size() * 0.95));
        long p99 = sortedLatencies.isEmpty() ? 0 : sortedLatencies.get(Math.min((int)(sortedLatencies.size() * 0.99), sortedLatencies.size() - 1));

        System.out.printf("Requests:     %d total, %d success, %d failed%n", 
            requests, successCount.get(), failureCount.get());
        System.out.printf("Duration:     %.2f seconds%n", elapsedSeconds);
        System.out.printf("Throughput:   %.2f req/sec%n", rps);
        System.out.printf("Latency (ms): min=%.0f, avg=%.2f, max=%.0f%n", 
            (double)stats.getMin(), stats.getAverage(), (double)stats.getMax());
        System.out.printf("Percentiles:  p50=%d, p90=%d, p95=%d, p99=%d ms%n", p50, p90, p95, p99);
    }

    // Test operations

    private void healthCheck() {
        sendGet("/api/v1/sal/health");
    }

    private void uploadFileSystem() {
        String content = Base64.getEncoder().encodeToString(
            ("Load test content " + UUID.randomUUID()).getBytes());
        String json = String.format("""
            {
                "name": "loadtest-%s.txt",
                "ownerId": "loadtest",
                "type": "TEXT",
                "storageType": "FILESYSTEM",
                "content": "%s"
            }
            """, UUID.randomUUID(), content);
        sendPost("/api/v1/sal/objects/upload", json);
    }

    private void uploadDatabase() {
        String content = Base64.getEncoder().encodeToString(
            ("DB test " + UUID.randomUUID()).getBytes());
        String json = String.format("""
            {
                "name": "dbtest-%s.txt",
                "ownerId": "loadtest",
                "type": "TEXT",
                "storageType": "DATABASE",
                "content": "%s"
            }
            """, UUID.randomUUID(), content);
        sendPost("/api/v1/sal/objects/upload", json);
    }

    private void getInfo(String uuid) {
        sendGet("/api/v1/sal/objects/" + uuid);
    }

    private void download(String uuid) {
        sendGet("/api/v1/sal/objects/" + uuid + "/content");
    }

    private void listVersions(String uuid) {
        sendGet("/api/v1/sal/objects/" + uuid + "/versions");
    }

    private void search() {
        String json = """
            {"latestOnly":true,"page":0,"size":10}
            """;
        sendPost("/api/v1/sal/search", json);
    }

    private String createSampleObject() {
        try {
            String content = Base64.getEncoder().encodeToString("Sample content for read tests".getBytes());
            String json = String.format("""
                {
                    "name": "sample-for-reads.txt",
                    "ownerId": "loadtest",
                    "type": "TEXT",
                    "storageType": "FILESYSTEM",
                    "content": "%s"
                }
                """, content);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/sal/objects/upload"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 201) {
                // Extract UUID from response
                String body = response.body();
                int start = body.indexOf("\"salUuid\":\"") + 11;
                int end = body.indexOf("\"", start);
                return body.substring(start, end);
            }
        } catch (Exception e) {
            System.err.println("Failed to create sample object: " + e.getMessage());
        }
        return null;
    }

    private void sendGet(String path) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new RuntimeException("HTTP " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendPost(String path, String json) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new RuntimeException("HTTP " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
