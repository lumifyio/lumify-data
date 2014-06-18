package io.lumify.imageMetadataExtractorTestPlatform;

import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.icc.IccDirectory;
import com.drew.metadata.Metadata;

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
 * NOTE: Only the ExifIFD0Directory and ExifSubIFDDirectory directories will be scanned for dates. The other 8
 * directories will not be scanned for date and time information yet. (Perhaps implement later).
 */
public class DateExtractor {

    public DateExtractor(){

    }

    /**
     * Checks the metadata directories in order until the date is found. The first match is returned.
     *
     * @param metadata
     * @return
     */
    public static String getDateDefault(Metadata metadata){

        Date date;

        //TODO. Check these directories and tags against the excel spreadsheet of constant values.

        ExifIFD0Directory exifIFD0dir = metadata.getDirectory(ExifIFD0Directory.class);
        date = exifIFD0dir.getDate(ExifIFD0Directory.TAG_DATETIME);
        if (date != null)
            return date.toString();

        ExifSubIFDDirectory exifDirectory = metadata.getDirectory(ExifSubIFDDirectory.class);
        date = exifDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        if (date != null)
            return date.toString();

        IccDirectory iccDirectory = metadata.getDirectory(IccDirectory.class);
        date = iccDirectory.getDate(IccDirectory.TAG_ICC_PROFILE_DATETIME);
        if (date != null)
            return date.toString();

        GpsDirectory gpsDirectory = metadata.getDirectory(GpsDirectory.class);
        date = gpsDirectory.getDate(GpsDirectory.TAG_GPS_DATE_STAMP);
        if (date != null)
            return date.toString();

        //TODO.
        return null;
    }



}
