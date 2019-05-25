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

import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner

/**
 * Allows user to create a new pet or edit an existing one.
 */
class EditorActivity : AppCompatActivity() {

    /** EditText field to enter the pet's name  */
    private var mNameEditText: EditText? = null

    /** EditText field to enter the pet's breed  */
    private var mBreedEditText: EditText? = null

    /** EditText field to enter the pet's weight  */
    private var mWeightEditText: EditText? = null

    /** EditText field to enter the pet's gender  */
    private var mGenderSpinner: Spinner? = null

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private var mGender = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById<View>(R.id.edit_pet_name) as EditText
        mBreedEditText = findViewById<View>(R.id.edit_pet_breed) as EditText
        mWeightEditText = findViewById<View>(R.id.edit_pet_weight) as EditText
        mGenderSpinner = findViewById<View>(R.id.spinner_gender) as Spinner

        setupSpinner()
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private fun setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        val genderSpinnerAdapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item)

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)

        // Apply the adapter to the spinner
        mGenderSpinner!!.adapter = genderSpinnerAdapter

        // Set the integer mSelected to the constant values
        mGenderSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selection: String = parent.getItemAtPosition(position) as String
                if (!TextUtils.isEmpty(selection)) {
                    mGender = when (selection) {
                        getString(R.string.gender_male) -> 1 // Male
                        getString(R.string.gender_female) -> 2 // Female
                        else -> 0 // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            override fun onNothingSelected(parent: AdapterView<*>) {
                mGender = 0 // Unknown
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        menuInflater.inflate(R.menu.menu_editor, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // User clicked on a menu option in the app bar overflow menu
        when (item.itemId) {
            // Respond to a click on the "Save" menu option
            R.id.action_save ->
                // Do nothing for now
                return true
            // Respond to a click on the "Delete" menu option
            R.id.action_delete ->
                // Do nothing for now
                return true
            // Respond to a click on the "Up" arrow button in the app bar
            android.R.id.home -> {
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}