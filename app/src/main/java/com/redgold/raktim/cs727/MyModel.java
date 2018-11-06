package com.redgold.raktim.cs727;


import java.util.Observable;

public class MyModel extends Observable {
    public String output = "0";
    public void change(String out) {
        setChanged();
        notifyObservers(out);
    }

}
