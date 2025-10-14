package com.github.maxpesh;

public enum Language {
    RU(),
    EN();

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
