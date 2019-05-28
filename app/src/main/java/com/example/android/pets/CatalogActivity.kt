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
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import com.example.android.pets.data.PetContract.PetEntry


/**
 * Displays list of pets that were entered and stored in the app.
 */
class CatalogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        // Setup FAB to open EditorActivity
        val fab: FloatingActionButton = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            val intent = Intent(this@CatalogActivity, EditorActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        displayDatabaseInfo()
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private fun displayDatabaseInfo() {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        val projection: Array<String> = arrayOf(
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT
        )

        // Perform a query on the provider using the ContentResolver.
        // Use the {@link PetEntry#CONTENT_URI} to access the pet data.
        val cursor: Cursor? = contentResolver.query(
                PetEntry.CONTENT_URI, // The content URI of the words table
                projection, // The columns to return for each row
                null, // Selection criteria
                null, // Selection criteria
                null) // The sort order for the returned rows

        // Find the ListView which will be populated with the pet data
        val petListView = findViewById<View>(R.id.list) as ListView

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        val emptyView = findViewById<View>(R.id.empty_view)
        petListView.emptyView = emptyView

        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        val adapter = PetCursorAdapter(this, cursor!!)

        // Attach the adapter to the ListView.
        petListView.adapter = adapter
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
        val newUri = contentResolver.insert(PetEntry.CONTENT_URI, values)
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
                displayDatabaseInfo()
                return true
            }
            // Respond to a click on the "Delete all entries" menu option
            R.id.action_delete_all_entries ->
                // Do nothing for now
                return true
        }
        return super.onOptionsItemSelected(item)
    }
}
