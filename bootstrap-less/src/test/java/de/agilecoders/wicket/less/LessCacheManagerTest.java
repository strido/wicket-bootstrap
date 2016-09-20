package de.agilecoders.wicket.less;

import org.apache.wicket.util.time.Time;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.LessSource.URLSource;


public class LessCacheManagerTest {

    private int invocationOfGetContent;
    private int invocationOfNewConfiguration;

    @Before
    public void setUp() {
        invocationOfGetContent = 0;
        invocationOfNewConfiguration = 0;
    }

    /**
     * Create a URLSource that keeps track of "getContent()" invocations.
     */
    protected URLSource createSampleURLSource() {
        return new LessSource.URLSource(getClass().getResource("resources/root.less")) {
            @Override
            public String getContent() throws FileNotFound, CannotReadFile {
                invocationOfGetContent++;
                return super.getContent();
            }
        };
    }

    @Test
    public void cachesCssResult() {
        LessCacheManager cacheManager = new LessCacheManager();

        URLSource urlSource = createSampleURLSource();

        // LessSource.getContent() ist only necessary, when compiling the .less file.
        // Otherwise the result would be in the cache.

        cacheManager.getCss(urlSource);
        assertEquals(1, invocationOfGetContent);

        cacheManager.getCss(urlSource);
        assertEquals(1, invocationOfGetContent);
    }

    @Test
    public void clearCacheForcesRecompile() {
        LessCacheManager cacheManager = new LessCacheManager();

        URLSource urlSource = createSampleURLSource();

        // LessSource.getContent() ist only necessary, when compiling the .less file.
        // Otherwise the result would be in the cache.

        cacheManager.getCss(urlSource);
        assertEquals(1, invocationOfGetContent);

        cacheManager.clearCache();

        cacheManager.getCss(urlSource);
        assertEquals(2, invocationOfGetContent);
    }

    @Test
    public void clearCacheImportedSourcesSize() {
        LessCacheManager cacheManager = new LessCacheManager();

        URLSource urlSource = createSampleURLSource();

        cacheManager.getCss(urlSource);
        int expectedItems = urlSource.getImportedSources().size();

        cacheManager.clearCache();

        cacheManager.getCss(urlSource);
        assertEquals(expectedItems, urlSource.getImportedSources().size());
    }

    @Test
    public void timestampBeforeAndAfterCompile()
    {
        /*
         * This test should not be used more than to illustrate that there is an issue with the time
         * stamping before and after the first compilation. It may succeed or fail depending on the
         * order where resources/root.less and resources/imported1.less was edited.
         *
         * If it succeeds should you edit resources/imported1.less and rerun the test, this change
         * will also fail the test cachesCssResult.
         */
        LessCacheManager cacheManager = new LessCacheManager();

        URLSource urlSource = createSampleURLSource();

        Time expectedTime = cacheManager.getLastModifiedTime(urlSource);
        cacheManager.getCss(urlSource);
        assertEquals(expectedTime, cacheManager.getLastModifiedTime(urlSource));
    }

    @Test
    public void usesLessCompilerConfigurationFactoryProvidedToCreateANewLesCompilerConfiguration() {
        LessCacheManager cacheManager = new LessCacheManager(new LessCompilerConfigurationFactory() {
            @Override
            public Configuration newConfiguration() {
                invocationOfNewConfiguration++;
                return new Configuration();
            }
        });

        URLSource urlSource = createSampleURLSource();

        cacheManager.getCss(urlSource);
        assertEquals(1, invocationOfNewConfiguration);

        cacheManager.clearCache();

        cacheManager.getCss(urlSource);
        assertEquals(2, invocationOfNewConfiguration);
    }

    @Test
    public void usesDefaultConfigurationFactoryWhenProvidingNull() {
        LessCacheManager cacheManager = new LessCacheManager(null);

        URLSource urlSource = createSampleURLSource();

        cacheManager.getCss(urlSource);

        // no NullPointerException
    }

}
