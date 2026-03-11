import { TEST_IDS, sel } from "../utils/TEST_IDS";

describe("TEST_IDS", () => {
  it("builds CSS selectors with sel", () => {
    expect(sel(TEST_IDS.LOGIN.EMAIL_INPUT)).toBe(
      '[data-testid="login-email-input"]',
    );
  });

  it("exposes current navbar ids", () => {
    expect(TEST_IDS.NAVBAR.AVATAR).toBe("navbar-avatar");
    expect(TEST_IDS.NAVBAR.USER_MENU_TRIGGER).toBe("navbar-mobile-menu-btn");
    expect(TEST_IDS.NAVBAR.MOBILE_DRAWER).toBe("navbar-mobile-drawer");
  });

  it("exposes register password toggle ids", () => {
    expect(TEST_IDS.REGISTER.TOGGLE_PASSWORD).toBe(
      "register-toggle-password-btn",
    );
    expect(TEST_IDS.REGISTER.TOGGLE_CONFIRM_PASSWORD).toBe(
      "register-toggle-confirm-password-btn",
    );
  });

  it("exposes post card media ids", () => {
    expect(TEST_IDS.POST_CARD.IMAGE).toBe("post-card-image");
    expect(TEST_IDS.POST_CARD.HAS_IMAGE).toBe("post-card-has-image");
    expect(TEST_IDS.POST_CARD.CARD(42)).toBe("post-card-42");
  });

  it("exposes current dashboard ids and legacy aliases", () => {
    expect(TEST_IDS.DASHBOARD.STAT_TOTAL_COMMENTS).toBe(
      "stat-card-total-comments",
    );
    expect(TEST_IDS.DASHBOARD.STAT_VALUE_TOTAL_COMMENTS).toBe(
      "stat-value-total-comments",
    );
    expect(TEST_IDS.DASHBOARD.CHART_CARD_POSTS_BY_CATEGORY).toBe(
      "chart-card-posts-by-category",
    );
    expect(TEST_IDS.DASHBOARD.CHART_PER_CATEGORY).toBe(
      TEST_IDS.DASHBOARD.CHART_POSTS_BY_CATEGORY,
    );
    expect(TEST_IDS.DASHBOARD.CHART_BY_DAY).toBe(
      TEST_IDS.DASHBOARD.CHART_POSTS_DAY_OF_WEEK,
    );
    expect(TEST_IDS.DASHBOARD.CONTRIBUTORS_LIST).toBe(
      TEST_IDS.DASHBOARD.CONTRIBUTORS_TABLE,
    );
  });

  it("builds normalized dynamic ids", () => {
    expect(TEST_IDS.HOME.CATEGORY_FILTER_BTN("Campus Life")).toBe(
      "category-filter-btn-campus-life",
    );
    expect(TEST_IDS.POST_CARD.BADGE("Q&A")).toBe("badge-q-a");
    expect(TEST_IDS.COMMENTS.ITEM(7)).toBe("comment-item-7");
  });
});
