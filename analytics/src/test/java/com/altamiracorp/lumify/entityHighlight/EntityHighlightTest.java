package com.altamiracorp.lumify.entityHighlight;

import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRowKey;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class EntityHighlightTest {
    @Test
    public void testGetHighlightedText() throws Exception {
        ArrayList<TermMention> terms = new ArrayList<TermMention>();
        ArtifactRowKey artifactKey = new ArtifactRowKey("artifact1");
        terms.add(createTermMention("joe ferner", 18, 28, artifactKey));
        terms.add(createTermMention("jeff kunkle", 33, 44, artifactKey));
        List<OffsetItem> termAndTermMetadata = new EntityHighlighter(, ).getTermAndTermMetadataForArtifact(null, terms);
        String highlightText = EntityHighlighter.getHighlightedText("Test highlight of Joe Ferner and Jeff Kunkle.", 0, termAndTermMetadata);
        assertEquals("Test highlight of <span class=\"entity\" title=\"joe ferner\" data-info=\"{&quot;title&quot;:&quot;joe ferner&quot;,&quot;start&quot;:18,&quot;_rowKey&quot;:&quot;artifact1:0000000000000028:0000000000000018&quot;,&quot;type&quot;:&quot;entity&quot;,&quot;end&quot;:28}\">Joe Ferner</span> and <span class=\"entity\" title=\"jeff kunkle\" data-info=\"{&quot;title&quot;:&quot;jeff kunkle&quot;,&quot;start&quot;:33,&quot;_rowKey&quot;:&quot;artifact1:0000000000000044:0000000000000033&quot;,&quot;type&quot;:&quot;entity&quot;,&quot;end&quot;:44}\">Jeff Kunkle</span>.", highlightText);
    }

    private TermMention createTermMention(String sign, int start, int end, ArtifactRowKey artifactKey) {
        TermMention termMention = new TermMention(new TermMentionRowKey(artifactKey.toString(), start, end));
        termMention.getMetadata().setSign(sign);
        return termMention;
    }

    public void testGetHighlightedTextOverlaps() throws Exception {
        ArrayList<TermMention> terms = new ArrayList<TermMention>();
        ArtifactRowKey artifactKey = ArtifactRowKey.build("artifact1".getBytes());
        terms.add(createTermMention("joe ferner", 18, 28, artifactKey));
        terms.add(createTermMention("jeff kunkle", 18, 21, artifactKey));
        List<OffsetItem> termAndTermMetadata = new EntityHighlighter(, ).getTermAndTermMetadataForArtifact(null, terms);
        String highlightText = EntityHighlighter.getHighlightedText("Test highlight of Joe Ferner.", 0, termAndTermMetadata);
        assertEquals("Test highlight of <span class=\"entity person\" term-key=\"joe ferner\\x1Fee\\x1Fperson\"><span class=\"entity person\" term-key=\"joe\\x1Fee\\x1Fperson\">Joe</span> Ferner</span>.", highlightText);
    }

    @Test
    public void testGetHighlightedTextNestedEntity() throws Exception {
        String text = "This is a test sentence";
        List<OffsetItem> offsetItems = new ArrayList<OffsetItem>();

        OffsetItem mockEntity1 = mock(TermMentionOffsetItem.class);
        when(mockEntity1.getStart()).thenReturn(0l);
        when(mockEntity1.getEnd()).thenReturn(4l);
        when(mockEntity1.getCssClasses()).thenReturn(new ArrayList<String>(Arrays.asList(new String[]{"first"})));
        when(mockEntity1.shouldHighlight()).thenReturn(true);
        when(mockEntity1.getInfoJson()).thenReturn(new JSONObject("{\"data\":\"attribute\"}"));
        offsetItems.add(mockEntity1);

        OffsetItem mockEntity2 = mock(TermMentionOffsetItem.class);
        when(mockEntity2.getStart()).thenReturn(0l);
        when(mockEntity2.getEnd()).thenReturn(4l);
        when(mockEntity2.getCssClasses()).thenReturn(new ArrayList<String>(Arrays.asList(new String[]{"second"})));
        when(mockEntity2.shouldHighlight()).thenReturn(true);
        when(mockEntity2.getInfoJson()).thenReturn(new JSONObject("{\"data\":\"attribute\"}"));
        offsetItems.add(mockEntity2);

        OffsetItem mockEntity3 = mock(TermMentionOffsetItem.class);
        when(mockEntity3.getStart()).thenReturn(0l);
        when(mockEntity3.getEnd()).thenReturn(7l);
        when(mockEntity3.getCssClasses()).thenReturn(new ArrayList<String>(Arrays.asList(new String[]{"third"})));
        when(mockEntity3.shouldHighlight()).thenReturn(true);
        when(mockEntity3.getInfoJson()).thenReturn(new JSONObject("{\"data\":\"attribute\"}"));
        offsetItems.add(mockEntity3);

        OffsetItem mockEntity4 = mock(TermMentionOffsetItem.class);
        when(mockEntity4.getStart()).thenReturn(5l);
        when(mockEntity4.getEnd()).thenReturn(9l);
        when(mockEntity4.getCssClasses()).thenReturn(new ArrayList<String>(Arrays.asList(new String[]{"fourth"})));
        when(mockEntity4.shouldHighlight()).thenReturn(true);
        when(mockEntity4.getInfoJson()).thenReturn(new JSONObject("{\"data\":\"attribute\"}"));
        offsetItems.add(mockEntity4);

        OffsetItem mockEntity5 = mock(TermMentionOffsetItem.class);
        when(mockEntity5.getStart()).thenReturn(15l);
        when(mockEntity5.getEnd()).thenReturn(23l);
        when(mockEntity5.getCssClasses()).thenReturn(new ArrayList<String>(Arrays.asList(new String[]{"fifth"})));
        when(mockEntity5.shouldHighlight()).thenReturn(true);
        when(mockEntity5.getInfoJson()).thenReturn(new JSONObject("{\"data\":\"attribute\"}"));
        offsetItems.add(mockEntity5);

        Assert.assertEquals("<span class=\"first\" data-info=\"{&quot;data&quot;:&quot;attribute&quot;}\">This</span> " +
                "<span class=\"fourth\" data-info=\"{&quot;data&quot;:&quot;attribute&quot;}\">is a</span> test <span " +
                "class=\"fifth\" data-info=\"{&quot;data&quot;:&quot;attribute&quot;}\">sentence</span>",
                EntityHighlighter.getHighlightedText(text, 0, offsetItems));
    }
}
