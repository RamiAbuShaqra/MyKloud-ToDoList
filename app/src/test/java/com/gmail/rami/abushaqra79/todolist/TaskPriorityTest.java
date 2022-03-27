package com.gmail.rami.abushaqra79.todolist;

import org.junit.Test;

import static org.junit.Assert.*;

public class TaskPriorityTest {
    @Test
    public void priorityLevel_nothingSelected_isCorrect() {
        assertEquals(0, MainActivity.getPriority(false, false, false));
    }

    @Test
    public void priorityLevel_highSelected_isCorrect() {
        assertEquals(1, MainActivity.getPriority(true, false, false));
    }

    @Test
    public void priorityLevel_mediumSelected_isCorrect() {
        assertEquals(2, MainActivity.getPriority(false, true, false));
    }

    @Test
    public void priorityLevel_lowSelected_isCorrect() {
        assertEquals(3, MainActivity.getPriority(false, false, true));
    }
}