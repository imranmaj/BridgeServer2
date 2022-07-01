package org.sagebionetworks.bridge.services;

import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

/**
 * Tests the RateLimiter
 */
public class RateLimiterTest {
    /**
     * Checks whether the RateLimiter correctly allows a download after a refill
     * which was not allowed before the refill.
     * 
     * @throws InterruptedException
     */
    @Test
    public void validAfterRefill() throws InterruptedException {
        RateLimiter rateLimiter = new RateLimiter(
                1_000, // 1 KB
                100_000_000, // 10 MB
                2, // 2s
                1_000_000 // 1 MB
        );

        assertFalse(rateLimiter.tryAcquireResource("foo", 10_000),
                "RateLimiter should have rejected 10 KB download with initial of 1 KB");

        Thread.sleep(3000);
        assertTrue(rateLimiter.tryAcquireResource("foo", 10_000),
                "RateLimiter should have allowed 10 KB download with initial of 1 KB after refill of 1 MB");
    }

    /**
     * Checks whether the RateLimiter allows a small file within the limit
     * immediately after rejecting a large file
     */
    @Test
    public void largeFileThenSmallFile() {
        RateLimiter rateLimiter = new RateLimiter(
                1_000, // 1 KB
                100_000_000, // 10 MB
                3600, // 1 hr
                1_000_000 // 1 MB
        );

        assertFalse(rateLimiter.tryAcquireResource("foo", 10_000),
                "RateLimiter should have rejected 10 KB download with initial of 1 KB");
        assertTrue(rateLimiter.tryAcquireResource("foo", 5),
                "RateLimiter should have allowed 5 B download with initial of 1 KB");
    }

    /**
     * Checks whether the RateLimiter rejects a large file immediately after
     * allowing a small file
     */
    @Test
    public void smallFileThenLargeFile() {
        RateLimiter rateLimiter = new RateLimiter(
                1_000, // 1 KB
                100_000_000, // 10 MB
                3600, // 1 hr
                1_000_000 // 1 MB
        );

        assertTrue(rateLimiter.tryAcquireResource("foo", 5),
                "RateLimiter should have allowed 5 B download with initial of 1 KB");
        assertFalse(rateLimiter.tryAcquireResource("foo", 10_000),
                "RateLimiter should have rejected 10 KB download with initial of 1 KB");
    }

    /**
     * Checks whether the RateLimiter allows a large number of small files but still
     * rejects the file that goes over the allowed limit
     */
    @Test
    public void manySmallFiles() {
        RateLimiter rateLimiter = new RateLimiter(
                1_000_000, // 1 MB
                100_000_000, // 10 MB
                3600, // 1 hr
                1_000_000 // 1 MB
        );

        for (int i = 0; i < 100; i++) {
            assertTrue(rateLimiter.tryAcquireResource("foo", 10_000),
                    "RateLimiter should have allowed 100 10 KB downloads with initial of 1 MB");
        }

        assertFalse(rateLimiter.tryAcquireResource("foo", 10_000),
                "RateLimiter should have rejected 101st 10 KB download with initial of 1 MB");
    }

    /**
     * Checks whether the RateLimiter can handle multiple users and correctly allows
     * and rejects downloads indepedently of the allowals and rejections of other
     * users
     */
    @Test
    public void multipleUsersValid() {
        RateLimiter rateLimiter = new RateLimiter(
                1_000_000, // 1 MB
                100_000_000, // 10 MB
                3600, // 1 hr
                1_000_000 // 1 MB
        );

        assertTrue(rateLimiter.tryAcquireResource("foo", 750_000),
                "RateLimiter should have allowed 750 KB download by foo with initial of 1 MB");
        assertTrue(rateLimiter.tryAcquireResource("bar", 750_000),
                "RateLimiter should have allowed 750 KB download by bar with initial of 1 MB");
        assertTrue(rateLimiter.tryAcquireResource("foo", 100_000),
                "RateLimiter should have allowed 100 KB download after 750 KB download by foo with initial of 1 MB");
        assertFalse(rateLimiter.tryAcquireResource("bar", 750_000),
                "RateLimiter should have rejected 2nd 750 KB download by bar with initial of 1 MB even after allowing download by another user");
        assertTrue(rateLimiter.tryAcquireResource("foo", 100_000),
                "RateLimiter should have allowed 2nd 100 KB download after 750 KB download by foo with initial of 1 MB "
                        + "even after rejecting download by another user");
    }
}
