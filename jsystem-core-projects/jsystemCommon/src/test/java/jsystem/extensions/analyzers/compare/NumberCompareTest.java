package jsystem.extensions.analyzers.compare;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for NumberCompare.
 *
 * Pattern: set testAgainst (the value to check), call analyze(), then
 * inspect getStatus() and getTitle().
 */
public class NumberCompareTest {

    // ---- EQUAL with percentage error ----

    @Test
    public void equal_percentageError_valueExactlyMatchPasses() {
        // 0% error: value must be exactly 100.
        NumberCompare nc = new NumberCompare(NumberCompare.compareOption.EQUAL, 100.0, 0.0);
        nc.setTestAgainst(100.0);
        nc.analyze();
        assertTrue("Exact match should pass", nc.getStatus());
    }

    @Test
    public void equal_percentageError_valueWithinErrorBandPasses() {
        // 10% error: 100 * (1-0.1) = 90, 100 * (1+0.1) = 110   value=105 in range
        NumberCompare nc = new NumberCompare(NumberCompare.compareOption.EQUAL, 100.0, 0.1);
        nc.setTestAgainst(105.0);
        nc.analyze();
        assertTrue("Value within 10% band should pass", nc.getStatus());
    }

    @Test
    public void equal_percentageError_valueOutsideErrorBandFails() {
        // 10% error: max=110; value=120 is outside
        NumberCompare nc = new NumberCompare(NumberCompare.compareOption.EQUAL, 100.0, 0.1);
        nc.setTestAgainst(120.0);
        nc.analyze();
        assertFalse("Value outside 10% band should fail", nc.getStatus());
    }

    // ---- EQUAL with difference error ----

    @Test
    public void equal_differenceError_valueWithinDifferencePasses() {
        // difference 5: [95, 105]; value=103
        NumberCompare nc = new NumberCompare(NumberCompare.compareOption.EQUAL, 100.0, 5L);
        nc.setTestAgainst(103.0);
        nc.analyze();
        assertTrue("Value within difference band should pass", nc.getStatus());
    }

    @Test
    public void equal_differenceError_valueOutsideDifferenceFails() {
        // difference 5: [95, 105]; value=110
        NumberCompare nc = new NumberCompare(NumberCompare.compareOption.EQUAL, 100.0, 5L);
        nc.setTestAgainst(110.0);
        nc.analyze();
        assertFalse("Value outside difference band should fail", nc.getStatus());
    }

    // ---- LESS ----

    @Test
    public void less_valueStrictlyLessThanMaxPasses() {
        // 0% error: max = 50; value = 30 < 50
        NumberCompare nc = new NumberCompare(NumberCompare.compareOption.LESS, 50.0, 0.0);
        nc.setTestAgainst(30.0);
        nc.analyze();
        assertTrue("30 < 50 should pass LESS", nc.getStatus());
    }

    @Test
    public void less_valueEqualToMaxFails() {
        // max = 50; value = 50 is NOT strictly less
        NumberCompare nc = new NumberCompare(NumberCompare.compareOption.LESS, 50.0, 0.0);
        nc.setTestAgainst(50.0);
        nc.analyze();
        assertFalse("50 < 50 should fail LESS", nc.getStatus());
    }

    @Test
    public void less_valueGreaterThanMaxFails() {
        NumberCompare nc = new NumberCompare(NumberCompare.compareOption.LESS, 50.0, 0.0);
        nc.setTestAgainst(60.0);
        nc.analyze();
        assertFalse("60 < 50 should fail LESS", nc.getStatus());
    }

    // ---- GREATER ----

    @Test
    public void greater_valueStrictlyGreaterThanMinPasses() {
        // 0% error: min = 20; value = 30 > 20
        NumberCompare nc = new NumberCompare(NumberCompare.compareOption.GREATER, 20.0, 0.0);
        nc.setTestAgainst(30.0);
        nc.analyze();
        assertTrue("30 > 20 should pass GREATER", nc.getStatus());
    }

