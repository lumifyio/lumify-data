package io.lumify.core.model.ontology;

import io.lumify.core.exception.LumifyException;
import io.lumify.core.model.properties.LumifyProperties;
import org.securegraph.Authorizations;

import java.util.ArrayList;

public class InMemoryConcept extends Concept {
    private String title;
    private String color;
    private String displayName;
    private String displayType;
    private String titleFormula;
    private String subtitleFormula;
    private String timeFormula;
    private String conceptIRI;
    private byte[] glyphIcon;
    private byte[] mapGlyphIcon;
    private boolean userVisible;

    protected InMemoryConcept(String conceptIRI, String parentIRI) {
        super(parentIRI, new ArrayList<OntologyProperty>());
        this.conceptIRI = conceptIRI;
    }

    @Override
    public String getIRI() {
        return this.conceptIRI;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public boolean hasGlyphIconResource() {
        return glyphIcon != null;
    }

    @Override
    public String getColor() {
        return color;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getDisplayType() {
        return displayType;
    }

    @Override
    public String getTitleFormula() {
        return titleFormula;
    }

    @Override
    public String getSubtitleFormula() {
        return this.subtitleFormula;
    }

    @Override
    public String getTimeFormula() {
        return this.timeFormula;
    }

    @Override
    public boolean getUserVisible() {
        return userVisible;
    }

    @Override
    public void setProperty(String name, Object value, Authorizations authorizations) {
        if (OntologyLumifyProperties.COLOR.getPropertyName().equals(name)) {
            this.color = (String) value;
        } else if (OntologyLumifyProperties.DISPLAY_TYPE.getPropertyName().equals(name)) {
            this.displayType = (String) value;
        } else if (OntologyLumifyProperties.TITLE_FORMULA.getPropertyName().equals(name)) {
            this.titleFormula = (String) value;
        } else if (OntologyLumifyProperties.SUBTITLE_FORMULA.getPropertyName().equals(name)) {
            this.subtitleFormula = (String) value;
        } else if (OntologyLumifyProperties.TIME_FORMULA.getPropertyName().equals(name)) {
            this.timeFormula = (String) value;
        } else if (OntologyLumifyProperties.USER_VISIBLE.getPropertyName().equals(name)) {
            this.userVisible = (Boolean) value;
        } else if (LumifyProperties.GLYPH_ICON.getPropertyName().equals(name)) {
            this.glyphIcon = (byte[]) value;
        } else if (LumifyProperties.MAP_GLYPH_ICON.getPropertyName().equals(name)) {
            this.mapGlyphIcon = (byte[]) value;
        } else if (LumifyProperties.TITLE.getPropertyName().equals(name)) {
            this.title = (String) value;
        } else if (LumifyProperties.DISPLAY_NAME.getPropertyName().equals(name)) {
            this.displayName = (String) value;
        } else {
            throw new LumifyException("Set not implemented for property " + name);
        }
    }

    @Override
    public void removeProperty(String name, Authorizations authorizations) {
        if (OntologyLumifyProperties.TITLE_FORMULA.getPropertyName().equals(name)) {
            this.titleFormula = null;
        } else if (OntologyLumifyProperties.SUBTITLE_FORMULA.getPropertyName().equals(name)) {
            this.subtitleFormula = null;
        } else if (OntologyLumifyProperties.TIME_FORMULA.getPropertyName().equals(name)) {
            this.timeFormula = null;
        } else {
            throw new LumifyException("Remove not implemented for property " + name);
        }
    }

    @Override
    public byte[] getGlyphIcon() {
        return glyphIcon;
    }

    @Override
    public byte[] getMapGlyphIcon() {
        return mapGlyphIcon;
    }

    public String getConceptIRI() {
        return conceptIRI;
    }
}
