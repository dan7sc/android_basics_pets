/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets

import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import com.example.android.pets.data.PetContract.PetEntry
import android.content.ContentUris
import android.net.Uri
import android.util.Log
import android.widget.AdapterView


/**
 * Displays list of pets that were entered and stored in the app.
 */
@Suppress("DEPRECATION")
class CatalogActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    /** Adapter for the ListView  */
    private lateinit var mCursorAdapter: PetCursorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        // Setup FAB to open EditorActivity
        val fab: FloatingActionButton = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            val intent = Intent(this@CatalogActivity, EditorActivity::class.java)
            startActivity(intent)
        }

        // Find the ListView which will be populated with the pet data
        val petListView: ListView = findViewById<View>(R.id.list) as ListView

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        val emptyView: View = findViewById<View>(R.id.empty_view)
        petListView.emptyView = emptyView

        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        // There is no pet data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = PetCursorAdapter(this, null)
        petListView.adapter = mCursorAdapter

        // Setup the item click listener
        petListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, id ->
            // Create new intent to go to {@link EditorActivity}
            val intent = Intent(this@CatalogActivity, EditorActivity::class.java)

            // Form the content URI that represents the specific pet that was clicked on,
            // by appending the "id" (passed as input to this method) onto the
            // {@link PetEntry#CONTENT_URI}.
            // For example, the URI would be "content://com.example.android.pets/pets/2"
            // if the pet with ID 2 was clicked on.
            val currentPetUri: Uri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id)

            // Set the URI on the data field of the intent
            intent.data = currentPetUri

            // Launch the {@link EditorActivity} to display the data for the current pet.
            startActivity(intent)
        }

        // Kick off the loader
        supportLoaderManager.initLoader(PET_LOADER, null, this)
    }

    /**
     * Helper method to insert hardcoded pet data into the database. For debugging purposes only.
     */
    private fun insertPet() {
        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
        val values = ContentValues()
        values.put(PetEntry.COLUMN_PET_NAME, "Toto")
        values.put(PetEntry.COLUMN_PET_BREED, "Terrier")
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE)
        values.put(PetEntry.COLUMN_PET_WEIGHT, 7)


        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link PetEntry#CONTENT_URI} to indicate that we want to insert
        // into the pets database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        val newUri: Uri? = contentResolver.insert(PetEntry.CONTENT_URI, values)
    }

    /**
     * Helper method to delete all pets in the database.
     */
    private fun deleteAllPets() {
        val rowsDeleted: Int = contentResolver.delete(PetEntry.CONTENT_URI, null, null)
        Log.v("CatalogActivity", "$rowsDeleted rows deleted from pet database")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        menuInflater.inflate(R.menu.menu_catalog, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // User clicked on a menu option in the app bar overflow menu
        when (item.itemId) {
            // Respond to a click on the "Insert dummy data" menu option
            R.id.action_insert_dummy_data -> {
                insertPet()
                return true
            }
            // Respond to a click on the "Delete all entries" menu option
            R.id.action_delete_all_entries -> {
                deleteAllPets()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor> {
        // Define a projection that specifies the columns from the table we care about.
        val projection = arrayOf(
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED
        )

        // This loader will execute the ContentProvider's query method on a background thread
        return CursorLoader(
                this, // Parent activity context
                PetEntry.CONTENT_URI, // Provider content URI to query
                projection, // Columns to include in the resulting Cursor
                null, // No selection clause
                null, // No selection arguments
                null // Default sort order
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        // Update {@link PetCursorAdapter} with this new cursor containing updated pet data
        mCursorAdapter.swapCursor(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null)
    }

    companion object {
        /** Identifier for the pet data loader  */
        private val PET_LOADER = 0
    }
}
