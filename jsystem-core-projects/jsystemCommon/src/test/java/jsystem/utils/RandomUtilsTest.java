package jsystem.utils;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class RandomUtilsTest {

    // A seeded Random makes tests deterministic.
    private static final long SEED = 42L;
    private Random random;

    @Before
    public void setUp() {
        random = new Random(SEED);
    }

    // ---- getRandomInt ----

    @Test
    public void getRandomInt_returnedValueIsWithinRange() {
        for (int i = 0; i < 100; i++) {
            int value = RandomUtils.getRandomInt(5, 10, new Random());
            assertTrue("Value " + value + " not in [5,10]", value >= 5 && value <= 10);
        }
    }

    @Test
    public void getRandomInt_minEqualsMaxReturnsThatValue() {
        assertEquals(7, RandomUtils.getRandomInt(7, 7, random));
    }

    // ---- getRandomFloat ----

    @Test
    public void getRandomFloat_returnedValueIsWithinRange() {
        for (int i = 0; i < 100; i++) {
            float value = RandomUtils.getRandomFloat(1.0f, 2.0f, new Random());
            assertTrue("Value " + value + " not in [1.0, 2.0]", value >= 1.0f && value <= 2.0f);
        }
    }

    // ---- getAllCharsInRange ----

    @Test
    public void getAllCharsInRange_returnsSingleRange() {
        // ASCII 65 ('A') to 67 ('C') => [A, B, C]
        ArrayList<Character> chars = RandomUtils.getAllCharsInRange(new int[][]{{65, 67}});
        assertEquals(3, chars.size());
        assertEquals(Character.valueOf('A'), chars.get(0));
        assertEquals(Character.valueOf('C'), chars.get(2));
    }

    @Test
    public void getAllCharsInRange_returnsMultipleRanges() {
        // Range [65,65] and [67,67] => [A, C]
        ArrayList<Character> chars = RandomUtils.getAllCharsInRange(new int[][]{{65, 65}, {67, 67}});
        assertEquals(2, chars.size());
    }

    // ---- getRandomAlphabet ----

    @Test
    public void getRandomAlphabet_returnedCharIsAlphabetic() {
        for (int i = 0; i < 50; i++) {
            char c = RandomUtils.getRandomAlphabet(' ', true, new Random());
            assertTrue("Expected alphabetic char, got: " + c, Character.isLetter(c));
        }
    }

    @Test
    public void getRandomAlphabet_caseSensitive_excludesGivenChar() {
        // Run many times; the returned char must never equal 'A'.
        char excluded = 'A';
        for (int i = 0; i < 200; i++) {
            char c = RandomUtils.getRandomAlphabet(excluded, true, new Random(i));
            assertNotEquals("Should not return excluded char", excluded, c);
        }
    }

    @Test
    public void getRandomAlphabet_caseInsensitive_excludesBothCases() {
        // Returned char must be neither 'A' nor 'a'.
        for (int i = 0; i < 200; i++) {
            char c = RandomUtils.getRandomAlphabet('A', false, new Random(i));
            assertTrue("Should not return 'A' or 'a'", c != 'A' && c != 'a');
        }
    }

    // ---- getRandomDigit ----

    @Test
    public void getRandomDigit_returnedCharIsDigit() {
        for (int i = 0; i < 50; i++) {
            char c = RandomUtils.getRandomDigit(' ', new Random());
            assertTrue("Expected digit, got: " + c, Character.isDigit(c));
        }
    }

    @Test
    public void getRandomDigit_excludesGivenDigit() {
        // With 9 possible alternatives, running 200 times should never return '5'.
        for (int i = 0; i < 200; i++) {
            char c = RandomUtils.getRandomDigit('5', new Random(i));
            assertNotEquals('5', c);
        }
    }

    // ---- getSeveralRandomInts (ranges + amount) ----

    @Test
    public void getSeveralRandomInts_rangesAmount_returnedSizeMatchesRequest() {
        int[] result = RandomUtils.getSeveralRandomInts(new int[][]{{1, 10}}, 5, random);
        assertEquals(5, result.length);
    }

    @Test
    public void getSeveralRandomInts_rangesAmount_allValuesWithinRanges() {
        int[] result = RandomUtils.getSeveralRandomInts(new int[][]{{1, 10}}, 10, new Random());
        for (int v : result) {
            assertTrue("Value " + v + " not in [1,10]", v >= 1 && v <= 10);
        }
    }

    @Test
    public void getSeveralRandomInts_rangesAmount_noRepeats() {
        int[] result = RandomUtils.getSeveralRandomInts(new int[][]{{1, 10}}, 10, random);
        Set<Integer> seen = new HashSet<>();
        for (int v : result) {
            assertTrue("Duplicate value: " + v, seen.add(v));
        }
    }

    @Test
    public void getSeveralRandomInts_amountLargerThanPool_returnsAllPoolValues() {
        // Pool is {1,2,3}; requesting 10 should return only 3 values.
        int[] result = RandomUtils.getSeveralRandomInts(new int[][]{{1, 3}}, 10, random);
        assertEquals(3, result.length);
    }

    // ---- getSeveralRandomInts (min/max/amount) ----

    @Test
    public void getSeveralRandomInts_minMax_correctSize() {
        int[] result = RandomUtils.getSeveralRandomInts(1, 5, 3, random);
        assertEquals(3, result.length);
    }

    // ---- getRandomizedIntGroup ----

    @Test
    public void getRandomizedIntGroup_containsAllValues() {
        int[] result = RandomUtils.getRandomizedIntGroup(1, 5, random);
        assertEquals(5, result.length);
        int[] sorted = Arrays.copyOf(result, result.length);
        Arrays.sort(sorted);
        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, sorted);
    }

    // ---- randomizeGroup ----

    @Test
    public void randomizeGroup_returnsSameLengthAndElements() {
        Object[] original = {"a", "b", "c", "d"};
        Object[] shuffled = RandomUtils.randomizeGroup(original, random);
        assertEquals(original.length, shuffled.length);
        // Every element of original must appear in shuffled.
        Set<Object> originalSet = new HashSet<>(Arrays.asList(original));
        for (Object o : shuffled) {
            assertTrue("Unexpected element: " + o, originalSet.contains(o));
        }
    }
}
