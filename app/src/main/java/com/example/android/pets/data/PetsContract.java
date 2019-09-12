package com.example.android.pets.data;

import android.content.ContentResolver;
import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;

public class PetsContract {

    static final String CONTENT_AUTHORITY = "com.example.android.pets";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PETS = "Pets";

    public static abstract class PetsEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,PATH_PETS);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;

        /**
         * table name and column names
         */
        public static final String TABLE_NAME = "Pets";
        public static final String COLUMN_ID = BaseColumns._ID;
        public static final String COLUMN_NAME= "name";
        public static final String COLUMN_BREED = "breed";
        public static final String COLUMN_GENDER = "gender";
        public static final String COLUMN_WEIGHT = "weight";


        /**
         * Possible values for gender
         */

        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE= 2;
        public static final int GENDER_UNKNOWN= 0;


        public static boolean isValidGender(int gender) {
            if (gender == GENDER_MALE || gender == GENDER_FEMALE || gender == GENDER_UNKNOWN) {
                return true;
            }
            return false;
        }
    }


}
