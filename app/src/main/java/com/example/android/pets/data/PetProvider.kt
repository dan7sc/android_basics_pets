package com.example.android.pets.data

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.content.UriMatcher
import com.example.android.pets.data.PetContract.PetEntry
import android.content.ContentUris
import android.database.sqlite.SQLiteDatabase
import android.util.Log


/**
 * [ContentProvider] for Pets app.
 */
class PetProvider : ContentProvider() {

    /** Database helper object  */
    private var mDbHelper: PetDbHelper? = null

    /** Tag for the log messages  */
    private val LOG_TAG = PetProvider::class.java.simpleName

    /** URI matcher code for the content URI for the pets table  */
    private val PETS = 100

    /** URI matcher code for the content URI for a single pet in the pets table  */
    private val PET_ID = 101

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    // Static initializer. This is run the first time anything is called from this class.
    init {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.pets/pets" will map to the
        // integer code {@link #PETS}. This URI is used to provide access to MULTIPLE rows
        // of the pets table.
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS)

        // The content URI of the form "content://com.example.android.pets/pets/#" will map to the
        // integer code {@link #PET_ID}. This URI is used to provide access to ONE single row
        // of the pets table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.pets/pets/3" matches, but
        // "content://com.example.android.pets/pets" (without a number at the end) doesn't match.
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID)
    }

    /**
     * Initialize the provider and the database helper object.
     */
    override fun onCreate(): Boolean {
        mDbHelper = PetDbHelper(context!!)
        return true
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?,
                       sortOrder: String?): Cursor? {

        var selection: String? = selection
        var selectionArgs: Array<String>? = selectionArgs
        // Get readable database
        val database = mDbHelper!!.readableDatabase

        // This cursor will hold the result of the query
        val cursor: Cursor

        // Figure out if the URI matcher can match the URI to a specific code
        val match: Int? = sUriMatcher.match(uri)
        when (match) {
            PETS ->
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = database.query(
                        PetEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                )
            PET_ID -> {
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = PetEntry._ID + "=?"
                selectionArgs = arrayOf(ContentUris.parseId(uri).toString())

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder)
            }
            else -> throw IllegalArgumentException("Cannot query unknown URI $uri")
        }
        return cursor
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    override fun insert(uri: Uri, contentValues: ContentValues?): Uri? {
        val match: Int? = sUriMatcher.match(uri)
        when (match) {
            PETS -> return insertPet(uri, contentValues!!)
            else -> throw IllegalArgumentException("Insertion is not supported for $uri")
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private fun insertPet(uri: Uri, values: ContentValues): Uri? {
        // Check that the name is not null
        val name: String? = values.getAsString(PetEntry.COLUMN_PET_NAME)
        if (name == null) {
            throw IllegalArgumentException("Pet requires a name")
        }

        // Check that the gender is valid
        val gender: Int? = values.getAsInteger(PetEntry.COLUMN_PET_GENDER)
        if (gender == null || !PetEntry.isValidGender(gender)) {
            throw IllegalArgumentException("Pet requires valid gender")
        }

        // If the weight is provided, check that it's greater than or equal to 0 kg
        val weight: Int? = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT)
        if (weight != null && weight < 0) {
            throw IllegalArgumentException("Pet requires valid weight")
        }

        // No need to check the breed, any value is valid (including null).

        // Get writeable database
        val database: SQLiteDatabase = mDbHelper!!.writableDatabase

        // Insert the new pet with the given values
        val id = database.insert(PetEntry.TABLE_NAME, null, values)
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id.equals(-1)) {
            Log.e(LOG_TAG, "Failed to insert row for $uri")
            return null
        }

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id)
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    override fun update(uri: Uri, contentValues: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {
        var selection: String? = selection
        var selectionArgs: Array<String>? = selectionArgs
        val match: Int? = sUriMatcher.match(uri)
        return when (match) {
            PETS -> updatePet(uri, contentValues!!, selection, selectionArgs)
            PET_ID -> {
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PetEntry._ID + "=?"
                selectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                updatePet(uri, contentValues!!, selection, selectionArgs)
            }
            else -> throw IllegalArgumentException("Update is not supported for $uri")
        }
    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private fun updatePet(uri: Uri, values: ContentValues, selection: String?, selectionArgs: Array<String>?): Int {
        // If the {@link PetEntry#COLUMN_PET_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(PetEntry.COLUMN_PET_NAME)) {
            val name = values.getAsString(PetEntry.COLUMN_PET_NAME)
            if (name == null) {
                throw IllegalArgumentException("Pet requires a name");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(PetEntry.COLUMN_PET_GENDER)) {
            val gender: Int? = values.getAsInteger(PetEntry.COLUMN_PET_GENDER)
            if (gender == null || !PetEntry.isValidGender(gender)) {
                throw IllegalArgumentException("Pet requires valid gender")
            }
        }

        // If the {@link PetEntry#COLUMN_PET_WEIGHT} key is present,
        // check that the weight value is valid.
        if (values.containsKey(PetEntry.COLUMN_PET_WEIGHT)) {
            // Check that the weight is greater than or equal to 0 kg
            val weight: Int? = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT)
            if (weight != null && weight < 0) {
                throw IllegalArgumentException("Pet requires valid weight")
            }
        }

        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0
        }

        // Otherwise, get writeable database to update the data
        val database: SQLiteDatabase = mDbHelper!!.getWritableDatabase()

        // Returns the number of database rows affected by the update statement
        return database.update(PetEntry.TABLE_NAME, values, selection, selectionArgs)
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    override fun getType(uri: Uri): String? {
        return null
    }

    companion object {

        /** Tag for the log messages  */
        val LOG_TAG = PetProvider::class.java.simpleName
    }
}
