package io.lumify.imageMetadataHelper;

import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import java.util.Date;

/**
 * Created by jon.hellmann on 6/17/14.
 *
 * Please see the "Date - From These Directories" spreadsheet in order to see which Directories provide which
 * Date and Time fields.
 *
 * More specifically, the spreadsheet will show which constant field values of the
 * metadata extractor are associated with which Date fields.
 *
 *
 */
public class DateExtractorV2 {



    /**
     * Checks the metadata directories in order until the date is found. The first match is returned.
     *
     * NOTE: Only the ExifIFD0Directory and ExifSubIFDDirectory directories will be scanned for dates. The other 8
     * directories will not be scanned for date and time information yet. (Perhaps implement later).
     * @param metadata
     * @return
     */
    public static String getDateDefault(Metadata metadata){

        //TODO. Check these directories and tags against the excel spreadsheet of constant values.

        String dateString = null;

        ExifIFD0Directory exifDir = metadata.getDirectory(ExifIFD0Directory.class);
        if (exifDir != null) {
            dateString = exifDir.getDescription(ExifIFD0Directory.TAG_DATETIME);
            if (dateString != null) {
                //System.out.println("Returning the date string from ExifIFD0.");
                return dateString;
            }
        }

        ExifSubIFDDirectory subDir = metadata.getDirectory(ExifSubIFDDirectory.class);
        if (subDir != null) {
            dateString = subDir.getDescription(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            if (dateString != null) {
                //System.out.println("Returning the date string from Exif--Sub--IFD0.");
                return dateString;
            }
        }

        //If no date was found, send back this String.
        return "no date available";
    }

}
