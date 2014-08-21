package io.github.bckfnn.yoke.acceptlanguage;

import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests.
 */
public class Tests {

    /**
     * test header parsing.
     */
    @Test
    public void parseTest() {
        test("en-US", Locale.US);
        test("en-US;q=0.4,de;q=1", Locale.GERMAN, Locale.US);
        test("da,en;q=0.7,sk;q=0.3", new Locale("da"), Locale.ENGLISH, new Locale("sk"));
    }
    
    private void test(String value, Locale... expected) {
        List<Locale> actual = AcceptLanguage.getLocales(value);
        Assert.assertEquals(expected.length, actual.size());
        for (int i = 0; i < actual.size(); i++) {
            Assert.assertEquals(expected[i], actual.get(i));
        }
    }
}
