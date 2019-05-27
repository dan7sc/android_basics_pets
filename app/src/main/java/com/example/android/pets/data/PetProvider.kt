package com.example.android.pets.data

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.content.UriMatcher
import com.example.android.pets.data.PetContract.PetEntry
import android.content.ContentUris


/**
 * [ContentProvider] for Pets app.
 */
class PetProvider : ContentProvider() {

    /** Database helper object  */
    private var mDbHelper: PetDbHelper? = null

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
        val match = sUriMatcher.match(uri)
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
        return null
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    override fun update(uri: Uri, contentValues: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
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
