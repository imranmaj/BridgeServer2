package org.sagebionetworks.bridge.services;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Rate limiter using the "token bucket" strategy. Resources to be rate limited
 * should be associated with a unique key, and multiple resources can be rate
 * limited simultaneously and individually.
 */
@Component
public class RateLimiter {
    private long initialTokens;
    // The maximum number of tokens that can be stored in each bucket. This is also
    // the default number of tokens in each bucket.
    private long maximumTokens;
    // The time between token refills for every bucket in seconds.
    private long refillIntervalSeconds;
    // The number of tokens that are placed into each bucket during a refill.
    private long refillAmount;
    // Collection which maps resource keys to buckets (represented by a number
    // of tokens in the corresponding bucket and the time at which the bucket was
    // last refilled).
    private Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Representation of a bucket in a token bucket rate limiter.
     */
    class Bucket {
        private long tokens;
        private Instant lastRefill;

        /**
         * Class constructor specifying tokens and lastRefill.
         * 
         * @param tokens     The number of tokens in this bucket.
         * @param lastRefill The time when the bucket was last refilled.
         */
        public Bucket(long tokens, Instant lastRefill) {
            this.tokens = tokens;
            this.lastRefill = lastRefill;
        }

        /**
         * Gets the number of tokens in this bucket.
         * 
         * @return The number of tokens in this bucket.
         */
        public long getTokens() {
            return tokens;
        }

        /**
         * Sets the number of tokens in this bucket.
         * 
         * @param tokens The new number of tokens in this bucket.
         */
        public void setTokens(long tokens) {
            this.tokens = tokens;
        }

        /**
         * Gets the time when the bucket was last refilled.
         * 
         * @return The time when the bucket was last refilled.
         */
        public Instant getLastRefill() {
            return lastRefill;
        }

        /**
         * Sets the time when the bucket was last refilled.
         * 
         * @param lastRefill The new time when the bucket was last refilled.
         */
        public void setLastRefill(Instant lastRefill) {
            this.lastRefill = lastRefill;
        }
    }

    /**
     * Class constructor specifying initialTokens, maximumTokens,
     * refillIntervalSeconds, and refillAmount.
     * 
     * @param initialTokens         The initial number of tokens stored in each
     *                              bucket.
     * @param maximumTokens         The maximum number of tokens that can be stored
     *                              in each bucket.
     * @param refillIntervalSeconds The time between token refills for every
     *                              bucket in seconds.
     * @param refillAmount          The number of tokens that are placed into
     *                              each bucket during a refill.
     */
    public RateLimiter(long initialTokens, long maximumTokens, long refillIntervalSeconds, long refillAmount) {
        this.initialTokens = initialTokens;
        this.maximumTokens = maximumTokens;
        this.refillIntervalSeconds = refillIntervalSeconds;
        this.refillAmount = refillAmount;
    }

    /**
     * Updates tokens in the bucket specified by the key based upon the refill
     * amount and the number of refill intervals that have occurred since the last
     * refill.
     * 
     * If the bucket does not exist it will be created with initialTokens.
     * 
     * @param key The key specifying which resource's bucket to refill tokens for.
     */
    private void updateTokens(String key) {
        buckets.putIfAbsent(key, new Bucket(initialTokens, Instant.now()));

        Bucket bucket = buckets.get(key);
        long currentTokens = bucket.getTokens();
        Instant lastRefill = bucket.getLastRefill();

        long secondsSinceLastRefill = Instant.now().getEpochSecond() - lastRefill.getEpochSecond();
        long refillsCount = secondsSinceLastRefill / refillIntervalSeconds;
        long tokensToRefill = (long) refillsCount * refillAmount;

        bucket.setTokens(Math.min(maximumTokens, currentTokens + tokensToRefill));
        // It's not just Instant.now() because we want to save the time between the last
        // refill and now.
        bucket.setLastRefill(lastRefill.plusSeconds(refillsCount * refillIntervalSeconds));
    }

    /**
     * Checks whether a resource can be obtained using the specified key.
     * 
     * @param key            The key indicating the resource trying to be acquired.
     * @param tokenDecrement The number of tokens to decrement from the bucket for
     *                       that resource.
     * @return A boolean determining whether the resource can be acquired (true if
     *         it can be acquired, false if it cannot).
     */
    public boolean tryAcquireResource(String key, long tokenDecrement) {
        updateTokens(key);

        Bucket bucket = buckets.get(key);
        long currentTokens = bucket.getTokens();
        if (currentTokens >= tokenDecrement) {
            bucket.setTokens(currentTokens - tokenDecrement);
            return true;
        }
        return false;
    }
}
