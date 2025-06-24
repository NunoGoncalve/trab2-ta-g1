package com.example.catcoins.model;

public enum OrderStatus {
    Active("Open"), Disabled("Filled"), Expired("Expired"), Cancelled("Cancelled");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    // Reverse lookup: from string to enum
    public static OrderStatus fromString(String text) {
        for (OrderStatus s : OrderStatus.values()) {
            if (s.label.equalsIgnoreCase(text)) {
                return s;
            }
        }
        throw new IllegalArgumentException("No enum constant with label " + text);
    }
}
