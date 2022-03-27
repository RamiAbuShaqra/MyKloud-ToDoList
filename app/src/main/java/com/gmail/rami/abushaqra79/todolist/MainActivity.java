package com.gmail.rami.abushaqra79.todolist;

import static androidx.recyclerview.widget.DividerItemDecoration.VERTICAL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gmail.rami.abushaqra79.todolist.database.ReadWriteDB;
import com.gmail.rami.abushaqra79.todolist.model.TaskEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Main activity
 */
public class MainActivity extends AppCompatActivity implements TaskAdapter.ItemClickListener {

    /**
     * Constant for logging
     */
    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Member variable for the RecyclerView
     */
    private RecyclerView mRecyclerView;

    /**
     * Member variables for the adapter
     */
    private TaskAdapter mAdapter;

    /**
     * ArrayList to store the generated keys for tasks
     */
    private ArrayList<String> mKeysList;

    /**
     * ArrayList to store the keys of tasks that need to be deleted
     */
    private ArrayList<String> mDeletedKeys;

    /**
     * TextView that is displayed when the list is empty
     */
    private TextView mEmptyStateTextView;

    /**
     * Loading spinner that is shown until data is fetched from database
     */
    private ProgressBar mProgressBar;

    /**
     * Member variable for reading from and writing to database
     */
    private ReadWriteDB mReadWriteDB;

    /**
     * A key for specific task
     */
    private String mKey;

