package tests.other;

import org.junit.jupiter.api.Test;
import taskscheduler.java.other.Duration;


import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;

public class DurationTest {

    @Test
    public void testConstructorAndGetDuration() {
        BigInteger bi = new BigInteger("1000");
        Duration duration = new Duration(bi);
        assertEquals(bi, duration.getDuration(), "Constructor or getDuration does not set or return correct value.");
    }

    @Test
    public void testSetDuration() {
        BigInteger bi = new BigInteger("1000");
        Duration duration = new Duration(BigInteger.ZERO);
        duration.setDuration(bi);
        assertEquals(bi, duration.getDuration(), "setDuration does not properly update the duration.");
    }

    @Test
    public void testAddDuration() {
        BigInteger initial = new BigInteger("1000");
        BigInteger toAdd = new BigInteger("500");
        BigInteger expected = new BigInteger("1500");
        Duration duration = new Duration(initial);
        duration.addDuration(toAdd);
        assertEquals(expected, duration.getDuration(), "addDuration does not correctly add values.");
    }

    @Test
    public void testSubtractDuration() {
        BigInteger initial = new BigInteger("1000");
        BigInteger toSubtract = new BigInteger("500");
        BigInteger expected = new BigInteger("500");
        Duration duration = new Duration(initial);
        duration.subtractDuration(toSubtract);
        assertEquals(expected, duration.getDuration(), "subtractDuration does not correctly subtract values.");
    }

    @Test
    public void testCompareDurations() {
        Duration d1 = new Duration(new BigInteger("1000"));
        Duration d2 = new Duration(new BigInteger("500"));
        Duration d3 = new Duration(new BigInteger("1500"));
        assertTrue(d1.compareDurations(d2) > 0, "d1 should be greater than d2");
        assertTrue(d1.compareDurations(d3) < 0, "d1 should be less than d3");
        assertEquals(0, d1.compareDurations(new Duration(new BigInteger("1000"))), "d1 should be equal to new Duration(1000)");
    }

    @Test
    public void testOfMillis() {
        long millis = 1000L;
        BigInteger expected = BigInteger.valueOf(millis);
        Duration duration = Duration.ofMillis(millis);
        assertEquals(expected, duration.getDuration(), "ofMillis does not correctly convert milliseconds to Duration.");
    }
}
