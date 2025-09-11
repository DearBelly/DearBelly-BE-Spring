package com.hanium.mom4u.domain.calendar.common;

public enum Color {
    CALENDAR1("#87CEEB"),   // SKY_BLUE
    CALENDAR2("#00FFFF"),   // AQUA_BLUE
    CALENDAR3("#228B22"),   // FOREST
    CALENDAR4("#90EE90"),   // LIGHT_GREEN
    CALENDAR5("#FBCEB1"),   // APRICOT
    CALENDAR6("#00CED1");   // CYAN

    private final String hex;

    Color(String hex) {
        this.hex = hex;
    }

}

