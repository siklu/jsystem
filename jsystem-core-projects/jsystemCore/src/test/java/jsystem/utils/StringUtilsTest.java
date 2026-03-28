package jsystem.utils;

import static org.junit.Assert.*;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.Set;
import org.junit.Test;

public class StringUtilsTest {

    // ---- replace ----

    @Test
    public void replace_substitutesAllOccurrences() {
        assertEquals("b b b", StringUtils.replace("a a a", "a", "b"));
    }

    @Test
    public void replace_noMatchReturnsOriginal() {
        String s = "hello world";
        assertSame(s, StringUtils.replace(s, "xyz", "abc"));
    }

    // Note: replace("", lookFor, replaceWith) is not tested because StringUtils.replace
    // does not guard against empty lookFor (infinite loop in indexOf loop).

    // ---- getPackageName ----

    @Test
    public void getPackageName_returnsPackagePart() {
        assertEquals("com.example", StringUtils.getPackageName("com.example.MyClass"));
    }

    @Test
    public void getPackageName_noPackageReturnsNull() {
        assertEquals("null", StringUtils.getPackageName("MyClass"));
    }

    // ---- getClassName(String) ----

    @Test
    public void getClassName_returnsSimpleClassNameFromFqn() {
        assertEquals("MyClass", StringUtils.getClassName("com.example.MyClass"));
    }

    @Test
    public void getClassName_noPackageReturnsSelf() {
        assertEquals("MyClass", StringUtils.getClassName("MyClass"));
    }

    // ---- getClassName(String, String) ----

    @Test
    public void getClassNameWithRoot_extractsFromAbsolutePath() throws FileNotFoundException {
        String root  = "/work/src/";
        String file  = "/work/src/com/example/MyClass.class";
        assertEquals("com.example.MyClass", StringUtils.getClassName(file, root));
    }

    @Test(expected = FileNotFoundException.class)
    public void getClassNameWithRoot_throwsWhenFileNotUnderRoot() throws FileNotFoundException {
        StringUtils.getClassName("/other/com/MyClass.class", "/work/src/");
    }

    @Test(expected = FileNotFoundException.class)
    public void getClassNameWithRoot_throwsWhenNotAClassFile() throws FileNotFoundException {
        StringUtils.getClassName("/work/src/com/MyClass.java", "/work/src/");
    }

    // ---- countString ----

    @Test
    public void countString_countsNonOverlapping() {
        assertEquals(3, StringUtils.countString("aaa", "a"));
    }

    @Test
    public void countString_returnsZeroWhenNullInput() {
        assertEquals(0, StringUtils.countString(null, "a"));
        assertEquals(0, StringUtils.countString("abc", null));
    }

    @Test
    public void countString_returnsZeroWhenNothingFound() {
        assertEquals(0, StringUtils.countString("hello", "xyz"));
    }

    @Test
    public void countString_regex_countsMatches() {
        assertEquals(3, StringUtils.countString("abc abc abc", "abc", true));
    }

    @Test
    public void countString_plain_delegatesToSimpleVersion() {
        assertEquals(2, StringUtils.countString("abab", "ab", false));
    }

    // ---- firstCharToUpper / firstCharToLower ----

    @Test
    public void firstCharToUpper_capitalisesFirstChar() {
        assertEquals("Hello", StringUtils.firstCharToUpper("hello"));
    }

    @Test
    public void firstCharToUpper_nullReturnsNull() {
        assertNull(StringUtils.firstCharToUpper(null));
    }

    @Test
    public void firstCharToUpper_emptyReturnsEmpty() {
        assertEquals("", StringUtils.firstCharToUpper(""));
    }

    @Test
    public void firstCharToUpper_singleChar() {
        assertEquals("A", StringUtils.firstCharToUpper("a"));
    }

    @Test
    public void firstCharToLower_lowercasesFirstChar() {
        assertEquals("hELLO", StringUtils.firstCharToLower("HELLO"));
    }

    @Test
    public void firstCharToLower_nullReturnsNull() {
        assertNull(StringUtils.firstCharToLower(null));
    }

    // ---- hasNotAllowedSpecialCharacters ----

    @Test
    public void hasNotAllowed_trueForHash() {
        assertTrue(StringUtils.hasNotAllowedSpecialCharacters("abc#def"));
    }

    @Test
    public void hasNotAllowed_trueForPercent() {
        assertTrue(StringUtils.hasNotAllowedSpecialCharacters("100%"));
    }

    @Test
    public void hasNotAllowed_falseForCleanString() {
        assertFalse(StringUtils.hasNotAllowedSpecialCharacters("helloWorld"));
    }

    @Test
    public void hasNotAllowed_falseForNull() {
        assertFalse(StringUtils.hasNotAllowedSpecialCharacters(null));
    }

    // ---- split ----

