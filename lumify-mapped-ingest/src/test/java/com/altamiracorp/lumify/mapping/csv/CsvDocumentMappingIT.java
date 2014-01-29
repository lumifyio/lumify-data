/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.mapping.csv;

import static org.junit.Assert.*;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention.Builder;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermRelationship;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.mapping.DocumentMapping;
import com.altamiracorp.lumify.util.LineReader;
import com.altamiracorp.securegraph.type.GeoCircle;
import com.altamiracorp.securegraph.type.GeoPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

/**
 * Integration test for CsvDocumentMapping classes.
 */
public class CsvDocumentMappingIT {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(CsvDocumentMappingIT.class);

    private static final String TEST_PROCESS_ID = "CsvDocumentMapping Integration Test";
    private static final String TEST_RESOURCE_DIR = CsvDocumentMappingIT.class.getPackage().getName().replaceAll("\\.", "/");
    private static final String TEST_FILE = String.format("%s/%s", TEST_RESOURCE_DIR, "csv_mapping_integration_test.csv");
    private static final String TEST_MAPPING_FILE = String.format("%s.mapping.json", TEST_FILE);

    private static final int PERSON_COL = 0;
    private static final int EMPLOYER_COL = 13;
    private static final int HOME_COL = 17;
    private static final int TWITTER_COL = 20;
    private static final int FACEBOOK_COL = 27;

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private static TermExtractionResult expectedResult;
    private CsvDocumentMapping mapping;
    private InputStream csvStream;

    @BeforeClass
    public static void setUpClass() throws Exception {
        // find the offsets for the various entities in each line and initialize the expected results
        Queue<TermStart> offsets = readOffsets();
        expectedResult = buildExpectedResult(offsets);
    }

    @Before
    public void setup() throws Exception {
        InputStream mappingStream = ClassLoader.getSystemResourceAsStream(TEST_MAPPING_FILE);
        mapping = (CsvDocumentMapping) JSON_MAPPER.readValue(mappingStream, DocumentMapping.class);
        csvStream = ClassLoader.getSystemResourceAsStream(TEST_FILE);
    }

    @Test
    public void testCsvDocumentMapping() throws Exception {
        StringWriter ingestWriter = new StringWriter();
        mapping.ingestDocument(csvStream, ingestWriter);
        TermExtractionResult actualResult = mapping.mapDocument(new StringReader(ingestWriter.toString()), TEST_PROCESS_ID);

        // if results are not equal, execute our own comparisons to identify the term or relationship that is not correct
        boolean match = expectedResult.equals(actualResult);
        if (!match) {
            List<TermMention> expectedMentions = expectedResult.getTermMentions();
            List<TermMention> actualMentions = actualResult.getTermMentions();

            // if sizes are off, identify the missing/extra elements
            int expSize = expectedMentions.size();
            int actSize = actualMentions.size();
            if (expSize != actSize) {
                String qualifier;
                Map<Integer, TermMention> startMap = new TreeMap<Integer, TermMention>();
                List<TermMention> delta;
                if (expSize < actSize) {
                    for (TermMention tm : actualMentions) {
                        startMap.put(tm.getStart(), tm);
                    }
                    for (TermMention tm : expectedMentions) {
                        startMap.remove(tm.getStart());
                    }
                    delta = new ArrayList<TermMention>(startMap.values());
                    qualifier = "Extra";
                } else {
                    for (TermMention tm : expectedMentions) {
                        startMap.put(tm.getStart(), tm);
                    }
                    for (TermMention tm : actualMentions) {
                        startMap.remove(tm.getStart());
                    }
                    delta = new ArrayList<TermMention>(startMap.values());
                    qualifier = "Missing";
                }
                for (TermMention tm : delta) {
                    LOGGER.error("%s Mention: %s", qualifier, tm);
                }
                fail(String.format("Expected %d mentions, got %d. (Delta Size: %d)", expSize, actSize, delta.size()));
            }

            for (int idx=0; idx < expectedMentions.size(); idx++) {
                assertEquals(String.format("[mention %d]", idx), expectedMentions.get(idx), actualMentions.get(idx));
            }
            List<TermRelationship> expectedRels = expectedResult.getRelationships();
            List<TermRelationship> actualRels = actualResult.getRelationships();
            assertEquals("relationship count", expectedRels.size(), actualRels.size());
            for (int idx=0; idx < expectedRels.size(); idx++) {
                assertEquals(String.format("[rel %d]", idx), expectedRels.get(idx), actualRels.get(idx));
            }
        }
    }

