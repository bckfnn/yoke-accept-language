yoke-accept-language
====================

AcceptLanguage middleware for yoke (https://github.com/pmlopes/yoke) framework

Downloading
-----------

AcceptLanguage is at the moment available in bintray's jcenter:

    <repositories>
        <repository>
            <id>jcenter</id>
            <url>http://jcenter.bintray.com/</url>
        </repository>
    </repositories>
    
    <dependencies>   
        <dependency>
            <groupId>io.github.bckfnn</groupId>
            <artifactId>yoke-accept-language</artifactId>
            <version>1.0</version>
        </dependency>
    </dependencies>

Usage
-----

Add the AcceptLocale as a middleware, and use the request properties "locales" and "locale".

    yoke.use(new AcceptLanguage(true));
    yoke.use(new Handler<YokeRequest>() {
        @Override
        public void handle(YokeRequest request) {
            // Use         
            List<Locale> locales = request.get("locales");
            Locale locale = request.get("locale");
        }
    });