    @Test
    public void greater_valueEqualToMinFails() {
        NumberCompare nc = new NumberCompare(NumberCompare.compareOption.GREATER, 20.0, 0.0);
        nc.setTestAgainst(20.0);
        nc.analyze();
        assertFalse("20 > 20 should fail GREATER", nc.getStatus());
    }

    @Test
    public void greater_valueLessThanMinFails() {
        NumberCompare nc = new NumberCompare(NumberCompare.compareOption.GREATER, 20.0, 0.0);
        nc.setTestAgainst(10.0);
        nc.analyze();
        assertFalse("10 > 20 should fail GREATER", nc.getStatus());
    }

    // ---- LESS_OR_EQUAL ----

    @Test
    public void lessOrEqual_valueEqualToMaxPasses() {
        NumberCompare nc = new NumberCompare(NumberCompare.compareOption.LESS_OR_EQUAL, 50.0, 0.0);
        nc.setTestAgainst(50.0);
        nc.analyze();
        assertTrue("50 <= 50 should pass LESS_OR_EQUAL", nc.getStatus());
    }

    @Test
    public void lessOrEqual_valueStrictlyLessPassess() {
        NumberCompare nc = new NumberCompare(NumberCompare.compareOption.LESS_OR_EQUAL, 50.0, 0.0);
        nc.setTestAgainst(40.0);
        nc.analyze();
        assertTrue("40 <= 50 should pass LESS_OR_EQUAL", nc.getStatus());
    }

    @Test
    public void lessOrEqual_valueGreaterThanMaxFails() {
        NumberCompare nc = new NumberCompare(NumberCompare.compareOption.LESS_OR_EQUAL, 50.0, 0.0);
        nc.setTestAgainst(60.0);
        nc.analyze();
        assertFalse("60 <= 50 should fail LESS_OR_EQUAL", nc.getStatus());
    }

    // ---- GREAT_OR_EQUAL ----

    @Test
    public void greatOrEqual_valueEqualToMinPasses() {
        NumberCompare nc = new NumberCompare(NumberCompare.compareOption.GREAT_OR_EQUAL, 20.0, 0.0);
        nc.setTestAgainst(20.0);
        nc.analyze();
        assertTrue("20 >= 20 should pass GREAT_OR_EQUAL", nc.getStatus());
    }

    @Test
    public void greatOrEqual_valueGreaterThanMinPasses() {
        NumberCompare nc = new NumberCompare(NumberCompare.compareOption.GREAT_OR_EQUAL, 20.0, 0.0);
        nc.setTestAgainst(30.0);
        nc.analyze();
        assertTrue("30 >= 20 should pass GREAT_OR_EQUAL", nc.getStatus());
    }

    @Test
    public void greatOrEqual_valueLessThanMinFails() {
        NumberCompare nc = new NumberCompare(NumberCompare.compareOption.GREAT_OR_EQUAL, 20.0, 0.0);
        nc.setTestAgainst(10.0);
        nc.analyze();
        assertFalse("10 >= 20 should fail GREAT_OR_EQUAL", nc.getStatus());
    }

    // ---- getTitle is set after analyze ----

    @Test
    public void analyze_setsTitleAfterCall() {
        NumberCompare nc = new NumberCompare(NumberCompare.compareOption.EQUAL, 100.0, 0.0);
        nc.setTestAgainst(100.0);
        nc.analyze();
        assertNotNull("Title should be set after analyze()", nc.getTitle());
        assertFalse("Title should not be empty", nc.getTitle().isEmpty());
    }

    // ---- integer string testAgainst ----

    @Test
    public void testAgainst_integerStringParsedCorrectly() {
        // setTestAgainst can be any Object whose toString() returns a parseable double.
        NumberCompare nc = new NumberCompare(NumberCompare.compareOption.EQUAL, 42.0, 0.0);
        nc.setTestAgainst("42");
        nc.analyze();
        assertTrue("String '42' should equal 42.0", nc.getStatus());
    }
}
