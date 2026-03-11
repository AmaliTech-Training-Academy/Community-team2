// ─────────────────────────────────────────────────────────────────────────────
// TEST_IDS.ts  –  Single source of truth for all data-testid attributes
//
// Usage (Selenium/WebDriver):
//   driver.findElement(By.css(`[data-testid="${TEST_IDS.LOGIN.EMAIL_INPUT}"]`))
//
// Usage (React component):
//   <input data-testid={TEST_IDS.LOGIN.EMAIL_INPUT} />
// ─────────────────────────────────────────────────────────────────────────────

export const TEST_IDS = {
  // ── APP ROOT ────────────────────────────────────────────────────────────────
  APP_ROOT: "app-root",

  // ── TOAST ───────────────────────────────────────────────────────────────────
  TOAST: {
    CONTAINER: "toast-container",
    SUCCESS: "toast-success",
    ERROR: "toast-error",
    WARNING: "toast-warning",
    CLOSE_BTN: "toast-close-btn",
  },

  // ── NAVBAR ──────────────────────────────────────────────────────────────────
  NAVBAR: {
    ROOT: "navbar",
    LOGO: "navbar-logo",
    ANALYTICS_LINK: "navbar-analytics-link",
    USER_MENU_TRIGGER: "navbar-mobile-menu-btn",
    AVATAR: "navbar-avatar",
    USERNAME: "navbar-username",
    EMAIL: "navbar-email",
    LOGOUT_BTN: "navbar-logout-btn",
    MOBILE_MENU_BTN: "navbar-mobile-menu-btn",
    MOBILE_DRAWER: "navbar-mobile-drawer",
    MOBILE_CLOSE_BTN: "navbar-mobile-close-btn",
    MOBILE_ANALYTICS_BTN: "navbar-mobile-analytics-btn",
    MOBILE_LOGOUT_BTN: "navbar-mobile-logout-btn",
  },

  // ── LOGIN PAGE ──────────────────────────────────────────────────────────────
  LOGIN: {
    PAGE: "login-page",
    CARD: "login-card",
    EMAIL_INPUT: "login-email-input",
    PASSWORD_INPUT: "login-password-input",
    TOGGLE_PASSWORD: "login-toggle-password-btn",
    EMAIL_ERROR: "login-email-error",
    PASSWORD_ERROR: "login-password-error",
    SUBMIT_BTN: "login-submit-btn",
    GOTO_REGISTER_LINK: "login-goto-register-link",
  },

  // ── REGISTER PAGE ───────────────────────────────────────────────────────────
  REGISTER: {
    PAGE: "register-page",
    CARD: "register-card",
    FULL_NAME_INPUT: "register-fullName-input",
    EMAIL_INPUT: "register-email-input",
    PASSWORD_INPUT: "register-password-input",
    CONFIRM_PASSWORD_INPUT: "register-confirmPassword-input",
    TOGGLE_PASSWORD: "register-toggle-password-btn",
    FULL_NAME_ERROR: "register-fullName-error",
    EMAIL_ERROR: "register-email-error",
    PASSWORD_ERROR: "register-password-error",
    CONFIRM_PASSWORD_ERROR: "register-confirmPassword-error",
    TOGGLE_CONFIRM_PASSWORD: "register-toggle-confirm-password-btn",
    SUBMIT_BTN: "register-submit-btn",
    GOTO_LOGIN_LINK: "register-goto-login-link",
  },

  // ── HOME PAGE ───────────────────────────────────────────────────────────────
  HOME: {
    PAGE: "home-page",
    CREATE_POST_BTN: "create-post-btn",
    SEARCH_BAR: "search-bar",
    SEARCH_INPUT: "search-input",
    SEARCH_CLEAR: "search-clear-btn",
    SEARCH_SUBMIT: "search-submit-btn",
    CATEGORY_FILTERS: "category-filters",
    POST_LIST: "post-list",
    EMPTY_STATE: "posts-empty-state",
    LOADING: "loading-spinner",

    // Dynamic – call as function: HOME.CATEGORY_FILTER_BTN('events')
    CATEGORY_FILTER_BTN: (cat: string) =>
      `category-filter-btn-${cat.toLowerCase().replace(/[^a-z]/g, "-")}`,
  },

  // ── POST CARD ───────────────────────────────────────────────────────────────
  POST_CARD: {
    CARD: (id: number) => `post-card-${id}`,
    TITLE: "post-card-title",
    EXCERPT: "post-card-excerpt",
    AUTHOR: "post-card-author",
    TIME: "post-card-time",
    IMAGE: "post-card-image",
    HAS_IMAGE: "post-card-has-image",
    COMMENT_COUNT: "post-card-comment-count",
    BADGE: (cat: string) =>
      `badge-${cat.toLowerCase().replace(/[^a-z]/g, "-")}`,
  },

  // ── POST MODAL (Create / Edit) ───────────────────────────────────────────────
  POST_MODAL: {
    OVERLAY: "post-modal-overlay",
    MODAL: "post-modal",
    TITLE: "post-modal-title",
    CLOSE_BTN: "post-modal-close-btn",
    TITLE_INPUT: "post-title-input",
    TITLE_ERROR: "post-title-error",
    CAT_SELECT: "post-category-select",
    CAT_DROPDOWN: "post-category-dropdown",
    CAT_OPTION: (cat: string) =>
      `post-category-option-${cat.toLowerCase().replace(/[^a-z]/g, "-")}`,
    CAT_ERROR: "post-category-error",
    BODY_INPUT: "post-body-input",
    BODY_ERROR: "post-body-error",
    CANCEL_BTN: "post-modal-cancel-btn",
    SUBMIT_BTN: "post-modal-submit-btn",
  },

  // ── POST DETAIL PAGE ────────────────────────────────────────────────────────
  POST_DETAIL: {
    PAGE: "post-detail-page",
    BACK_BTN: "back-to-posts-btn",
    CARD: "post-detail-card",
    TITLE: "post-detail-title",
    BODY: "post-detail-body",
    EDIT_BTN: "post-edit-btn",
    DELETE_BTN: "post-delete-btn",
  },

  // ── DELETE CONFIRM MODAL ────────────────────────────────────────────────────
  DELETE_MODAL: {
    OVERLAY: "delete-modal-overlay",
    MODAL: "delete-modal",
    TITLE: "delete-modal-title",
    CANCEL_BTN: "delete-modal-cancel-btn",
    CONFIRM_BTN: "delete-modal-confirm-btn",
  },

  // ── COMMENTS ────────────────────────────────────────────────────────────────
  COMMENTS: {
    SECTION: "comments-section",
    COUNT_HEADING: "comments-count-heading",
    EMPTY_STATE: "comment-empty-state",
    ITEM: (id: number) => `comment-item-${id}`,
    AUTHOR: "comment-author",
    TEXT: "comment-text",
    EDIT_BTN: "comment-edit-btn",
    DELETE_BTN: "comment-delete-btn",
    EDIT_INPUT: "comment-edit-input",
    EDIT_SAVE_BTN: "comment-edit-save-btn",
    EDIT_CANCEL_BTN: "comment-edit-cancel-btn",
    FORM: "comment-form",
    INPUT: "comment-input",
    SUBMIT_BTN: "comment-submit-btn",
  },

  // ── DASHBOARD PAGE ──────────────────────────────────────────────────────────
  DASHBOARD: {
    PAGE: "dashboard-page",
    TITLE: "dashboard-title",
    STATS_GRID: "dashboard-stats-grid",
    CHARTS_GRID: "dashboard-charts-grid",

    STAT_TOTAL_POSTS: "stat-card-total-posts",
    STAT_VALUE_TOTAL_POSTS: "stat-value-total-posts",
    STAT_TOTAL_USERS: "stat-card-total-users",
    STAT_VALUE_TOTAL_USERS: "stat-value-total-users",
    STAT_TOTAL_COMMENTS: "stat-card-total-comments",
    STAT_VALUE_TOTAL_COMMENTS: "stat-value-total-comments",

    STAT_CAT_CARD: (cat: string) =>
      `stat-card-cat-${cat.toLowerCase().replace(/[^a-z]/g, "-")}`,
    STAT_CAT_VALUE: (cat: string) =>
      `stat-value-cat-${cat.toLowerCase().replace(/[^a-z]/g, "-")}`,

    CHART_CARD_POSTS_BY_CATEGORY: "chart-card-posts-by-category",
    CHART_POSTS_BY_CATEGORY: "chart-posts-by-category",
    CHART_PER_CATEGORY: "chart-posts-by-category",
    CHART_PER_CATEGORY_TITLE: "chart-posts-by-category-title",
    CHART_CARD_POSTS_DAY_OF_WEEK: "chart-card-posts-day-of-week",
    CHART_POSTS_DAY_OF_WEEK: "chart-posts-day-of-week",
    CHART_BY_DAY: "chart-posts-day-of-week",
    CHART_BY_DAY_TITLE: "chart-posts-day-of-week-title",

    CONTRIBUTORS_CARD: "contributors-card",
    CONTRIBUTORS_TABLE: "contributors-table",
    CONTRIBUTORS_LIST: "contributors-table",
    CONTRIBUTOR_ITEM: (rank: number) => `contributor-item-${rank}`,
    CONTRIBUTOR_NAME: "contributor-name",
    CONTRIBUTOR_COUNT: "contributor-count",
    CONTRIBUTOR_RANK: "contributor-rank",
  },

  // ── SHARED ──────────────────────────────────────────────────────────────────
  SHARED: {
    LOADING_SPINNER: "loading-spinner",
  },
} as const;

// ── CSS SELECTOR HELPERS (for Selenium/REST Assured) ───────────────────────────
// Usage: SELECTORS.LOGIN.EMAIL_INPUT → '[data-testid="login-email-input"]'
export function sel(testId: string): string {
  return `[data-testid="${testId}"]`;
}
