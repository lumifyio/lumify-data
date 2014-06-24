package io.lumify.imageMetadataExtractorTestPlatform;

import java.util.TimeZone;

/**
 * Created by jon.hellmann on 6/23/14.
 */
public class Testing {

    public static void main(String[] args) {
        new Testing();
    }

    public Testing(){
        //Testing. Get time zone offset.

        TimeZone tz = TimeZone.getTimeZone("GMT");
        System.out.println("tz.getRawOffset(): " + tz.getRawOffset());

        TimeZone timeZone = TimeZone.getDefault();
        System.out.println("timeZone.getRawOffset(): " + timeZone.getRawOffset());


    }
}
