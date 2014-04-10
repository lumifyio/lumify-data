package com.altamiracorp.lumify.facebook;

import com.altamiracorp.lumify.core.model.properties.types.DateLumifyProperty;
import com.altamiracorp.lumify.core.model.properties.types.TextLumifyProperty;
import com.altamiracorp.securegraph.TextIndexHint;

public interface FacebookConstants {
    public static final String POST_CONCEPT = "http://lumify.io/facebook#facebookPost";
    public static final String NAME = "name";
    public static final String UID = "uid";
    public static final String SEX = "sex";
    public static final TextLumifyProperty GENDER = new TextLumifyProperty("gender", TextIndexHint.EXACT_MATCH);
    public static final String EMAIL = "email";
    public static final String EMAIL_ADDRESS = "emailAddress";
    public static final String EMAIL_RELATIONSHIP = "personHasEmailAddress";
    public static final String BIRTHDAY_DATE = "birthday_date";
    public static final DateLumifyProperty BIRTHDAY = new DateLumifyProperty("birthday");
    public static final String PIC = "pic";
    public static final String BIRTHDAY_FORMAT = "MM/dd";
    public static final String USERNAME = "username";
    public static final String FACEBOOK_PROFILE_IMAGE = "http://lumify.io/facebook#facebookProfileImage";
    public static final String GZIP_EXTENSION = ".gz";
    public static final TextLumifyProperty PROFILE_ID = new TextLumifyProperty("profileId", TextIndexHint.EXACT_MATCH);
    public static final String COORDS = "coords";
    public static final String TIMESTAMP = "timestamp";
    public static final String POSTED_RELATIONSHIP = "postPostedByProfile";
    public static final String MENTIONED_RELATIONSHIP = "postMentionedProfile";
    public static final String FACEBOOK = "Facebook";
    public static final String FACEBOOK_POST = "post";
    public static final String MESSAGE = "message";
    public static final String FACEBOOK_PROFILE = "http://lumify.io/facebook#facebookProfile";
    public static final String APP_ID = "facebook.appId";
    public static final String APP_SECRET = "facebook.appSecret";
    public static final String ACCESS_TOKEN = "facebook.accessToken";
    public static final String USER_PERMISSION = "facebook.userPermissions";
    public static final String LOCATION_PERMISSION = "facebook.locationPermissions";
    public static final String LATITUDE = "facebook.latitude";
    public static final String LONGITUDE = "facebook.longitude";
    public static final String DISTANCE = "facebook.distance";
    public static final String AUTHOR_UID = "author_uid";
    public static final String TAGGED_UIDS = "tagged_uids";
    public static final String ENTITY_HAS_IMAGE_PROFILE_PHOTO = "entityHasImageFacebookProfileImage";
    public static final String PROFILE_CONTAINS_IMAGE_OF_ENTITY ="facebookProfileImageContainsImageOfEntity";
    public static final String FACEBOOK_VERTEX_ID = "FB-USER-";
}
