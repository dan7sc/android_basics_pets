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

import android.content.Context
import android.database.Cursor
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.TextView

import com.example.android.pets.data.PetContract.PetEntry


/**
 * [PetCursorAdapter] is an adapter for a list or grid view
 * that uses a [Cursor] of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the [Cursor].
 */
class PetCursorAdapter
/**
 * Constructs a new [PetCursorAdapter].
 *
 * @param context The context
 * @param c       The cursor from which to get the data.
 */
(context: Context, c: Cursor?)/* flags */ : CursorAdapter(context, c, 0) {

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     * moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     * correct row.
     */
    override fun bindView(view: View, context: Context, cursor: Cursor) {
        // Find individual views that we want to modify in the list item layout
        val nameTextView: TextView = view.findViewById<View>(R.id.name) as TextView
        val summaryTextView: TextView = view.findViewById<View>(R.id.summary) as TextView

        // Find the columns of pet attributes that we're interested in
        val nameColumnIndex: Int = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME)
        val breedColumnIndex: Int = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED)

        // Read the pet attributes from the Cursor for the current pet
        val petName: String = cursor.getString(nameColumnIndex)
        var petBreed: String = cursor.getString(breedColumnIndex)

        // If the pet breed is empty string or null, then use some default text
        // that says "Unknown breed", so the TextView isn't blank.
        if (TextUtils.isEmpty(petBreed)) {
            petBreed = context.getString(R.string.unknown_breed)
        }

        // Update the TextViews with the attributes for the current pet
        nameTextView.text = petName
        summaryTextView.text = petBreed
    }
}
