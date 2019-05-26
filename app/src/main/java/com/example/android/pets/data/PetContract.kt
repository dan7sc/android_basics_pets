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

package com.example.android.pets.data

import android.provider.BaseColumns

/**
 * API Contract for the Pets app.
 */
object PetContract {

    /**
     * Inner class that defines constant values for the pets database table.
     * Each entry in the table represents a single pet.
     */
    class PetEntry : BaseColumns {
        companion object {

            /** Name of database table for pets  */
            val TABLE_NAME: String = "pets"

            /**
             * Unique ID number for the pet (only for use in the database table).
             *
             * Type: INTEGER
             */
            val _ID: String = BaseColumns._ID

            /**
             * Name of the pet.
             *
             * Type: TEXT
             */
            val COLUMN_PET_NAME: String = "name"

            /**
             * Breed of the pet.
             *
             * Type: TEXT
             */
            val COLUMN_PET_BREED: String = "breed"

            /**
             * Gender of the pet.
             *
             * The only possible values are [.GENDER_UNKNOWN], [.GENDER_MALE],
             * or [.GENDER_FEMALE].
             *
             * Type: INTEGER
             */
            val COLUMN_PET_GENDER: String = "gender"

            /**
             * Weight of the pet.
             *
             * Type: INTEGER
             */
            val COLUMN_PET_WEIGHT: String = "weight"

            /**
             * Possible values for the gender of the pet.
             */
            val GENDER_UNKNOWN: Int = 0
            val GENDER_MALE: Int = 1
            val GENDER_FEMALE: Int = 2
        }
    }

}
// To prevent someone from accidentally instantiating the contract class,
// give it an empty constructor.

