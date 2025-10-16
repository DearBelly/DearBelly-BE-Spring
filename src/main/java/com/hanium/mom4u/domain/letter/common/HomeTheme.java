package com.hanium.mom4u.domain.letter.common;

public enum HomeTheme {
    SUNSET, MINT, COTTONLIGHT, COTTONDARK, NIGHT, VIOLA;

    public static HomeTheme fromOrDefault(String s) {
        if (s == null || s.isBlank()) return MINT;
        try { return HomeTheme.valueOf(s); } catch (Exception e) { return MINT; }
    }
}

