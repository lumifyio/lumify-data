package io.lumify.imageMetadataExtractor;

import io.lumify.core.model.properties.types.DateLumifyProperty;
import io.lumify.core.model.properties.types.StringLumifyProperty;

/**
 * Created by jon.hellmann on 6/20/14.
 */
public class Ontology {


    public static final StringLumifyProperty ORIENTATION = new StringLumifyProperty("http://lumify.io/exif#orientation");
    public static final StringLumifyProperty ORIENTATION_DESCRIPTION = new StringLumifyProperty("http://lumify.io/exif#orientationdescription");
    public static final DateLumifyProperty DATE_TAKEN = new DateLumifyProperty("http://lumify.io/exif#datetaken");
    public static final StringLumifyProperty DEVICE_MAKE = new StringLumifyProperty("http://lumify.io/exif#devicemake");
    public static final StringLumifyProperty DEVICE_MODEL = new StringLumifyProperty("http://lumify.io/exif#devicemodel");


}