    @Test
    public void split_splitsByDelimiter() {
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a,b,c", ","));
    }

    @Test
    public void split_nullTextReturnsEmptyArray() {
        assertEquals(0, StringUtils.split(null, ",").length);
    }

    @Test
    public void split_nullDelimReturnsEmptyArray() {
        assertEquals(0, StringUtils.split("abc", null).length);
    }

    // ---- stringArrayToSet ----

    @Test
    public void stringArrayToSet_convertsArray() {
        Set<String> set = StringUtils.stringArrayToSet(new String[]{"a", "b", "a"});
        assertEquals(2, set.size());
        assertTrue(set.contains("a"));
        assertTrue(set.contains("b"));
    }

    @Test
    public void stringArrayToSet_nullReturnsEmptySet() {
        assertTrue(StringUtils.stringArrayToSet(null).isEmpty());
    }

    // ---- mergeStringArrays ----

    @Test
    public void mergeStringArrays_mergesInOrder() {
        String[] result = StringUtils.mergeStringArrays(new String[][]{{"a", "b"}, {"c"}});
        assertArrayEquals(new String[]{"a", "b", "c"}, result);
    }

    @Test
    public void mergeStringArrays_nullInputReturnsEmptyArray() {
        assertEquals(0, StringUtils.mergeStringArrays(null).length);
    }

    @Test
    public void mergeStringArrays_skipsNullInnerArrays() {
        String[] result = StringUtils.mergeStringArrays(new String[][]{{"a"}, null, {"b"}});
        assertArrayEquals(new String[]{"a", "b"}, result);
    }

    // ---- isEmpty ----

    @Test
    public void isEmpty_trueForNull() {
        assertTrue(StringUtils.isEmpty(null));
    }

    @Test
    public void isEmpty_trueForBlank() {
        assertTrue(StringUtils.isEmpty("   "));
    }

    @Test
    public void isEmpty_falseForNonBlank() {
        assertFalse(StringUtils.isEmpty("hello"));
    }

    // ---- intArrToString ----

    @Test
    public void intArrToString_defaultCommaDelimiter() {
        assertEquals("1,2,3", StringUtils.intArrToString(new int[]{1, 2, 3}));
    }

    @Test
    public void intArrToString_customDelimiter() {
        assertEquals("1-2-3", StringUtils.intArrToString(new int[]{1, 2, 3}, "-"));
    }

    @Test
    public void intArrToString_singleElement() {
        assertEquals("42", StringUtils.intArrToString(new int[]{42}));
    }

    // ---- formatTimeToString ----

    @Test
    public void formatTimeToString_secondsOnly() {
        assertEquals("00:00:05", StringUtils.formatTimeToString(5));
    }

    @Test
    public void formatTimeToString_oneHour() {
        assertEquals("01:00:00", StringUtils.formatTimeToString(3600));
    }

    @Test
    public void formatTimeToString_complex() {
        // 3661 seconds = 1h 1m 1s
        assertEquals("01:01:01", StringUtils.formatTimeToString(3661));
    }

    // ---- bytesToString / stringToBytes round-trip ----

    @Test
    public void bytesToString_roundTrip() {
        byte[] original = {0x00, 0x0f, (byte) 0xff, 0x42};
        String hex = StringUtils.bytesToString(original);
        byte[] restored = StringUtils.stringToBytes(hex);
        assertArrayEquals(original, restored);
    }

    @Test
    public void bytesToString_knownValue() {
        assertEquals("0042ff", StringUtils.bytesToString(new byte[]{0x00, 0x42, (byte) 0xff}));
    }

    // ---- propertiesToString / stringToProperties round-trip ----

    @Test
    public void propertiesRoundTrip_singleEntry() {
        Properties p = new Properties();
        p.setProperty("key", "value");
        String encoded = StringUtils.propertiesToString(p);
        Properties restored = StringUtils.stringToProperties(encoded);
        assertEquals("value", restored.getProperty("key"));
    }

    @Test
    public void propertiesRoundTrip_multipleEntries() {
        Properties p = new Properties();
        p.setProperty("a", "1");
        p.setProperty("b", "2");
        String encoded = StringUtils.propertiesToString(p);
        Properties restored = StringUtils.stringToProperties(encoded);
        assertEquals("1", restored.getProperty("a"));
        assertEquals("2", restored.getProperty("b"));
    }

    @Test
    public void propertiesToString_nullReturnsEmpty() {
        assertEquals("", StringUtils.propertiesToString(null));
    }

    // ---- objectArrayToString ----

    @Test
    public void objectArrayToString_joinsWithSeparator() {
        assertEquals("a|b|c", StringUtils.objectArrayToString("|", "a", "b", "c"));
    }

    @Test
    public void objectArrayToString_singleElement() {
        assertEquals("x", StringUtils.objectArrayToString(",", "x"));
    }
}
