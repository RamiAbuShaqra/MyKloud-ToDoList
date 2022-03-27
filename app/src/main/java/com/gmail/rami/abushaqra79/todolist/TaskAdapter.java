package com.gmail.rami.abushaqra79.todolist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.rami.abushaqra79.todolist.model.TaskEntry;

import java.util.List;

/**
 * This TaskAdapter creates and binds ViewHolders, that hold the description and priority of a task,
 * to a RecyclerView to efficiently display data.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    /**
     * Member variable to handle item clicks
     */
    private final ItemClickListener mItemClickListener;

    /**
     * Member variable to handle CheckBox checking for deletion
     */
    private TickForDeleteListener mDeleteListener;

    /**
     * Member variable for the List that holds tasks data
     */
    private List<TaskEntry> mTaskEntries;

    /**
     * Member variable for the context
     */
    private final Context mContext;

    /**
     * Constructor for the TaskAdapter that initializes the Context.
     *
     * @param context  The current Context
     * @param listener The ItemClickListener
     */
    public TaskAdapter(Context context, ItemClickListener listener) {
        mContext = context;
        mItemClickListener = listener;
    }

    /**
     * Called when ViewHolders are created to fill a RecyclerView.
     *
     * @return A new TaskViewHolder that holds the view for each task.
     */
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        // Get a reference to the sharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String language = sharedPreferences.getString(mContext.getString(R.string.settings_select_language_key), mContext.getString(R.string.settings_select_language_default));

        // Inflate the task_layout to a view
        if (language.equals(mContext.getString(R.string.settings_english_value))) {
            view = LayoutInflater.from(mContext).inflate(R.layout.task_layout, parent, false);
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.task_layout_arabic, parent, false);
        }

        return new TaskViewHolder(view);
    }

    /**
     * Called by the RecyclerView to display data at a specified position in the list.
     *
     * @param holder   The ViewHolder to bind data to.
     * @param position The position of the data in the list.
     */
    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        // Determine the values of the wanted data
        TaskEntry taskEntry = mTaskEntries.get(position);
        String description = taskEntry.getDescription();
        int priority = taskEntry.getPriority();

        //Set values
        holder.taskDescriptionView.setText(description);

        // Get the appropriate color based on the priority
        int priorityColor = getPriorityColor(priority);
        holder.priorityView.setButtonTintList(ColorStateList.valueOf(priorityColor));
        // Attach a click listener to the priority CheckBox
        holder.priorityView.setOnClickListener(view -> {
            if (mDeleteListener != null) {
                mDeleteListener.tickForDelete(holder.priorityView, holder.getBindingAdapterPosition());
            }

            if (holder.priorityView.isChecked()) {
                holder.taskDescriptionView.setPaintFlags(holder.taskDescriptionView.getPaintFlags()
                        | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.taskDescriptionView.setTextColor(ContextCompat.getColor(mContext, R.color.light_gray));
            } else {
                holder.taskDescriptionView.setPaintFlags(0);
                holder.taskDescriptionView.setTextColor(ContextCompat.getColor(mContext, R.color.black));
            }
        });
    }

    /**
     * Helper method for selecting the correct priority checkbox color.
     *     P1 = red, P2 = blue, P3 = green
     */
    private int getPriorityColor(int priority) {
        int priorityColor = 0;

        switch (priority) {
            case 1:
                priorityColor = ContextCompat.getColor(mContext, R.color.materialRed);
                break;
            case 2:
                priorityColor = ContextCompat.getColor(mContext, R.color.materialBlue);
                break;
            case 3:
                priorityColor = ContextCompat.getColor(mContext, R.color.materialGreen);
                break;
            default:
                break;
        }
        return priorityColor;
    }

    /**
     * Returns the number of items to display.
     */
    @Override
    public int getItemCount() {
        if (mTaskEntries == null) {
            return 0;
        }
        return mTaskEntries.size();
    }

    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setTasks(List<TaskEntry> taskEntries) {
        mTaskEntries = taskEntries;
        notifyDataSetChanged();
    }

    /**
     * Interface to handle item clicks
     */
    public interface ItemClickListener {
        void onItemClickListener(int clickedItemIndex);
    }

    /**
     * Interface to handle CheckBox checking for deletion
     */
    public interface TickForDeleteListener{
        void tickForDelete(CheckBox checkBox, int position);
    }

    // Initialize the deletion listener
    public void setTickForDeleteListener(TickForDeleteListener deleteListener) {
        mDeleteListener = deleteListener;
    }

    /**
     * Inner class for creating ViewHolders.
     */
    class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        /**
         * Class variables for the task description
         */
        TextView taskDescriptionView;

        /**
         * Class variables for the task priority
         */
        CheckBox priorityView;

        /**
         * Constructor for the TaskViewHolders.
         *
         * @param itemView The view inflated in onCreateViewHolder.
         */
        public TaskViewHolder(View itemView) {
            super(itemView);

            // Initialize the views
            taskDescriptionView = itemView.findViewById(R.id.taskDescription);
            priorityView = itemView.findViewById(R.id.priorityCheckBox);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getLayoutPosition();
            mItemClickListener.onItemClickListener(clickedPosition);
        }
    }
}