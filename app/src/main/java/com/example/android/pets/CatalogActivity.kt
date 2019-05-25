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

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

/**
 * Displays list of pets that were entered and stored in the app.
 */
class CatalogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        // Setup FAB to open EditorActivity
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this@CatalogActivity, EditorActivity::class.java)
            startActivity(intent)
        }
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
            R.id.action_insert_dummy_data ->
                // Do nothing for now
                return true
            // Respond to a click on the "Delete all entries" menu option
            R.id.action_delete_all_entries ->
                // Do nothing for now
                return true
        }
        return super.onOptionsItemSelected(item)
    }
}