    /**
     * Initialize the contents of the Activity's standard options menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    /**
     * This method is called whenever the Settings item in the menu is selected.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get a reference to sharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Get the selected language by user from sharedPreferences
        String language = sharedPreferences.getString(getString(R.string.settings_select_language_key), getString(R.string.settings_select_language_default));

        // Set the content view based on the language selected
        if (language.equals(getString(R.string.settings_english_value))) {
            setContentView(R.layout.activity_main);
        } else {
            setContentView(R.layout.activity_main_arabic);
        }

        // Initialize the views
        mEmptyStateTextView = findViewById(R.id.empty_view);
        mProgressBar = findViewById(R.id.loading_spinner);

        // Initialize the database object
        mReadWriteDB = new ReadWriteDB();

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        // If there is a network connection, fetch data, otherwise display a message to notify
        // the user that there is no connection.
        if (activeNetwork != null && activeNetwork.isConnected()) {
            // Retrieve data from database
            retrieveTasks();
        } else {
            mProgressBar.setVisibility(View.GONE);
            mEmptyStateTextView.setVisibility(View.VISIBLE);
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }

        // Initialize the lists
        mKeysList = new ArrayList<>();
        mDeletedKeys = new ArrayList<>();

        // Initialize the floating action button for deleting tasks
        FloatingActionButton fabDeleteButton = findViewById(R.id.fab_delete);

        // Set the RecyclerView to its corresponding view
        mRecyclerView = findViewById(R.id.recyclerViewTasks);

        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new TaskAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);

        // Divider decoration for list in RecyclerView
        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        mRecyclerView.addItemDecoration(decoration);

        // Attach the adapter to a listener interface so it can detect if any task is selected
        // for deletion so the delete FAB is set to visible.
        mAdapter.setTickForDeleteListener((checkBox, position) -> {
            if (checkBox.isChecked()) {
                fabDeleteButton.setVisibility(View.VISIBLE);
                mDeletedKeys.add(mKeysList.get(position));
            } else {
                fabDeleteButton.setVisibility(View.INVISIBLE);
                mDeletedKeys.remove(mKeysList.get(position));
            }
        });

        // Attach click listener to the deletion FAB
        fabDeleteButton.setOnClickListener(view -> {
            mReadWriteDB.deleteSelectedTasks(mDeletedKeys);

            // Refresh the screen so the changes can take effect
            Intent intent = getIntent();
            finish();
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        /*
         Set the Floating Action Button (FAB) to its corresponding View.
         Attach an OnClickListener to it, so that when it's clicked, a dialog will be created
         to enter task information.
         */
        FloatingActionButton fabAddButton = findViewById(R.id.fab_add);
        fabAddButton.setOnClickListener(view ->
                createAddUpdateDialog(getString(R.string.add_task_header), getString(R.string.add_positive_button), -1));
    }

    @Override
    public void onItemClickListener(int clickedItemIndex) {
        createAddUpdateDialog(getString(R.string.update_task_header), getString(R.string.update_positive_button), clickedItemIndex);
    }

    /**
     * Helper method to get the priority of the task based on the selected CheckBox.
     *
     * @param high True if the high priority is selected.
     * @param medium True if the medium priority is selected.
     * @param low True if the low priority is selected.
     * @return Priority level as an integer.
     */
    public static int getPriority(boolean high, boolean medium, boolean low) {
        int priority = 0;

        if (high) priority = 1;
        if (medium) priority = 2;
        if (low) priority = 3;

        return priority;
    }

    /**
     * This method is called to retrieve the data from the database.
     */
    private void retrieveTasks() {
        List<TaskEntry> entries = new ArrayList<>();

        mReadWriteDB.readExistingTasks(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String description = Objects.requireNonNull(child.child("description")
                                .getValue()).toString();
                        int priority = Integer.parseInt(Objects.requireNonNull(child.child("priority")
                                        .getValue()).toString());

                        TaskEntry task = new TaskEntry(description, priority);
                        entries.add(task);
                        mKeysList.add(child.getKey());
                    }
                    mAdapter.setTasks(entries);
                } else {
                    mRecyclerView.setVisibility(View.GONE);
                    mEmptyStateTextView.setVisibility(View.VISIBLE);
                }
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read data", error.toException());
            }
        });
    }

    /**
     * This method is called to create a dialog for creating new task or updating an
     * existing one.
     *
     * @param header The header of alert dialog ('Add Task' or 'Update Task').
     * @param label The label of the positive button in the alert dialog.
     * @param index The index of selected task to be updated. If is a new task then -1
     */
    private void createAddUpdateDialog(String header, String label, int index){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.add_update_task_dialog, null);

        TextView headerText = dialogView.findViewById(R.id.header_text);
        headerText.setText(header);

        TextInputLayout titleLayout = dialogView.findViewById(R.id.task_title_input_layout);
        EditText taskTitle = dialogView.findViewById(R.id.task_title);

        CheckBox priorityHigh = dialogView.findViewById(R.id.priority_high);
        CheckBox priorityMedium = dialogView.findViewById(R.id.priority_medium);
        CheckBox priorityLow = dialogView.findViewById(R.id.priority_low);

        if (index >= 0) {
            mKey = mKeysList.get(index);

            mReadWriteDB.readSelectedTask(mKey, new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String savedDescription = Objects.requireNonNull(snapshot.child("description")
                                .getValue()).toString();
                        int savedPriority = Integer.parseInt(Objects.requireNonNull(snapshot.child("priority")
                                .getValue()).toString());

                        taskTitle.setText(savedDescription);
                        switch (savedPriority) {
                            case 1:
                                priorityHigh.setChecked(true);
                                break;
                            case 2:
                                priorityMedium.setChecked(true);
                                break;
                            case 3:
                                priorityLow.setChecked(true);
                                break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Failed to read value
                    Log.e(TAG, "Failed to read data", error.toException());
                }
            });
        }

        // Text change listener for the EditText field (task description)
        taskTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                titleLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // OnChecked listener for high priority CheckBox
        priorityHigh.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                priorityHigh.setError(null);
                priorityMedium.setChecked(false);
                priorityLow.setChecked(false);
            }
        });

        // OnChecked listener for medium priority CheckBox
        priorityMedium.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                priorityHigh.setError(null);
                priorityHigh.setChecked(false);
                priorityLow.setChecked(false);
            }
        });

        // OnChecked listener for low priority CheckBox
        priorityLow.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                priorityHigh.setError(null);
                priorityHigh.setChecked(false);
                priorityMedium.setChecked(false);
            }
        });

        builder.setView(dialogView)
                .setPositiveButton(label, (dialog, i) -> {
                })
                .setNegativeButton("Cancel", (dialog, i) -> {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            boolean wantToCloseDialog = false;

            String title = taskTitle.getText().toString();
            if (!title.equals("") &&
                    (priorityHigh.isChecked() || priorityMedium.isChecked() || priorityLow.isChecked())) {

                int priority = getPriority(priorityHigh.isChecked(),
                        priorityMedium.isChecked(), priorityLow.isChecked());

                if (label.equals(getString(R.string.add_positive_button))) {
                    TaskEntry taskEntry = new TaskEntry(title, priority);

                    int size = mKeysList.size();
                    if (size == 0) {
                        mReadWriteDB.addTask(taskEntry, size);
                    } else {
                        int lastID = Integer.parseInt(mKeysList.get(size - 1));
                        mReadWriteDB.addTask(taskEntry, lastID);
                    }
                }

                if (label.equals(getString(R.string.update_positive_button))) {
                    mReadWriteDB.updateSelectedTask(mKey, title, priority);
                }

                wantToCloseDialog = true;
            } else {
                if (title.equals("")) {
                    titleLayout.setError("Enter task title");
                }
                if (!priorityHigh.isChecked() && !priorityMedium.isChecked() && !priorityLow.isChecked()) {
                    priorityHigh.setError("Choose task priority");
                }
            }

            if (wantToCloseDialog) {
                alertDialog.dismiss();

                // Refresh the screen so the changes can take effect
                Intent intent = getIntent();
                finish();
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }
}