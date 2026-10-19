package com.mevdz.ghst;

public enum Category {
    COMBAT("Combat"), VISUAL("Visual"), UTILITY("Utility");

    public final String label;

    Category(String label) { this.label = label; }
}
