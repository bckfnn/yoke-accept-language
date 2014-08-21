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

    /**
     * Constructor.
     * @param cache true if the parsed values should be cached. 
     */
    public AcceptLanguage(boolean cache) {
        if (cache) {
            this.cache = new LruCache(1024);
        }
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

        request.put("locales", locales);
        if (locales.size() > 0) {
            request.put("locale", locales.get(0));
        } else {
            request.put("locale", Locale.ENGLISH);
        }
        next.handle(null);
    }

    private List<Locale> getLocales(String languages) {
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
