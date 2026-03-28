package jsystem.utils;

import static org.junit.Assert.*;
import java.util.Vector;
import org.junit.Before;
import org.junit.Test;

public class TimeStopperTest {

    private TimeStopper stopper;

    @Before
    public void setUp() {
        stopper = new TimeStopper();
        stopper.init();
    }

    // ---- getTimeDiff ----

    @Test
    public void getTimeDiff_isNonNegativeAfterInit() {
        long diff = stopper.getTimeDiff();
        assertTrue("Time diff should be >= 0, was: " + diff, diff >= 0);
    }

    @Test
    public void getTimeDiff_increasesOverTime() throws InterruptedException {
        long first = stopper.getTimeDiff();
        Thread.sleep(50);
        long second = stopper.getTimeDiff();
        assertTrue("Second diff should be >= first", second >= first);
    }

    // ---- getTimeDiffInSec ----

    @Test
    public void getTimeDiffInSec_isNonNegative() {
        assertTrue(stopper.getTimeDiffInSec() >= 0f);
    }

    @Test
    public void getTimeDiffInSec_consistentWithMillis() throws InterruptedException {
        Thread.sleep(100);
        float sec = stopper.getTimeDiffInSec();
        long ms  = stopper.getTimeDiff();
        // Both should be around 100ms/0.1s; verify the relationship: sec ≈ ms/1000
        assertEquals((float) ms / 1000f, sec, 0.05f);
    }

    // ---- getTimeDiffInMin ----

    @Test
    public void getTimeDiffInMin_isNonNegative() {
        assertTrue(stopper.getTimeDiffInMin() >= 0f);
    }

    // ---- lap ----

    @Test
    public void lap_returnsNonNegativeMillis() throws InterruptedException {
        Thread.sleep(20);
        long lapTime = stopper.lap();
        assertTrue("Lap time should be >= 0, was: " + lapTime, lapTime >= 0);
    }

    @Test
    public void lap_resetsTimer() throws InterruptedException {
        Thread.sleep(50);
        stopper.lap();
        // After lap, timer resets; immediately-measured diff should be small.
        long diffAfterLap = stopper.getTimeDiff();
        assertTrue("Timer should reset after lap; diff was: " + diffAfterLap, diffAfterLap < 50);
    }

    @Test
    public void lap_storesValueInAllTimes() throws InterruptedException {
        Thread.sleep(10);
        long lapTime = stopper.lap();
        Vector<Long> times = stopper.getAllTimes();
        assertEquals(1, times.size());
        assertEquals(lapTime, (long) times.get(0));
    }

    @Test
    public void multipleLaps_allTimesHasCorrectCount() throws InterruptedException {
        stopper.lap();
        Thread.sleep(10);
        stopper.lap();
        Thread.sleep(10);
        stopper.lap();
        assertEquals(3, stopper.getAllTimes().size());
    }

    // ---- getAllTimesInSeconds ----

    @Test
    public void getAllTimesInSeconds_returnsCorrectConversion() throws InterruptedException {
        Thread.sleep(100);
        long ms = stopper.lap();
        Vector<Float> seconds = stopper.getAllTimesInSeconds();
        assertEquals(1, seconds.size());
        assertEquals((float) ms / 1000f, seconds.get(0), 0.05f);
    }

    // ---- getAllTimesInMinutes ----

    @Test
    public void getAllTimesInMinutes_returnsCorrectConversion() throws InterruptedException {
        long ms = stopper.lap();
        Vector<Float> minutes = stopper.getAllTimesInMinutes();
        assertEquals(1, minutes.size());
        assertEquals((float) ms / (1000f * 60f), minutes.get(0), 0.001f);
    }

    // ---- init resets state ----

    @Test
    public void init_clearsAllTimes() throws InterruptedException {
        stopper.lap();
        stopper.lap();
        stopper.init();
        assertTrue("allTimes should be empty after re-init", stopper.getAllTimes().isEmpty());
    }
}
