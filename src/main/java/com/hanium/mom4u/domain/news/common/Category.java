package com.hanium.mom4u.domain.news.common;


public enum Category {
    HEALTH(1, "건강"),
    FINANCIAL(2, "지원금"),
    PREGNANCY_PLANNING(3, "임신준비"),
    CHILD(4, "출산/육아"),
    EDUCATION(5, "교육프로그램"),
    EMOTIONAL(6, "정서");

    private final int order;
    private final String displayName;

    private Category(int order, String displayName) {
        this.order = order;
        this.displayName = displayName;
    }

    public static Category getCategory(int order) {
        for (Category category : Category.values()) {
            if (category.order == order) {
                return category;
            }
        }
        return null;
    }

    public int getOrder() {
        return order;
    }
    public String getDisplayName() {
        return displayName;
    }
}

