/*
 * (C) Copyright 2017 Boni Garcia (http://bonigarcia.github.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.github.bonigarcia.test.docker;

import static com.google.common.collect.Maps.difference;
import static io.github.bonigarcia.BrowserType.CHROME;
import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import com.google.common.collect.MapDifference;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import io.github.bonigarcia.SeleniumJupiter;
import io.github.bonigarcia.SelenoidConfig;

@ExtendWith(MockitoExtension.class)
public class SelenoidConfigTest {

    final Logger log = getLogger(lookup().lookupClass());

    @InjectMocks
    SelenoidConfig selenoidConfig;

    @BeforeAll
    static void setup() {
        SeleniumJupiter.config().setChromeLatestVersion("65.0");
        SeleniumJupiter.config().setFirefoxLatestVersion("58.0");
        SeleniumJupiter.config().setOperaLatestVersion("51.0");
        SeleniumJupiter.config().setBrowserListFromDockerHub(false);
    }

    @AfterAll
    static void teardown() {
        SeleniumJupiter.config().reset();
    }

    @ParameterizedTest
    @CsvSource({ "3.6, 4.0, 48.0", "46.0, 47.0, 48.0", "46, 47.0, 48" })
    void testNextVersion(String version, String expectedNextVersion,
            String latestVersion) {
        String nextVersion = CHROME.getNextVersion(version, latestVersion);
        assertThat(nextVersion, equalTo(expectedNextVersion));
    }

    @Test
    @SuppressWarnings("serial")
    void testBrowserConfig() throws IOException {
        String browsersJsonFromProperties = selenoidConfig
                .getBrowsersJsonAsString();

        String expectedBrowsersJson = IOUtils.toString(
                this.getClass().getResourceAsStream("/browsers-test.json"),
                defaultCharset());

        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> browserMap = gson
                .fromJson(browsersJsonFromProperties, mapType);
        Map<String, Object> expectedBrowserMap = gson
                .fromJson(expectedBrowsersJson, mapType);
        MapDifference<String, Object> difference = difference(browserMap,
                expectedBrowserMap);
        log.debug("{}", difference);

        assertTrue(difference.areEqual());
    }

}
