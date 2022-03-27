package com.gmail.rami.abushaqra79.todolist.database;

import com.gmail.rami.abushaqra79.todolist.model.TaskEntry;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A class for reading from and writing to database.
 */
public class ReadWriteDB {

    /**
     * Member variable for the database reference
     */
    private final DatabaseReference databaseReference;

    /**
     * Constructor to initialize the database reference
     */
    public ReadWriteDB() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Tasks");
    }

    /**
     * Creates new task under 'Tasks' node, and adds it after the last added task.
     *
     * @param task Task object to be added to the list.
     * @param lastID The ID of the last added task.
     */
    public void addTask(TaskEntry task, int lastID) {
        String id = lastID + 1 + "";
        databaseReference.child(id).setValue(task);
    }

    /**
     * Reads the existing list of tasks from the database.
     *
     * @param listener A listener that detects any changes in the tasks list in the database.
     */
    public void readExistingTasks(ValueEventListener listener) {
        databaseReference.addValueEventListener(listener);
    }

    /**
     * Reads a specific task from the database when it is clicked.
     *
     * @param key The generated key of the clicked task.
     * @param listener A listener that detects any changes in the tasks list in the database.
     */
    public void readSelectedTask(String key, ValueEventListener listener) {
        databaseReference.child(key).addListenerForSingleValueEvent(listener);

    }

    /**
     * Updates the information of a specific task when it is clicked.
     *
     * @param key The generated key of the clicked task.
     * @param description The description of the To-do task.
     * @param priority The priority of the To-do task.
     */
    public void updateSelectedTask(String key, String description, int priority) {
        databaseReference.child(key).child("description").setValue(description);
        databaseReference.child(key).child("priority").setValue(priority);
    }

    /**
     * Deletes specific task(s).
     *
     * @param keys A list of keys of the task(s) to be deleted.
     */
    public void deleteSelectedTasks(ArrayList<String> keys) {
        for (int i = 0; i < keys.size(); i++) {
            databaseReference.child(keys.get(i)).removeValue();
        }
    }
}