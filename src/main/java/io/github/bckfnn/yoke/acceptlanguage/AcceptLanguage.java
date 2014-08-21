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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.vertx.java.core.Handler;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;

/**
 * AcceptLanguage is a Yoke middleware that parses the "accept-language" http header and based
 * on that assign an ordered list of locales to "locales" and highest priority locale to "locale".
 */
public class AcceptLanguage extends Middleware {
    private LruCache cache = null;
    private String localesName = "locales";
    private String localeName = "locale";
    private Locale defaultLocale = Locale.ENGLISH;

    /**
     * Constructor.
     * @param cache true if the parsed values should be cached. 
     */
    public AcceptLanguage(boolean cache) {
        if (cache) {
            this.cache = new LruCache(1024);
        }
    }

    /**
     * Set the name of the request variable where the list of locales will be stored.
     * Default to <code>"locales"</code>. 
     * @param localesName name of the request variable.
     * @return this, for chaining.
     */
    public AcceptLanguage localesName(String localesName) {
        this.localesName = localesName;
        return this;
    }

    /**
     * Set the name of the request variable where the highest priority locale will be stored.
     * Default to <code>"locale"</code>. 
     * @param localeName name of the request variable.
     * @return this, for chaining.
     */
    public AcceptLanguage localeName(String localeName) {
        this.localeName = localeName;
        return this;
    }

    /**
     * Set the default locale to assign "locale" if no Accept-Locale header is available.
     * @param defaultLocale the default Locale ti use.
     * @return this, for chaining.
     */
    public AcceptLanguage defaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
        return this;
    }

    @Override
    public void handle(YokeRequest request, Handler<Object> next) {
        String languages = request.getHeader("accept-language");

        List<Locale> locales = null;

        if (cache != null) {
            locales = cache.get(languages);
            if (locales == null) {
                locales = getLocales(languages);
                cache.put(languages, locales);
            }
        } else {
            locales = getLocales(languages);
        }

        request.put(localesName, locales);
        if (locales.size() > 0) {
            request.put(localeName, locales.get(0));
        } else {
            request.put(localeName, defaultLocale);
        }
        next.handle(null);
    }

    /**
     * Parse the Accept-Language HTTP header.
     * @param languages the header value.
     * @return sorted list of Locale.
     */
    public static List<Locale> getLocales(String languages) {
        List<LangQ> list = new ArrayList<>();

        if (languages == null) {
            return new ArrayList<Locale>();
        }

        for (String str : languages.split(",")) {
            String[] arr = str.trim().replace("-", "_").split(";");

            // Parse the q-value
            Double q = 1.0D;
            for (String s : arr) {
                s = s.trim();
                if (s.startsWith("q=")) {
                    try {
                        q = Double.parseDouble(s.substring(2).trim());
                    } catch (NumberFormatException e) {
                        // ignore the q value.
                    }
                    break;
                }
            }
            list.add(new LangQ(arr[0], q));
        }

        Collections.sort(list);

        List<Locale> ret = new ArrayList<>();
        for (LangQ lang : list) {
            ret.add(lang.getLocale());
        }
        return ret;
    }

    static class LangQ implements Comparable<LangQ> {
        public String lang;
        public double q;

        public LangQ(String lang, double q) {
            this.lang = lang;
            this.q = q;
        }

        public Locale getLocale() {
            // Parse the locale
            String[] l = lang.split("_");
            switch(l.length) {
            case 2: return new Locale(l[0], l[1]);
            case 3: return new Locale(l[0], l[1], l[2]);
            default: return new Locale(l[0]);
            }

        }

        @Override
        public int compareTo(LangQ o) {
            return (int) Math.signum(o.q - this.q);
        }
    }

    private class LruCache extends LinkedHashMap<String, List<Locale>> {
        private static final long serialVersionUID = 1L;

        private final int maxEntries;

        public LruCache(final int maxEntries) {
            super(maxEntries + 1, 1.0f, true);
            this.maxEntries = maxEntries;
        }

        @Override
        protected boolean removeEldestEntry(final Map.Entry<String, List<Locale>> eldest) {
            return super.size() > maxEntries;
        }
    }
}
