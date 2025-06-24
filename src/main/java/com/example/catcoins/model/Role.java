package com.example.catcoins.model;

public enum Role {
    Admin("Admin"), Client("Client");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    // Reverse lookup: from string to enum
    public static Role fromString(String text) {
        for (Role r : Role.values()) {
            if (r.label.equalsIgnoreCase(text)) {
                return r;
            }
        }
        throw new IllegalArgumentException("No enum constant with label " + text);
    }
}
