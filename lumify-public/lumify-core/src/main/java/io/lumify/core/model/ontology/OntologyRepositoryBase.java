package io.lumify.core.model.ontology;

import com.google.common.io.Files;
import io.lumify.core.exception.LumifyException;
import io.lumify.core.model.properties.LumifyProperties;
import io.lumify.core.util.LumifyLogger;
import io.lumify.core.util.LumifyLoggerFactory;
import net.lingala.zip4j.core.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.coode.owlapi.rdf.rdfxml.RDFXMLRenderer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.securegraph.TextIndexHint;
import org.securegraph.property.StreamingPropertyValue;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.ReaderDocumentSource;
import org.semanticweb.owlapi.model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public abstract class OntologyRepositoryBase implements OntologyRepository {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(OntologyRepositoryBase.class);

    public void defineOntology() {
        Concept rootConcept = getOrCreateConcept(null, OntologyRepository.ROOT_CONCEPT_IRI, "root");
        Concept entityConcept = getOrCreateConcept(rootConcept, OntologyRepository.ENTITY_CONCEPT_IRI, "thing");
        addEntityGlyphIcon(entityConcept);
        importBaseOwlFile();
    }

    private void importBaseOwlFile() {
        importResourceOwl("base.owl", "http://lumify.io");
        importResourceOwl("user.owl", "http://lumify.io/user");
        importResourceOwl("workspace.owl", "http://lumify.io/workspace");
    }

    private void importResourceOwl(String fileName, String iri) {
        InputStream baseOwlFile = OntologyRepositoryBase.class.getResourceAsStream(fileName);
        checkNotNull(baseOwlFile, "Could not load resource " + OntologyRepositoryBase.class.getResource(fileName));

        try {
            importFile(baseOwlFile, IRI.create(iri), null);
        } catch (Exception e) {
            throw new LumifyException("Could not import ontology file", e);
        } finally {
            IOUtils.closeQuietly(baseOwlFile);
        }
    }

    private void addEntityGlyphIcon(Concept entityConcept) {
        InputStream entityGlyphIconInputStream = OntologyRepositoryBase.class.getResourceAsStream("entity.png");
        checkNotNull(entityGlyphIconInputStream, "Could not load resource " + OntologyRepositoryBase.class.getResource("entity.png"));

        try {
            ByteArrayOutputStream imgOut = new ByteArrayOutputStream();
            IOUtils.copy(entityGlyphIconInputStream, imgOut);

            byte[] rawImg = imgOut.toByteArray();

            addEntityGlyphIconToEntityConcept(entityConcept, rawImg);
        } catch (IOException e) {
            throw new LumifyException("invalid stream for glyph icon");
        }
    }

    protected abstract void addEntityGlyphIconToEntityConcept(Concept entityConcept, byte[] rawImg);

    public boolean isOntologyDefined() {
        try {
            Concept concept = getConceptByIRI(OntologyRepository.ROOT_CONCEPT_IRI);
            return concept != null; // todo should check for more
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains(OntologyLumifyProperties.ONTOLOGY_TITLE.getKey())) {
                return false;
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public void importFile(File inFile, IRI documentIRI) throws Exception {
        if (!inFile.exists()) {
            throw new LumifyException("File " + inFile + " does not exist");
        }
        File inDir = inFile.getParentFile();

        FileInputStream inFileIn = new FileInputStream(inFile);
        try {
            importFile(inFileIn, documentIRI, inDir);
        } finally {
            inFileIn.close();
        }
    }

    private void importFile(InputStream in, IRI documentIRI, File inDir) throws Exception {
        byte[] inFileData = IOUtils.toByteArray(in);

        Reader inFileReader = new InputStreamReader(new ByteArrayInputStream(inFileData));

        OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
        OWLOntologyManager m = createOwlOntologyManager(config, documentIRI);

        OWLOntologyDocumentSource documentSource = new ReaderDocumentSource(inFileReader, documentIRI);
        OWLOntology o = m.loadOntologyFromOntologyDocument(documentSource, config);

        for (OWLClass ontologyClass : o.getClassesInSignature()) {
            if (!o.isDeclared(ontologyClass, false)) {
                continue;
            }
            importOntologyClass(o, ontologyClass, inDir);
        }

        for (OWLDataProperty dataTypeProperty : o.getDataPropertiesInSignature()) {
            if (!o.isDeclared(dataTypeProperty, false)) {
                continue;
            }
            importDataProperty(o, dataTypeProperty);
        }

        for (OWLObjectProperty objectProperty : o.getObjectPropertiesInSignature()) {
            if (!o.isDeclared(objectProperty, false)) {
                continue;
            }
            importObjectProperty(o, objectProperty);
        }

        storeOntologyFile(new ByteArrayInputStream(inFileData), documentIRI);
    }

    public OWLOntologyManager createOwlOntologyManager(OWLOntologyLoaderConfiguration config, IRI excludeDocumentIRI) throws Exception {
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
        loadOntologyFiles(m, config, excludeDocumentIRI);
        return m;
    }

    protected abstract void storeOntologyFile(InputStream inputStream, IRI documentIRI);

    public void exportOntology(OutputStream out, IRI documentIRI) throws Exception {
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
        config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);

        List<OWLOntology> loadedOntologies = loadOntologyFiles(m, config, null);
        OWLOntology o = findOntology(loadedOntologies, documentIRI);
        if (o == null) {
            throw new LumifyException("Could not find ontology with iri " + documentIRI);
        }

        Writer fileWriter = new OutputStreamWriter(out);

        new RDFXMLRenderer(o, fileWriter).render();
    }

    protected abstract List<OWLOntology> loadOntologyFiles(OWLOntologyManager m, OWLOntologyLoaderConfiguration config, IRI excludedIRI) throws Exception;

    private OWLOntology findOntology(List<OWLOntology> loadedOntologies, IRI documentIRI) {
        for (OWLOntology o : loadedOntologies) {
            if (documentIRI.equals(o.getOntologyID().getOntologyIRI())) {
                return o;
            }
        }
        return null;
    }

    protected Concept importOntologyClass(OWLOntology o, OWLClass ontologyClass, File inDir) throws IOException {
        String uri = ontologyClass.getIRI().toString();
        if ("http://www.w3.org/2002/07/owl#Thing".equals(uri)) {
            return getEntityConcept();
        }

        String label = getLabel(o, ontologyClass);
        checkNotNull(label, "label cannot be null or empty: " + uri);
        LOGGER.info("Importing ontology class " + uri + " (label: " + label + ")");

        Concept parent = getParentConcept(o, ontologyClass, inDir);
        Concept result = getOrCreateConcept(parent, uri, label);

        String color = getColor(o, ontologyClass);
        if (color != null) {
            result.setProperty(OntologyLumifyProperties.COLOR.getKey(), color, OntologyRepository.VISIBILITY.getVisibility());
        }

        String displayType = getDisplayType(o, ontologyClass);
        if (displayType != null) {
            result.setProperty(OntologyLumifyProperties.DISPLAY_TYPE.getKey(), displayType, OntologyRepository.VISIBILITY.getVisibility());
        }

        String titleFormula = getTitleFormula(o, ontologyClass);
        if (titleFormula != null) {
            result.setProperty(OntologyLumifyProperties.TITLE_FORMULA.getKey(), titleFormula, OntologyRepository.VISIBILITY.getVisibility());
        }

        String glyphIconFileName = getGlyphIconFileName(o, ontologyClass);
        setIconProperty(result, inDir, glyphIconFileName, LumifyProperties.GLYPH_ICON.getKey());

        String mapGlyphIconFileName = getMapGlyphIconFileName(o, ontologyClass);
        setIconProperty(result, inDir, mapGlyphIconFileName, LumifyProperties.MAP_GLYPH_ICON.getKey());

        return result;
    }

    private void setIconProperty(Concept concept, File inDir, String glyphIconFileName, String propertyKey) throws IOException {
        if (glyphIconFileName != null) {
            File iconFile = new File(inDir, glyphIconFileName);
            if (!iconFile.exists()) {
                throw new RuntimeException("Could not find icon file: " + iconFile.toString());
            }
            InputStream iconFileIn = new FileInputStream(iconFile);
            try {
                StreamingPropertyValue value = new StreamingPropertyValue(iconFileIn, byte[].class);
                value.searchIndex(false);
                value.store(true);
                concept.setProperty(propertyKey, value, OntologyRepository.VISIBILITY.getVisibility());
            } finally {
                iconFileIn.close();
            }
        }
    }

    protected Concept getParentConcept(OWLOntology o, OWLClass ontologyClass, File inDir) throws IOException {
        Set<OWLClassExpression> superClasses = ontologyClass.getSuperClasses(o);
        if (superClasses.size() == 0) {
            return getEntityConcept();
        } else if (superClasses.size() == 1) {
            OWLClassExpression superClassExpr = superClasses.iterator().next();
            OWLClass superClass = superClassExpr.asOWLClass();
            String superClassUri = superClass.getIRI().toString();
            Concept parent = getConceptByIRI(superClassUri);
            if (parent != null) {
                return parent;
            }

            parent = importOntologyClass(o, superClass, inDir);
            if (parent == null) {
                throw new LumifyException("Could not find or create parent: " + superClass);
            }
            return parent;
        } else {
            throw new LumifyException("Unhandled multiple super classes. Found " + superClasses.size() + ", expected 0 or 1.");
        }
    }

    protected void importDataProperty(OWLOntology o, OWLDataProperty dataTypeProperty) {
        String propertyIRI = dataTypeProperty.getIRI().toString();
        String propertyDisplayName = getLabel(o, dataTypeProperty);
        PropertyType propertyType = getPropertyType(o, dataTypeProperty);
        boolean userVisible = getUserVisible(o, dataTypeProperty);
        boolean searchable = getSearchable (o, dataTypeProperty);
        if (propertyType == null) {
            throw new LumifyException("Could not get property type on data property " + propertyIRI);
        }

        for (OWLClassExpression domainClassExpr : dataTypeProperty.getDomains(o)) {
            OWLClass domainClass = domainClassExpr.asOWLClass();
            String domainClassUri = domainClass.getIRI().toString();
            Concept domainConcept = getConceptByIRI(domainClassUri);
            checkNotNull(domainConcept, "Could not find class with uri: " + domainClassUri);

            LOGGER.info("Adding data property " + propertyIRI + " to class " + domainConcept.getTitle());

            ArrayList<PossibleValueType> possibleValues = getPossibleValues(o, dataTypeProperty);
            Collection<TextIndexHint> textIndexHints = getTextIndexHints(o, dataTypeProperty);
            addPropertyTo(domainConcept, propertyIRI, propertyDisplayName, propertyType, possibleValues, textIndexHints, userVisible, searchable);
        }
    }

    protected abstract OntologyProperty addPropertyTo(
            Concept concept,
            String propertyIRI,
            String displayName,
            PropertyType dataType,
            ArrayList<PossibleValueType> possibleValues,
            Collection<TextIndexHint> textIndexHints,
            boolean userVisible,
            boolean searchable);

    protected void importObjectProperty(OWLOntology o, OWLObjectProperty objectProperty) {
        String uri = objectProperty.getIRI().toString();
        String label = getLabel(o, objectProperty);
        checkNotNull(label, "label cannot be null or empty for " + uri);
        LOGGER.info("Importing ontology object property " + uri + " (label: " + label + ")");

        for (Concept domain : getDomainsConcepts(o, objectProperty)) {
            for (Concept range : getRangesConcepts(o, objectProperty)) {
                getOrCreateRelationshipType(domain, range, uri, label);
            }
        }
    }

    private Iterable<Concept> getRangesConcepts(OWLOntology o, OWLObjectProperty objectProperty) {
        String uri = objectProperty.getIRI().toString();
        if (objectProperty.getRanges(o).size() == 0) {
            throw new LumifyException("Invalid number of range properties on " + uri);
        }

        List<Concept> ranges = new ArrayList<Concept>();
        for (OWLClassExpression rangeClassExpr : objectProperty.getRanges(o)) {
            OWLClass rangeClass = rangeClassExpr.asOWLClass();
            String rangeClassUri = rangeClass.getIRI().toString();
            Concept ontologyClass = getConceptByIRI(rangeClassUri);
            checkNotNull(ontologyClass, "Could not find class with uri: " + rangeClassUri);
            ranges.add(ontologyClass);
        }
        return ranges;
    }

    private Iterable<Concept> getDomainsConcepts(OWLOntology o, OWLObjectProperty objectProperty) {
        String uri = objectProperty.getIRI().toString();
        if (objectProperty.getDomains(o).size() == 0) {
            throw new LumifyException("Invalid number of domain properties on " + uri);
        }

        List<Concept> domains = new ArrayList<Concept>();
        for (OWLClassExpression rangeClassExpr : objectProperty.getDomains(o)) {
            OWLClass rangeClass = rangeClassExpr.asOWLClass();
            String rangeClassUri = rangeClass.getIRI().toString();
            Concept ontologyClass = getConceptByIRI(rangeClassUri);
            checkNotNull(ontologyClass, "Could not find class with uri: " + rangeClassUri);
            domains.add(ontologyClass);
        }
        return domains;
    }

    protected PropertyType getPropertyType(OWLOntology o, OWLDataProperty dataTypeProperty) {
        Set<OWLDataRange> ranges = dataTypeProperty.getRanges(o);
        if (ranges.size() == 0) {
            return null;
        }
        if (ranges.size() > 1) {
            throw new LumifyException("Unexpected number of ranges on data property " + dataTypeProperty.getIRI().toString());
        }
        for (OWLDataRange range : ranges) {
            if (range instanceof OWLDatatype) {
                OWLDatatype datatype = (OWLDatatype) range;
                return getPropertyType(datatype.getIRI().toString());
            }
        }
        throw new LumifyException("Could not find range on data property " + dataTypeProperty.getIRI().toString());
    }

    private PropertyType getPropertyType(String iri) {
        if ("http://www.w3.org/2001/XMLSchema#string".equals(iri)) {
            return PropertyType.STRING;
        }
        if ("http://www.w3.org/2001/XMLSchema#dateTime".equals(iri)) {
            return PropertyType.DATE;
        }
        if ("http://www.w3.org/2001/XMLSchema#int".equals(iri)) {
            return PropertyType.DOUBLE;
        }
        if ("http://www.w3.org/2001/XMLSchema#double".equals(iri)) {
            return PropertyType.DOUBLE;
        }
        if ("http://lumify.io#geolocation".equals(iri)) {
            return PropertyType.GEO_LOCATION;
        }
        if ("http://lumify.io#currency".equals(iri)) {
            return PropertyType.CURRENCY;
        }
        if ("http://lumify.io#image".equals(iri)) {
            return PropertyType.IMAGE;
        }
        if ("http://www.w3.org/2001/XMLSchema#hexBinary".equals(iri)) {
            return PropertyType.BINARY;
        }
        if ("http://www.w3.org/2001/XMLSchema#boolean".equals(iri)) {
            return PropertyType.BOOLEAN;
        }
        if ("http://www.w3.org/2001/XMLSchema#integer".equals(iri)) {
            return PropertyType.INTEGER;
        }
        throw new LumifyException("Unhandled property type " + iri);
    }

    public static String getLabel(OWLOntology o, OWLEntity owlEntity) {
        for (OWLAnnotation annotation : owlEntity.getAnnotations(o)) {
            if (annotation.getProperty().isLabel()) {
                OWLLiteral value = (OWLLiteral) annotation.getValue();
                return value.getLiteral();
            }
        }
        return owlEntity.getIRI().toString();
    }

    protected String getColor(OWLOntology o, OWLEntity owlEntity) {
        return getAnnotationValueByUri(o, owlEntity, OntologyLumifyProperties.COLOR.getKey());
    }

    protected String getDisplayType(OWLOntology o, OWLEntity owlEntity) {
        return getAnnotationValueByUri(o, owlEntity, OntologyLumifyProperties.DISPLAY_TYPE.getKey());
    }

    protected String getTitleFormula(OWLOntology o, OWLEntity owlEntity) {
        return getAnnotationValueByUri(o, owlEntity, OntologyLumifyProperties.TITLE_FORMULA.getKey());
    }

    protected boolean getUserVisible(OWLOntology o, OWLEntity owlEntity) {
        String val = getAnnotationValueByUri(o, owlEntity, OntologyLumifyProperties.USER_VISIBLE.getKey());
        return val == null || Boolean.parseBoolean(val);
    }

    protected boolean getSearchable(OWLOntology o, OWLEntity owlEntity) {
        String val = getAnnotationValueByUri(o, owlEntity, "http://lumify.io#searchable");
        return  val == null || Boolean.parseBoolean(val);
    }

    protected String getGlyphIconFileName(OWLOntology o, OWLEntity owlEntity) {
        return getAnnotationValueByUri(o, owlEntity, OntologyLumifyProperties.GLYPH_ICON_FILE_NAME.getKey());
    }

    protected String getMapGlyphIconFileName(OWLOntology o, OWLEntity owlEntity) {
        return getAnnotationValueByUri(o, owlEntity, OntologyLumifyProperties.MAP_GLYPH_ICON_FILE_NAME.getKey());
    }

    protected ArrayList<PossibleValueType> getPossibleValues(OWLOntology o, OWLEntity owlEntity) {
        return getAnnotationValuesByUri(o, owlEntity, OntologyLumifyProperties.POSSIBLE_VALUES.getKey());
    }

    protected Collection<TextIndexHint> getTextIndexHints(OWLOntology o, OWLDataProperty owlEntity) {
        return TextIndexHint.parse(getAnnotationValueByUri(o, owlEntity, OntologyLumifyProperties.TEXT_INDEX_HINTS.getKey()));
    }

    protected ArrayList<PossibleValueType> getAnnotationValuesByUri(OWLOntology o, OWLEntity owlEntity, String uri) {
        ArrayList<PossibleValueType> possibleValueTypes = new ArrayList<PossibleValueType>();
        for (OWLAnnotation annotation : owlEntity.getAnnotations(o)) {
            if (annotation.getProperty().getIRI().toString().equals(uri)) {
                OWLLiteral value = (OWLLiteral) annotation.getValue();
                String[] possibleValues = value.getLiteral().split(",");
                for (String possibleValue : possibleValues) {
                    String[] parsedValue = possibleValue.split(":");
                    checkArgument(parsedValue.length == 2, "Possible values must be in the format mapping:value");
                    possibleValueTypes.add(new PossibleValueType(parsedValue[0], parsedValue[1]));
                }
            }
        }
        return possibleValueTypes;
    }

    private String getAnnotationValueByUri(OWLOntology o, OWLEntity owlEntity, String uri) {
        for (OWLAnnotation annotation : owlEntity.getAnnotations(o)) {
            if (annotation.getProperty().getIRI().toString().equals(uri)) {
                OWLLiteral value = (OWLLiteral) annotation.getValue();
                return value.getLiteral();
            }
        }
        return null;
    }

    @Override
    public void writePackage(File file, IRI documentIRI) throws Exception {
        ZipFile zipped = new ZipFile(file);
        if (zipped.isValidZipFile()) {
            File tempDir = Files.createTempDir();
            try {
                LOGGER.info("Extracting: %s to %s", file.getAbsoluteFile(), tempDir.getAbsolutePath());
                zipped.extractAll(tempDir.getAbsolutePath());

                File owlFile = findOwlFile(tempDir);
                importFile(owlFile, documentIRI);
            } finally {
                FileUtils.deleteDirectory(tempDir);
            }
        } else {
            importFile(file, documentIRI);
        }
    }

    protected File findOwlFile(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        for (File child : files) {
            if (child.isDirectory()) {
                File found = findOwlFile(child);
                if (found != null) {
                    return found;
                }
            } else if (child.getName().toLowerCase().endsWith(".owl")) {
                return child;
            }
        }
        return null;
    }

    @Override
    public void resolvePropertyIds(JSONArray filterJson) throws JSONException {
        for (int i = 0; i < filterJson.length(); i++) {
            JSONObject filter = filterJson.getJSONObject(i);
            if (filter.has("propertyId") && !filter.has("propertyName")) {
                String propertyVertexId = filter.getString("propertyId");
                OntologyProperty property = getProperty(propertyVertexId);
                if (property == null) {
                    throw new RuntimeException("Could not find property with id: " + propertyVertexId);
                }
                filter.put("propertyName", property.getTitle());
                filter.put("propertyDataType", property.getDataType());
            }
        }
    }
}
