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
package com.example.android.pets;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.content.CursorLoader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.android.pets.data.PetsContract;


/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * in update mod pet is untouched
     */
    public boolean petHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            petHasChanged = true;
            return false;
        }
    };


    /**
     * EditText field to enter the pet's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the pet's breed
     */
    private EditText mBreedEditText;

    /**
     * EditText field to enter the pet's weight
     */
    private EditText mWeightEditText;

    /**
     * EditText field to enter the pet's gender
     */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    public static final int petLoader = 0;
    Uri currentPetUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        currentPetUri = intent.getData();

        if (currentPetUri == null) {
            //add new pet
            setTitle("Add a new Pet");

            invalidateOptionsMenu();
        }
        else {
            // update current pet
            setTitle(getString(R.string.editor_activity_title_edit_pet));

            getLoaderManager().initLoader(petLoader,null,this);

        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = 1; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = 2; // Female
                    } else {
                        mGender = 0; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                savePet();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                if (!petHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void savePet() {
        int rowNumber = 0;

        if (currentPetUri == null) {
            String nameString = mNameEditText.getText().toString().trim();
            String breedString = mBreedEditText.getText().toString().trim();
            int gender = mGender;
            String weightString = mWeightEditText.getText().toString().trim();

            if (TextUtils.isEmpty(nameString) &&
                    TextUtils.isEmpty(breedString) &&
                    TextUtils.isEmpty(weightString) &&
                    gender == PetsContract.PetsEntry.GENDER_UNKNOWN) {
                return;
            }
            int weight = 0;

            if (!TextUtils.isEmpty(weightString)) {
                weight = Integer.parseInt(weightString);
            }


            ContentValues values = new ContentValues();
            values.put(PetsContract.PetsEntry.COLUMN_NAME, nameString);
            values.put(PetsContract.PetsEntry.COLUMN_BREED, breedString);
            values.put(PetsContract.PetsEntry.COLUMN_GENDER, gender);
            values.put(PetsContract.PetsEntry.COLUMN_WEIGHT, weight);

            Uri uri = getContentResolver().insert(PetsContract.PetsEntry.CONTENT_URI, values);

            if (uri == null) {
                Toast.makeText(this,getString(R.string.editor_insert_pet_not_successful), Toast.LENGTH_SHORT).show();
            }
            else {

                Toast.makeText(this, getString(R.string.editor_insert_pet_successful), Toast.LENGTH_SHORT).show();
            }
        }
        else {
            String name = mNameEditText.getText().toString().trim();
            String breed = mBreedEditText.getText().toString().trim();
            int gender = mGender;
            int weight = Integer.parseInt(mWeightEditText.getText().toString().trim());

            ContentValues values = new ContentValues();
            values.put(PetsContract.PetsEntry.COLUMN_NAME, name);
            values.put(PetsContract.PetsEntry.COLUMN_BREED, breed);
            values.put(PetsContract.PetsEntry.COLUMN_GENDER, gender);
            values.put(PetsContract.PetsEntry.COLUMN_WEIGHT, weight);

            rowNumber  = getContentResolver().update(currentPetUri, values, null, null );

            if (rowNumber == 0) {
                Toast.makeText(this,getString(R.string.editor_update_pet_unsuccessful), Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this,getString(R.string.editor_update_pet_successful), Toast.LENGTH_SHORT).show();
            }

        }


    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                PetsContract.PetsEntry.COLUMN_ID,
                PetsContract.PetsEntry.COLUMN_NAME,
                PetsContract.PetsEntry.COLUMN_BREED,
                PetsContract.PetsEntry.COLUMN_WEIGHT,
                PetsContract.PetsEntry.COLUMN_GENDER };

        return new CursorLoader(this,
                currentPetUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        if (data.moveToNext()) {

            int nameColumnIndex = data.getColumnIndex(PetsContract.PetsEntry.COLUMN_NAME);
            int breedColumnIndex = data.getColumnIndex(PetsContract.PetsEntry.COLUMN_BREED);
            int genderColumnIndex = data.getColumnIndex(PetsContract.PetsEntry.COLUMN_GENDER);
            int weightColumnIndex = data.getColumnIndex(PetsContract.PetsEntry.COLUMN_WEIGHT);

            String name = data.getString(nameColumnIndex);
            String breed = data.getString(breedColumnIndex);
            int gender = data.getInt(genderColumnIndex);
            int weight = data.getInt(weightColumnIndex);

            mNameEditText.setText(name);
            mBreedEditText.setText(breed);
            mWeightEditText.setText(Integer.toString(weight));

            switch (gender) {
                case PetsContract.PetsEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(1);
                    break;

                case PetsContract.PetsEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);
                    break;

                case PetsContract.PetsEntry.GENDER_UNKNOWN:
                    mGenderSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentPetUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    //method to create alert dialog
    private void showUnsavedChangesDialog (DialogInterface.OnClickListener discardButtonOnClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);

        //yes I want to discard
        builder.setPositiveButton(R.string.discard, discardButtonOnClickListener);

        //No I want to keep editing
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //if back button is pressed
    @Override
    public void onBackPressed() {

        if (!petHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonOnClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };
        showUnsavedChangesDialog(discardButtonOnClickListener);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void deletePet() {
        int rowNumber = 0;
        if (currentPetUri != null) {
            rowNumber = getContentResolver().delete(currentPetUri, null, null);
        }

        if (rowNumber == 0) {
            Toast.makeText(this, R.string.editor_delete_pet_failed, Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, R.string.editor_delete_pet_successful, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

}