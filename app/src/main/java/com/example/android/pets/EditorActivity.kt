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
import com.example.android.pets.data.PetContract.PetEntry
import android.widget.Toast
import android.content.ContentValues
import android.content.DialogInterface
import android.database.Cursor
import android.net.Uri
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v4.content.CursorLoader
import android.support.v7.app.AlertDialog


/**
 * Allows user to create a new pet or edit an existing one.
 */
@Suppress("DEPRECATION")
class EditorActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    /** Content URI for the existing pet (null if it's a new pet)  */
    private var mCurrentPetUri: Uri? = null

    /** EditText field to enter the pet's name  */
    private var mNameEditText: EditText? = null

    /** EditText field to enter the pet's breed  */
    private var mBreedEditText: EditText? = null

    /** EditText field to enter the pet's weight  */
    private var mWeightEditText: EditText? = null

    /** EditText field to enter the pet's gender  */
    private var mGenderSpinner: Spinner? = null

    /**
     * Gender of the pet. The possible valid values are in the PetContract.java file:
     * [PetEntry.GENDER_UNKNOWN], [PetEntry.GENDER_MALE], or
     * [PetEntry.GENDER_FEMALE].
     */
    private var mGender = PetEntry.GENDER_UNKNOWN

    /** Boolean flag that keeps track of whether the pet has been edited (true) or not (false)  */
    private var mPetHasChanged = false

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private val mTouchListener = View.OnTouchListener { _, _ ->
        mPetHasChanged = true
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new pet or editing an existing one.
        val intent = intent
        mCurrentPetUri = intent.data

        // If the intent DOES NOT contain a pet content URI, then we know that we are
        // creating a new pet.
        if (mCurrentPetUri == null) {
            // This is a new pet, so change the app bar to say "Add a Pet"
            title = getString(R.string.editor_activity_title_new_pet)

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu()
        } else {
            // Otherwise this is an existing pet, so change app bar to say "Edit Pet"
            title = getString(R.string.editor_activity_title_edit_pet)

            // Initialize a loader to read the pet data from the database
            // and display the current values in the editor
            supportLoaderManager.initLoader(EXISTING_PET_LOADER, null, this)
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById<View>(R.id.edit_pet_name) as EditText
        mBreedEditText = findViewById<View>(R.id.edit_pet_breed) as EditText
        mWeightEditText = findViewById<View>(R.id.edit_pet_weight) as EditText
        mGenderSpinner = findViewById<View>(R.id.spinner_gender) as Spinner

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText!!.setOnTouchListener(mTouchListener)
        mBreedEditText!!.setOnTouchListener(mTouchListener)
        mWeightEditText!!.setOnTouchListener(mTouchListener)
        mGenderSpinner!!.setOnTouchListener(mTouchListener)

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
                        getString(R.string.gender_male) -> PetEntry.GENDER_MALE // Male
                        getString(R.string.gender_female) -> PetEntry.GENDER_FEMALE // Female
                        else -> PetEntry.GENDER_UNKNOWN // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            override fun onNothingSelected(parent: AdapterView<*>) {
                mGender = PetEntry.GENDER_UNKNOWN // Unknown
            }
        }
    }
    /**
     * Get user input from editor and save pet into database.
     */
    private fun savePet() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        val nameString: String = mNameEditText!!.text.toString().trim()
        val breedString: String = mBreedEditText!!.text.toString().trim()
        val weightString: String = mWeightEditText!!.text.toString().trim()

        // Check if this is supposed to be a new pet
        // and check if all the fields in the editor are blank
        if (mCurrentPetUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(breedString) &&
                TextUtils.isEmpty(weightString) && mGender == PetEntry.GENDER_UNKNOWN) {
            // Since no fields were modified, we can return early without creating a new pet.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return
        }

        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        val values = ContentValues()
        values.put(PetEntry.COLUMN_PET_NAME, nameString)
        values.put(PetEntry.COLUMN_PET_BREED, breedString)
        values.put(PetEntry.COLUMN_PET_GENDER, mGender)
        // If the weight is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        var weight = 0
        if (!TextUtils.isEmpty(weightString)) {
            weight = Integer.parseInt(weightString)
        }
        values.put(PetEntry.COLUMN_PET_WEIGHT, weight)

        // Determine if this is a new or existing pet by checking if mCurrentPetUri is null or not
        if (mCurrentPetUri == null) {
            // This is a NEW pet, so insert a new pet into the provider,
            // returning the content URI for the new pet.
            val newUri: Uri? = contentResolver.insert(PetEntry.CONTENT_URI, values)

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_pet_failed),
                        Toast.LENGTH_SHORT).show()
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_pet_successful),
                        Toast.LENGTH_SHORT).show()
            }
        } else {
            // Otherwise this is an EXISTING pet, so update the pet with content URI: mCurrentPetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify.
            val rowsAffected: Int = contentResolver.update(mCurrentPetUri!!, values, null, null)

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_pet_failed),
                        Toast.LENGTH_SHORT).show()
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_pet_successful),
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        menuInflater.inflate(R.menu.menu_editor, menu)
        return true
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentPetUri == null) {
            val menuItem: MenuItem = menu.findItem(R.id.action_delete)
            menuItem.isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // User clicked on a menu option in the app bar overflow menu
        when (item.itemId) {
            // Respond to a click on the "Save" menu option
            R.id.action_save -> {
                // Save pet to database
                savePet()
                // Exit activity
                finish()
                return true
            }
            // Respond to a click on the "Delete" menu option
            R.id.action_delete -> {
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog()
                return true
            }
            // Respond to a click on the "Up" arrow button in the app bar
            android.R.id.home -> {
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(this@EditorActivity)
                    return true
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                val discardButtonClickListener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { _, _ ->
                    // User clicked "Discard" button, navigate to parent activity.
                    NavUtils.navigateUpFromSameTask(this@EditorActivity)
                }

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * This method is called when the back button is pressed.
     */
    override fun onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mPetHasChanged) {
            super.onBackPressed()
            return
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        val discardButtonClickListener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { _, _ ->
            // User clicked "Discard" button, close the current activity.
            finish()
        }

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener)
    }

    override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor> {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        val projection = arrayOf(PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT)

        // This loader will execute the ContentProvider's query method on a background thread
        return CursorLoader(
                this, // Parent activity context
                mCurrentPetUri!!, // Query the content URI for the current pet
                projection,  // Columns to include in the resulting Cursor
                null, // No selection clause
                null, // No selection arguments
                null) // Default sort order
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.count < 1) {
            return
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            val nameColumnIndex: Int = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME)
            val breedColumnIndex: Int = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED)
            val genderColumnIndex: Int = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER)
            val weightColumnIndex: Int = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT)

            // Extract out the value from the Cursor for the given column index
            val name: String = cursor.getString(nameColumnIndex)
            val breed: String = cursor.getString(breedColumnIndex)
            val gender: Int = cursor.getInt(genderColumnIndex)
            val weight: Int = cursor.getInt(weightColumnIndex)

            // Update the views on the screen with the values from the database
            mNameEditText!!.setText(name)
            mBreedEditText!!.setText(breed)
            mWeightEditText!!.setText(Integer.toString(weight))

            // Gender is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            when (gender) {
                PetEntry.GENDER_MALE -> mGenderSpinner!!.setSelection(1)
                PetEntry.GENDER_FEMALE -> mGenderSpinner!!.setSelection(2)
                else -> mGenderSpinner!!.setSelection(0)
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText!!.setText("")
        mBreedEditText!!.setText("")
        mWeightEditText!!.setText("")
        mGenderSpinner!!.setSelection(0) // Select "Unknown" gender
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     * the user confirms they want to discard their changes
     */
    private fun showUnsavedChangesDialog(
            discardButtonClickListener: DialogInterface.OnClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.unsaved_changes_dialog_msg)
        builder.setPositiveButton(R.string.discard, discardButtonClickListener)
        builder.setNegativeButton(R.string.keep_editing) { dialog, _ ->
            // User clicked the "Keep editing" button, so dismiss the dialog
            // and continue editing the pet.
            dialog?.dismiss()
        }

        // Create and show the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }


    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private fun showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.delete_dialog_msg)
        builder.setPositiveButton(R.string.delete, DialogInterface.OnClickListener { _, _ ->
            // User clicked the "Delete" button, so delete the pet.
            deletePet()
        })
        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            // User clicked the "Cancel" button, so dismiss the dialog
            // and continue editing the pet.
            dialog?.dismiss()
        }

        // Create and show the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private fun deletePet() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentPetUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            val rowsDeleted = contentResolver.delete(mCurrentPetUri, null, null)

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_pet_failed),
                        Toast.LENGTH_SHORT).show()
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_pet_successful),
                        Toast.LENGTH_SHORT).show()
            }
        }

        // Close the activity
        finish()
    }

    companion object {

        /** Identifier for the pet data loader  */
        private val EXISTING_PET_LOADER = 0
    }
}
