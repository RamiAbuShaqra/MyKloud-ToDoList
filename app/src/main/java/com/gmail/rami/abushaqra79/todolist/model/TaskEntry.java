package com.gmail.rami.abushaqra79.todolist.model;

/**
 * A model class for the To-do task.
 */
public class TaskEntry {

    /**
     * Member variable for the description of the task
     */
    private final String mDescription;

    /**
     * Member variable for the priority level of the task
     */
    private final int mPriority;

    /**
     * Constructor that initialize the fields.
     *
     * @param description The description of the task.
     * @param priority The priority of the task.
     */
    public TaskEntry(String description, int priority) {
        mDescription = description;
        mPriority = priority;
    }

    /**
     * Getter method for task description.
     *
     * @return Description as a string.
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * Getter method for task priority.
     *
     * @return Priority as an integer.
     */
    public int getPriority() {
        return mPriority;
    }
}