    private static TermExtractionResult buildExpectedResult(final Queue<TermStart> offsetsQueue) {
        TermExtractionResult expected = new TermExtractionResult();
        List<TermMention> mentions = new ArrayList<TermMention>();
        List<TermRelationship> relationships = new ArrayList<TermRelationship>();

        TermStart offsets;

        int testNum = 1;
        // line 1: Missing Required Entity; NO entities added
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships, null, null, null, null, null);

        // line 2: Missing Required Field (Double) of Required Entity: NO entities added
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships, null, null, null, null, null);

        // line 3: Bad Data for Required Field (Double) of Required Entity: NO entities added
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships, null, null, null, null, null);

        // line 4: Full Joe Smith Line (lowercase booleans)
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                joeSmith(offsets.personStart),
                acme(offsets.employerStart),
                chantilly(offsets.homeStart),
                joeSmithTwitter(offsets.twitterStart),
                facebook(JOE_SMITH_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 5: Full Jane Smith Line (uppercase booleans)
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                janeSmith(offsets.personStart),
                intertech(offsets.employerStart),
                chantilly(offsets.homeStart),
                janeSmithTwitter(offsets.twitterStart),
                facebook(JANE_SMITH_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 6: Full Bob Saget Line (mixed case booleans)
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                bobSaget(offsets.personStart),
                aristocrats(offsets.employerStart),
                philadelphia(offsets.homeStart),
                bobSagetTwitter(offsets.twitterStart),
                facebook(BOB_SAGET_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 7: Missing Optional Entities
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                null,
                acme(offsets.employerStart),
                chantilly(offsets.homeStart),
                null,
                facebook(JOE_SMITH_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 8: Missing All Optional Properties
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                janeSmith(offsets.personStart)
                        .setProperty(PERSON_HEIGHT_IN, null)
                        .setProperty(PERSON_WEIGHT_LBS, null),
                intertech(offsets.employerStart)
                        .setProperty(EMPLOYER_LOCATION, null),
                chantilly(offsets.homeStart)
                        .setProperty(HOME_CITY_NAME, null),
                initBuilder(TWITTER_CONCEPT, JANE_SMITH_TWITTER_HANDLE, offsets.twitterStart).useExisting(true),
                facebook(JANE_SMITH_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 9: Invalid values for all Optional Properties
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                bobSaget(offsets.personStart)
                        .setProperty(PERSON_HEIGHT_IN, null)
                        .setProperty(PERSON_WEIGHT_LBS, null),
                aristocrats(offsets.employerStart),
                philadelphia(offsets.homeStart),
                bobSagetTwitter(offsets.twitterStart)
                        .setProperty(TWITTER_FRIEND_COUNT, null)
                        .setProperty(TWITTER_LAST_TWEETED_AT, null),
                facebook(BOB_SAGET_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 10: Missing latitude for Optional Geo-Properties
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                joeSmith(offsets.personStart),
                acme(offsets.employerStart)
                        .setProperty(EMPLOYER_LOCATION, null),
                chantilly(offsets.homeStart),
                joeSmithTwitter(offsets.twitterStart)
                        .setProperty(TWITTER_LAST_TWEETED_FROM, null),
                facebook(JOE_SMITH_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 11: Missing longitude for Optional Geo-Properties
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                joeSmith(offsets.personStart),
                acme(offsets.employerStart)
                        .setProperty(EMPLOYER_LOCATION, null),
                chantilly(offsets.homeStart),
                joeSmithTwitter(offsets.twitterStart)
                        .setProperty(TWITTER_LAST_TWEETED_FROM, null),
                facebook(JOE_SMITH_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 12: Missing radius for Optional GeoCircles
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                joeSmith(offsets.personStart),
                acme(offsets.employerStart),
                chantilly(offsets.homeStart),
                joeSmithTwitter(offsets.twitterStart)
                        .setProperty(TWITTER_LAST_TWEETED_FROM, null),
                facebook(JOE_SMITH_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 13: Invalid latitude for Optional Geo-Properties
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                joeSmith(offsets.personStart),
                acme(offsets.employerStart)
                        .setProperty(EMPLOYER_LOCATION, null),
                chantilly(offsets.homeStart),
                joeSmithTwitter(offsets.twitterStart)
                        .setProperty(TWITTER_LAST_TWEETED_FROM, null),
                facebook(JOE_SMITH_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 14: Invalid longitude for Optional Geo-Properties
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                joeSmith(offsets.personStart),
                acme(offsets.employerStart)
                        .setProperty(EMPLOYER_LOCATION, null),
                chantilly(offsets.homeStart),
                joeSmithTwitter(offsets.twitterStart)
                        .setProperty(TWITTER_LAST_TWEETED_FROM, null),
                facebook(JOE_SMITH_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 15: Invalid radius for Optional GeoCircles
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                joeSmith(offsets.personStart),
                acme(offsets.employerStart),
                chantilly(offsets.homeStart),
                joeSmithTwitter(offsets.twitterStart)
                        .setProperty(TWITTER_LAST_TWEETED_FROM, null),
                facebook(JOE_SMITH_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 16: Missing Required Boolean
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                null,
                acme(offsets.employerStart),
                chantilly(offsets.homeStart),
                joeSmithTwitter(offsets.twitterStart),
                facebook(JOE_SMITH_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 17: Missing Required Date
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                null,
                intertech(offsets.employerStart),
                chantilly(offsets.homeStart),
                janeSmithTwitter(offsets.twitterStart),
                facebook(JANE_SMITH_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 18: Invalid Required Date
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                null,
                intertech(offsets.employerStart),
                chantilly(offsets.homeStart),
                janeSmithTwitter(offsets.twitterStart),
                facebook(JANE_SMITH_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 19: Missing Required GeoCircle Latitude
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                null,
                aristocrats(offsets.employerStart),
                philadelphia(offsets.homeStart),
                bobSagetTwitter(offsets.twitterStart),
                facebook(BOB_SAGET_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 20: Missing Required GeoCircle Longitude
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                null,
                aristocrats(offsets.employerStart),
                philadelphia(offsets.homeStart),
                bobSagetTwitter(offsets.twitterStart),
                facebook(BOB_SAGET_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 21: Missing Required GeoCircle Radius
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                null,
                aristocrats(offsets.employerStart),
                philadelphia(offsets.homeStart),
                bobSagetTwitter(offsets.twitterStart),
                facebook(BOB_SAGET_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 22: Invalid Required GeoCircle Latitude
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                null,
                acme(offsets.employerStart),
                chantilly(offsets.homeStart),
                joeSmithTwitter(offsets.twitterStart),
                facebook(JOE_SMITH_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 23: Invalid Required GeoCircle Longitude
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                null,
                acme(offsets.employerStart),
                chantilly(offsets.homeStart),
                joeSmithTwitter(offsets.twitterStart),
                facebook(JOE_SMITH_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 24: Invalid Required GeoCircle Radius
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                null,
                acme(offsets.employerStart),
                chantilly(offsets.homeStart),
                joeSmithTwitter(offsets.twitterStart),
                facebook(JOE_SMITH_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 25: Missing Required GeoPoint Latitude
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                null,
                intertech(offsets.employerStart),
                chantilly(offsets.homeStart),
                janeSmithTwitter(offsets.twitterStart),
                facebook(JANE_SMITH_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 26: Missing Required GeoPoint Longitude
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                null,
                intertech(offsets.employerStart),
                chantilly(offsets.homeStart),
                janeSmithTwitter(offsets.twitterStart),
                facebook(JANE_SMITH_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 27: Missing Required GeoPoint Altitude
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                null,
                intertech(offsets.employerStart),
                chantilly(offsets.homeStart),
                janeSmithTwitter(offsets.twitterStart),
                facebook(JANE_SMITH_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 28: Invalid Required GeoPoint Latitude
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                null,
                aristocrats(offsets.employerStart),
                philadelphia(offsets.homeStart),
                bobSagetTwitter(offsets.twitterStart),
                facebook(BOB_SAGET_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 29: Invalid Required GeoPoint Longitude
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                null,
                aristocrats(offsets.employerStart),
                philadelphia(offsets.homeStart),
                bobSagetTwitter(offsets.twitterStart),
                facebook(BOB_SAGET_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 30: Invalid Required GeoPoint Altitude
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                null,
                aristocrats(offsets.employerStart),
                philadelphia(offsets.homeStart),
                bobSagetTwitter(offsets.twitterStart),
                facebook(BOB_SAGET_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 31: Missing Required Integer
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                null,
                acme(offsets.employerStart),
                chantilly(offsets.homeStart),
                joeSmithTwitter(offsets.twitterStart),
                facebook(JOE_SMITH_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 32: Invalid Required Integer
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                null,
                acme(offsets.employerStart),
                chantilly(offsets.homeStart),
                joeSmithTwitter(offsets.twitterStart),
                facebook(JOE_SMITH_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 33: Missing Required Long
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                janeSmith(offsets.personStart),
                intertech(offsets.employerStart),
                null,
                janeSmithTwitter(offsets.twitterStart),
                facebook(JANE_SMITH_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 34: Invalid Required Long
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                janeSmith(offsets.personStart),
                intertech(offsets.employerStart),
                null,
                janeSmithTwitter(offsets.twitterStart),
                facebook(JANE_SMITH_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 35: Missing Required String
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                null,
                aristocrats(offsets.employerStart),
                philadelphia(offsets.homeStart),
                bobSagetTwitter(offsets.twitterStart),
                facebook(BOB_SAGET_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        // line 36: Whitespace Required String
        offsets = offsetsQueue.poll();
        addLine(testNum++, mentions, relationships,
                null,
                aristocrats(offsets.employerStart),
                philadelphia(offsets.homeStart),
                bobSagetTwitter(offsets.twitterStart),
                facebook(BOB_SAGET_FACEBOOK_HANDLE, offsets.facebookStart)
        );

        expected.addAllTermMentions(mentions);
        expected.addAllRelationships(relationships);
        return expected;
    }

    private static void addLine(final int testNum, final List<TermMention> mentions, final List<TermRelationship> rels,
            final Builder personBld, final Builder employerBld, final Builder homeBld, final Builder twitterBld,
            final Builder facebookBld) {
        TermMention person = personBld != null ? personBld.setProperty(TEST_NUMBER, testNum).build() : null;
        TermMention employer = employerBld != null ? employerBld.setProperty(TEST_NUMBER, testNum).build() : null;
        TermMention home = homeBld != null ? homeBld.setProperty(TEST_NUMBER, testNum).build() : null;
        TermMention twitter = twitterBld != null ? twitterBld.setProperty(TEST_NUMBER, testNum).build() : null;
        // no properties on Facebook terms; add test number to process
        TermMention facebook = facebookBld != null ? facebookBld.process(String.format("%s[%d]", TEST_NUMBER, testNum)).build() : null;
        if (person != null) {
            mentions.add(person);
        }
        if (employer != null) {
            mentions.add(employer);
        }
        if (home != null) {
            mentions.add(home);
        }
        if (twitter != null) {
            mentions.add(twitter);
        }
        if (facebook != null) {
            mentions.add(facebook);
        }
        if (person != null && home != null) {
            rels.add(new TermRelationship(person, home, LIVES_AT));
        }
        if (person != null && employer != null) {
            rels.add(new TermRelationship(employer, person, EMPLOYS));
        }
        if (person != null && twitter != null) {
            rels.add(new TermRelationship(person, twitter, HAS_ONLINE_PRESENCE));
        }
        if (person != null && facebook != null) {
            rels.add(new TermRelationship(person, facebook, HAS_ONLINE_PRESENCE));
        }
    }

    private static TermMention.Builder initBuilder(final String conceptLabel, final String sign, final int start) {
        return new TermMention.Builder()
                .sign(sign)
                .start(start)
                .end(start + (sign != null ? sign.length() : 0))
                .ontologyClassUri(conceptLabel)
                .resolved(true)
                .process(TEST_PROCESS_ID);
    }

    private static TermMention.Builder joeSmith(final int start) {
        return initBuilder(PERSON_CONCEPT, JOE_SMITH_NAME, start)
                .useExisting(false)
                .setProperty(PERSON_BIRTH_DATE, JOE_SMITH_BIRTH_DATE.getTime())
                .setProperty(PERSON_GENDER, JOE_SMITH_GENDER)
                .setProperty(PERSON_DECEASED, JOE_SMITH_DECEASED)
                .setProperty(PERSON_HEIGHT_FT, JOE_SMITH_HEIGHT_FT)
                .setProperty(PERSON_HEIGHT_IN, JOE_SMITH_HEIGHT_IN)
                .setProperty(PERSON_WEIGHT_LBS, JOE_SMITH_WEIGHT_LBS)
                .setProperty(PERSON_HOME_ADDRESS, JOE_SMITH_HOME_ADDRESS)
                .setProperty(PERSON_HOME_AREA, JOE_SMITH_HOME_AREA);
    }

    private static TermMention.Builder joeSmithTwitter(final int start) {
        return initBuilder(TWITTER_CONCEPT, JOE_SMITH_TWITTER_HANDLE, start)
                .useExisting(true)
                .setProperty(TWITTER_FRIEND_COUNT, JOE_SMITH_FRIEND_COUNT)
                .setProperty(TWITTER_HAS_PROFILE_PIC, JOE_SMITH_HAS_PROFILE_PIC)
                .setProperty(TWITTER_LAST_TWEETED_AT, JOE_SMITH_LAST_TWEETED_AT.getTime())
                .setProperty(TWITTER_LAST_TWEETED_FROM, JOE_SMITH_LAST_TWEETED_FROM);
    }

    private static TermMention.Builder janeSmith(final int start) {
        return initBuilder(PERSON_CONCEPT, JANE_SMITH_NAME, start)
                .useExisting(false)
                .setProperty(PERSON_BIRTH_DATE, JANE_SMITH_BIRTH_DATE.getTime())
                .setProperty(PERSON_GENDER, JANE_SMITH_GENDER)
                .setProperty(PERSON_DECEASED, JANE_SMITH_DECEASED)
                .setProperty(PERSON_HEIGHT_FT, JANE_SMITH_HEIGHT_FT)
                .setProperty(PERSON_HEIGHT_IN, JANE_SMITH_HEIGHT_IN)
                .setProperty(PERSON_WEIGHT_LBS, JANE_SMITH_WEIGHT_LBS)
                .setProperty(PERSON_HOME_ADDRESS, JANE_SMITH_HOME_ADDRESS)
                .setProperty(PERSON_HOME_AREA, JANE_SMITH_HOME_AREA);
    }

    private static TermMention.Builder janeSmithTwitter(final int start) {
        return initBuilder(TWITTER_CONCEPT, JANE_SMITH_TWITTER_HANDLE, start)
                .useExisting(true)
                .setProperty(TWITTER_FRIEND_COUNT, JANE_SMITH_FRIEND_COUNT)
                .setProperty(TWITTER_HAS_PROFILE_PIC, JANE_SMITH_HAS_PROFILE_PIC)
                .setProperty(TWITTER_LAST_TWEETED_AT, JANE_SMITH_LAST_TWEETED_AT.getTime())
                .setProperty(TWITTER_LAST_TWEETED_FROM, JANE_SMITH_LAST_TWEETED_FROM);
    }

    private static TermMention.Builder bobSaget(final int start) {
        return initBuilder(PERSON_CONCEPT, BOB_SAGET_NAME, start)
                .useExisting(false)
                .setProperty(PERSON_BIRTH_DATE, BOB_SAGET_BIRTH_DATE.getTime())
                .setProperty(PERSON_GENDER, BOB_SAGET_GENDER)
                .setProperty(PERSON_DECEASED, BOB_SAGET_DECEASED)
                .setProperty(PERSON_HEIGHT_FT, BOB_SAGET_HEIGHT_FT)
                .setProperty(PERSON_HEIGHT_IN, BOB_SAGET_HEIGHT_IN)
                .setProperty(PERSON_WEIGHT_LBS, BOB_SAGET_WEIGHT_LBS)
                .setProperty(PERSON_HOME_ADDRESS, BOB_SAGET_HOME_ADDRESS)
                .setProperty(PERSON_HOME_AREA, BOB_SAGET_HOME_AREA);
    }

    private static TermMention.Builder bobSagetTwitter(final int start) {
        return initBuilder(TWITTER_CONCEPT, BOB_SAGET_TWITTER_HANDLE, start)
                .useExisting(true)
                .setProperty(TWITTER_FRIEND_COUNT, BOB_SAGET_FRIEND_COUNT)
                .setProperty(TWITTER_HAS_PROFILE_PIC, BOB_SAGET_HAS_PROFILE_PIC)
                .setProperty(TWITTER_LAST_TWEETED_AT, BOB_SAGET_LAST_TWEETED_AT.getTime())
                .setProperty(TWITTER_LAST_TWEETED_FROM, BOB_SAGET_LAST_TWEETED_FROM);
    }

    private static TermMention.Builder acme(final int start) {
        return initBuilder(ORGANIZATION_CONCEPT, ACME_CORP_NAME, start)
                .useExisting(false)
                .setProperty(EMPLOYER_LOCATION, ACME_CORP_LOCATION)
                .setProperty(EMPLOYER_EARNINGS, ACME_CORP_EARNINGS);

    }

    private static TermMention.Builder intertech(final int start) {
        return initBuilder(ORGANIZATION_CONCEPT, INTERTECH_NAME, start)
                .useExisting(false)
                .setProperty(EMPLOYER_LOCATION, INTERTECH_LOCATION)
                .setProperty(EMPLOYER_EARNINGS, INTERTECH_EARNINGS);
    }

    private static TermMention.Builder aristocrats(final int start) {
        return initBuilder(ORGANIZATION_CONCEPT, ARISTOCRATS_NAME, start)
                .useExisting(false)
                .setProperty(EMPLOYER_LOCATION, ARISTOCRATS_ADDRESS)
                .setProperty(EMPLOYER_EARNINGS, ARISTOCRATS_EARNINGS);
    }

    private static TermMention.Builder chantilly(final int start) {
        return initBuilder(LOCATION_CONCEPT, CHANTILLY_ZIP_CODE, start)
                .useExisting(true)
                .setProperty(HOME_CITY_NAME, CHANTILLY_CITY)
                .setProperty(HOME_POPULATION, CHANTILLY_POPULATION);
    }

    private static TermMention.Builder philadelphia(final int start) {
        return initBuilder(LOCATION_CONCEPT, PHILADELPHIA_ZIP_CODE, start)
                .useExisting(true)
                .setProperty(HOME_CITY_NAME, PHILADELPHIA_CITY)
                .setProperty(HOME_POPULATION, PHILADELPHIA_POPULATION);
    }

    private static TermMention.Builder facebook(final String name, final int start) {
        return initBuilder(FACEBOOK_CONCEPT, name, start).useExisting(true);
    }

    private static final DateFormat SHORT_FMT = new SimpleDateFormat("MM/dd/yyyy");
    private static final DateFormat LONG_FMT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private static final DateFormat CUSTOM_FMT = new SimpleDateFormat("yyyyMMdd-HHmmss");
    private static Date toDate(final DateFormat fmt, final String str) {
        try {
            return fmt.parse(str);
        } catch (ParseException pe) {
            throw new IllegalArgumentException(pe);
        }
    }

    // ontology concepts
    private static final String TWITTER_CONCEPT = "twitterUser";
    private static final String FACEBOOK_CONCEPT = "facebookAccount";
    private static final String ORGANIZATION_CONCEPT = "organization";
    private static final String PERSON_CONCEPT = "person";
    private static final String LOCATION_CONCEPT = "location";

    // relationship labels
    private static final String LIVES_AT = "livesAt";
    private static final String EMPLOYS = "employs";
    private static final String HAS_ONLINE_PRESENCE = "hasOnlinePresence";

    // property keys
    private static final String PERSON_BIRTH_DATE = "Birth Date";
    private static final String PERSON_GENDER = "Gender";
    private static final String PERSON_DECEASED = "Deceased";
    private static final String PERSON_HEIGHT_FT = "Height_ft";
    private static final String PERSON_HEIGHT_IN = "Height_in";
    private static final String PERSON_WEIGHT_LBS = "Weight_lbs";
    private static final String PERSON_HOME_ADDRESS = "Home Address";
    private static final String PERSON_HOME_AREA = "Home Area";
    private static final String EMPLOYER_LOCATION = "Location";
    private static final String EMPLOYER_EARNINGS = "Earnings";
    private static final String HOME_CITY_NAME = "City Name";
    private static final String HOME_POPULATION = "Population";
    private static final String TWITTER_FRIEND_COUNT = "Friend Count";
    private static final String TWITTER_HAS_PROFILE_PIC = "hasProfilePic";
    private static final String TWITTER_LAST_TWEETED_AT = "Last Tweeted At";
    private static final String TWITTER_LAST_TWEETED_FROM = "Last Tweeted From";
    private static final String TEST_NUMBER = "Test Number";

    // joe smith values
    private static final String JOE_SMITH_NAME = "Joe Smith";
    private static final Date JOE_SMITH_BIRTH_DATE = toDate(SHORT_FMT, "02/28/1952");
    private static final String JOE_SMITH_GENDER = "male";
    private static final boolean JOE_SMITH_DECEASED = false;
    private static final int JOE_SMITH_HEIGHT_FT = 6;
    private static final int JOE_SMITH_HEIGHT_IN = 2;
    private static final double JOE_SMITH_WEIGHT_LBS = 173.27d;
    private static final GeoPoint JOE_SMITH_HOME_ADDRESS = new GeoPoint(39.046198d, -77.473209d, 322.08d);
    private static final GeoCircle JOE_SMITH_HOME_AREA = new GeoCircle(39.046198d, -77.473209d, 32.37d);
    private static final String JOE_SMITH_TWITTER_HANDLE = "@joesmith";
    private static final long JOE_SMITH_FRIEND_COUNT = 263L;
    private static final boolean JOE_SMITH_HAS_PROFILE_PIC = true;
    private static final Date JOE_SMITH_LAST_TWEETED_AT = toDate(CUSTOM_FMT, "20120707-120022");
    private static final GeoCircle JOE_SMITH_LAST_TWEETED_FROM = new GeoCircle(38.9756d, -77.6414d, 15.73d);
    private static final String JOE_SMITH_FACEBOOK_HANDLE = "joesmith27";

    // acme corp values
    private static final String ACME_CORP_NAME = "Acme Corporation";
    private static final GeoPoint ACME_CORP_LOCATION = new GeoPoint(38.9714d, -77.3886d);
    private static final double ACME_CORP_EARNINGS = 297346122.87;

    // chantilly values
    private static final String CHANTILLY_ZIP_CODE = "20151";
    private static final String CHANTILLY_CITY = "Chantilly, VA";
    private static final long CHANTILLY_POPULATION = 23039L;

    // jane smith values
    private static final String JANE_SMITH_NAME = "Jane Smith";
    private static final Date JANE_SMITH_BIRTH_DATE = toDate(LONG_FMT, "08/19/1959 17:33:02");
    private static final String JANE_SMITH_GENDER = "female";
    private static final boolean JANE_SMITH_DECEASED = true;
    private static final int JANE_SMITH_HEIGHT_FT = 5;
    private static final int JANE_SMITH_HEIGHT_IN = 1;
    private static final double JANE_SMITH_WEIGHT_LBS = 118.96d;
    private static final GeoPoint JANE_SMITH_HOME_ADDRESS = JOE_SMITH_HOME_ADDRESS;
    private static final GeoCircle JANE_SMITH_HOME_AREA = JOE_SMITH_HOME_AREA;
    private static final String JANE_SMITH_TWITTER_HANDLE = "@janesmith";
    private static final long JANE_SMITH_FRIEND_COUNT = 1893L;
    private static final boolean JANE_SMITH_HAS_PROFILE_PIC = false;
    private static final Date JANE_SMITH_LAST_TWEETED_AT = toDate(CUSTOM_FMT, "20140122-003218");
    private static final GeoCircle JANE_SMITH_LAST_TWEETED_FROM = new GeoCircle(38.8951d, -77.0367d, 0.275d);
    private static final String JANE_SMITH_FACEBOOK_HANDLE = "msjane";

    // intertech values
    private static final String INTERTECH_NAME = "Intertech";
    private static final GeoPoint INTERTECH_LOCATION = new GeoPoint(38.9544d, -77.3646d);
    private static final double INTERTECH_EARNINGS = 872390.01d;

    // bob saget values
    private static final String BOB_SAGET_NAME = "Bob Saget";
    private static final Date BOB_SAGET_BIRTH_DATE = toDate(SHORT_FMT, "05/17/1956");
    private static final String BOB_SAGET_GENDER = "male";
    private static final boolean BOB_SAGET_DECEASED = false;
    private static final int BOB_SAGET_HEIGHT_FT = 6;
    private static final int BOB_SAGET_HEIGHT_IN = 5;
    private static final double BOB_SAGET_WEIGHT_LBS = 193.0d;
    private static final GeoPoint BOB_SAGET_HOME_ADDRESS = new GeoPoint(39.950598d, -75.149274d, 39.18d);
    private static final GeoCircle BOB_SAGET_HOME_AREA = new GeoCircle(39.950598d, -75.149274d, 57.0d);
    private static final String BOB_SAGET_TWITTER_HANDLE = "@bobsaget";
    private static final long BOB_SAGET_FRIEND_COUNT = 2876322L;
    private static final boolean BOB_SAGET_HAS_PROFILE_PIC = true;
    private static final Date BOB_SAGET_LAST_TWEETED_AT = toDate(CUSTOM_FMT, "20140120-230442");
    private static final GeoCircle BOB_SAGET_LAST_TWEETED_FROM = new GeoCircle(34.05d, -118.25d, 2.86d);
    private static final String BOB_SAGET_FACEBOOK_HANDLE = "fullhousedad";

    // aristocrats values
    private static final String ARISTOCRATS_NAME = "Aristocrats, Inc.";
    private static final GeoPoint ARISTOCRATS_ADDRESS = new GeoPoint(34.05d, -118.25d);
    private static final double ARISTOCRATS_EARNINGS = 1392867.39d;

    // philadelphia values
    private static final String PHILADELPHIA_ZIP_CODE = "19106";
    private static final String PHILADELPHIA_CITY = "Philadelphia, PA";
    private static final long PHILADELPHIA_POPULATION = 5441567L;

    private static Queue<TermStart> readOffsets() throws Exception {
        InputStream mappingStream = ClassLoader.getSystemResourceAsStream(TEST_MAPPING_FILE);
        CsvDocumentMapping csvMap = (CsvDocumentMapping) JSON_MAPPER.readValue(mappingStream, DocumentMapping.class);
        InputStream csvStream = ClassLoader.getSystemResourceAsStream(TEST_FILE);

        StringWriter ingestWriter = new StringWriter();
        csvMap.ingestDocument(csvStream, ingestWriter);
        StringReader ingestReader = new StringReader(ingestWriter.toString());
        LineReader lineReader = new LineReader(ingestReader);
        lineReader.skipLines(csvMap.getSkipRows());

        Queue<TermStart> startQueue = new LinkedList<TermStart>();
        int lineStart = lineReader.getOffset();
        for (String line = lineReader.readLine(); line != null && !line.isEmpty(); line = lineReader.readLine()) {
            CsvListReader csvReader = new CsvListReader(new StringReader(line), CsvPreference.EXCEL_PREFERENCE);
            List<String> fields = csvReader.read();
            startQueue.add(new TermStart(
                    findColumnStart(fields, PERSON_COL, lineStart),
                    findColumnStart(fields, EMPLOYER_COL, lineStart),
                    findColumnStart(fields, HOME_COL, lineStart),
                    findColumnStart(fields, TWITTER_COL, lineStart),
                    findColumnStart(fields, FACEBOOK_COL, lineStart))
            );
            lineStart = lineReader.getOffset();
        }
        return startQueue;
    }

    private static class TermStart {
        public final int personStart;
        public final int employerStart;
        public final int homeStart;
        public final int twitterStart;
        public final int facebookStart;

        public TermStart(int personStart, int employerStart, int homeStart, int twitterStart, int facebookStart) {
            this.personStart = personStart;
            this.employerStart = employerStart;
            this.homeStart = homeStart;
            this.twitterStart = twitterStart;
            this.facebookStart = facebookStart;
        }
    }

    private static int findColumnStart(final List<String> fields, final int index, final int fromOffset) {
        int start = fromOffset;
        for (int idx = 0; idx < index; idx++) {
            String field = fields.get(idx);
            start += ((field != null ? field.length() : 0) + 1);
        }
        return start;
    }
}
