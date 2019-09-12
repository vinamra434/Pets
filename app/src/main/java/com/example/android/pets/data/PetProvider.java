package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.pets.data.PetsContract.PetsEntry;

import static android.content.ContentValues.TAG;

/**
 * {@link ContentProvider} for Pets app.
 */
public class PetProvider extends ContentProvider {

    private static final int PETS = 100;
    private static final int PET_ID = 101;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(PetsContract.CONTENT_AUTHORITY, PetsContract.PATH_PETS, PETS);
        uriMatcher.addURI(PetsContract.CONTENT_AUTHORITY, PetsContract.PATH_PETS + "/#", PET_ID);

    }

    /** Tag for the log messages */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    /**
     * Initialize the provider and the database helper object.
     */
    private PetDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = uriMatcher.match(uri);
        switch (match) {
            case PETS:
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.

                cursor = database.query(PetsEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = PetsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(PetsEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // set notification URI on the cursor,
        // so we know what content URi the cursor was created for
        // If the data at this URI changes, we know to update the Cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);

        }

    }

    private Uri insertPet(Uri uri, ContentValues contentValues) {

        if (contentValues.getAsString(PetsEntry.COLUMN_NAME)== null) {
            throw new IllegalArgumentException("Pet name cannot be null");
        }

        Integer weight = contentValues.getAsInteger(PetsEntry.COLUMN_WEIGHT);
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Pet weight cannot be negative");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long rowNumber = database.insert(PetsEntry.TABLE_NAME, null, contentValues);

        if (rowNumber == -1) {
            Log.e(TAG, "failed to insert pet data" + uri);
            return null;
        }

        //notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri,rowNumber);
    }
    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = uriMatcher.match(uri);

        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PET_ID:
                selection = PetsEntry.COLUMN_ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, contentValues,selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not possible for " + uri);

        }
    }

    private int updatePet(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        //check if key is present
        if (contentValues.containsKey(PetsEntry.COLUMN_NAME)) {
            String name = contentValues.getAsString(PetsEntry.COLUMN_NAME);

            //check if value is null
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        if (contentValues.containsKey(PetsEntry.COLUMN_WEIGHT)) {
            Integer weight = contentValues.getAsInteger(PetsEntry.COLUMN_WEIGHT);

            if (weight == null || weight < 0) {
                throw new IllegalArgumentException("Pet requires a proper weight");
            }
        }

        if (contentValues.containsKey(PetsEntry.COLUMN_GENDER)) {
            Integer gender = contentValues.getAsInteger(PetsEntry.COLUMN_GENDER);

            if (gender == null || !PetsEntry.isValidGender(gender)) {
                throw new IllegalArgumentException("Pet requires valid gender");
            }
        }

        if (contentValues.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();


        int rowNumber =  database.update(PetsEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        if(rowNumber != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowNumber;
    }


    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowNumber;
        final int match = uriMatcher.match(uri);
        switch (match) {
            case PETS:
                rowNumber = database.delete(PetsEntry.TABLE_NAME, selection, selectionArgs);

                if(rowNumber != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowNumber;

            case PET_ID:
                selection = PetsEntry.COLUMN_ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowNumber = database.delete(PetsEntry.TABLE_NAME, selection, selectionArgs);

                if(rowNumber != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowNumber;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {

        final int match = uriMatcher.match(uri);

        switch(match) {
            case PETS:
                return PetsEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " +
                        " uri" + "with the match" + match);
        }
    }
}