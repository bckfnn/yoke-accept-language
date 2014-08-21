/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
