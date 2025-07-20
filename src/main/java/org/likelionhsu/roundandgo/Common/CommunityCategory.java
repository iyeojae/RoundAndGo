package org.likelionhsu.roundandgo.Common;

public enum CommunityCategory {
    JOIN("조인글"),
    QUESTION("질문글"),
    INFORMATION("정보글"),
    REVIEW("후기글"),
    FREE_TALK("자유글");

    private final String label;

    CommunityCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static CommunityCategory fromLabel(String label) {
        for (CommunityCategory category : CommunityCategory.values()) {
            if (category.label.equals(label)) {
                return category;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 카테고리 라벨입니다: " + label);
    }
}
