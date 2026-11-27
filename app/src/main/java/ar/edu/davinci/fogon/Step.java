package ar.edu.davinci.fogon;

import java.io.Serializable;

public class Step implements Serializable {
    private String text;
    private int minutes;

    public Step() {
    }

    public Step(String text, int minutes) {
        this.text = text;
        this.minutes = minutes;
    }

    public String getText() {
        return text;
    }

    public int getMinutes() {
        return minutes;
    }

    public boolean hasTimer() {
        return minutes > 0;
    }
}
