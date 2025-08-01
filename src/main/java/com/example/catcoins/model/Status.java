package com.example.catcoins.model;

public enum Status {
    Active("Active"), Disabled("Disabled");

    private final String label;

    Status(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    // Reverse lookup: from string to enum
    public static Status fromString(String text) {
        for (Status s : Status.values()) {
            if (s.label.equalsIgnoreCase(text)) {
                return s;
            }
        }
        throw new IllegalArgumentException("No enum constant with label " + text);
    }
}
