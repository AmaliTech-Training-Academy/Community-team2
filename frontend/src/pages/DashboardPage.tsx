import { useEffect } from "react";
import { Link } from "react-router-dom";
import { useAnalyticsStore } from "../features/analytics/analyticsStore";
import { Spinner } from "../components/atoms/Spinner";
import HomeIcon from "../assets/images/home.svg?react";
import ChevronRightIcon from "../assets/images/chevron-right.svg?react";
import PostsStatIcon from "../assets/images/posts-stat.svg?react";
import CommentsStatIcon from "../assets/images/comments-stat.svg?react";

const NAVY = "#395362";
const CATEGORY_ORDER = ["Events", "Discussion", "News", "Alert"] as const;
const DAY_ORDER = ["Mon", "Tues", "Wed", "Thurs", "Fri", "Sat", "Sun"] as const;
const AXIS_LABEL_WIDTH = 16;
const CHART_AXIS_GAP = 16;
const CHART_SECTION_WIDTH = 512;
const CATEGORY_GRAPH_HEIGHT = 262.74;
const CATEGORY_BAR_AREA_HEIGHT = 218.74;
const CATEGORY_PLOT_HEIGHT = 178;
const CATEGORY_LABELS_HEIGHT = 36;
const CATEGORY_BAR_WIDTH = 74;
const CATEGORY_BAR_GAP = 32;
const DAY_GRAPH_HEIGHT = 248.74;
const DAY_BAR_AREA_HEIGHT = 218.74;
const DAY_PLOT_HEIGHT = 178;
const DAY_LABELS_HEIGHT = 22;
const DAY_BAR_WIDTH = 50;
const DAY_GRID_WIDTH = 496;
const DAY_BAR_GAP =
  (DAY_GRID_WIDTH - DAY_ORDER.length * DAY_BAR_WIDTH) / (DAY_ORDER.length - 1);
const DAY_LABEL_SLOT_WIDTH = DAY_GRID_WIDTH / DAY_ORDER.length;
const SVG_CHART_WIDTH = AXIS_LABEL_WIDTH + CHART_AXIS_GAP + CHART_SECTION_WIDTH;

function getRoundedYAxisMax(values: number[]) {
  const maxValue = Math.max(0, ...values);
  const rounded = Math.ceil(maxValue / 10) * 10;
  return Math.max(10, rounded);
}

function getAxisTicks(axisMax: number) {
  return Array.from(
    { length: Math.floor(axisMax / 10) + 1 },
    (_, index) => axisMax - index * 10,
  );
}

function getPlotLineY(index: number, tickCount: number, plotHeight: number) {
  if (tickCount <= 1) return 0;
  return (index * plotHeight) / (tickCount - 1);
}

function getBarHeight(value: number, axisMax: number, plotHeight: number) {
  if (axisMax <= 0) return 0;
  return (value / axisMax) * plotHeight;
}

export default function DashboardPage() {
  const data = useAnalyticsStore((s) => s.data);
  const loading = useAnalyticsStore((s) => s.loading);
  const fetch = useAnalyticsStore((s) => s.fetch);

  useEffect(() => {
    fetch();
  }, [fetch]);

  const categoryValues = data
    ? CATEGORY_ORDER.map((category) => data.categoryBreakdown[category] ?? 0)
    : [];
  const dayValues = data
    ? [
        data.dayActivity.find((d) => d.day === "Mon")?.count ?? 0,
        data.dayActivity.find((d) => d.day === "Tue")?.count ?? 0,
        data.dayActivity.find((d) => d.day === "Wed")?.count ?? 0,
        data.dayActivity.find((d) => d.day === "Thu")?.count ?? 0,
        data.dayActivity.find((d) => d.day === "Fri")?.count ?? 0,
        data.dayActivity.find((d) => d.day === "Sat")?.count ?? 0,
        data.dayActivity.find((d) => d.day === "Sun")?.count ?? 0,
      ]
    : [];
  const categoryAxisMax = getRoundedYAxisMax(categoryValues);
  const categoryTicks = getAxisTicks(categoryAxisMax);
  const categoryAverageValue =
    categoryValues.length > 0
      ? categoryValues.reduce((sum, value) => sum + value, 0) / categoryValues.length
      : 0;
  const categoryAverageY =
    categoryAxisMax > 0
      ? CATEGORY_PLOT_HEIGHT - (categoryAverageValue / categoryAxisMax) * CATEGORY_PLOT_HEIGHT
      : CATEGORY_PLOT_HEIGHT;
  const dayAxisMax = getRoundedYAxisMax(dayValues);
  const dayTicks = getAxisTicks(dayAxisMax);
  const dayAverageValue =
    dayValues.length > 0
      ? dayValues.reduce((sum, value) => sum + value, 0) / dayValues.length
      : 0;
  const dayAverageY =
    dayAxisMax > 0
      ? DAY_PLOT_HEIGHT - (dayAverageValue / dayAxisMax) * DAY_PLOT_HEIGHT
      : DAY_PLOT_HEIGHT;

  if (loading) return <Spinner />;
  if (!data) return null;

  const totalComments = data.totalComments ?? 0;

  return (
    <div
      data-testid="dashboard-page"
      className="mx-auto w-full max-w-300.5 fade-in text-blue-gray-light"
    >
      <div className="mb-16">
        <div className="inline-flex h-11.25 items-center gap-4 rounded-lg border border-borderstroke bg-background px-5 py-3">
          <Link
            data-testid="dashboard-breadcrumb-home-link"
            to="/"
            className="flex items-center gap-2 text-sm font-medium text-blue-gray-light transition-opacity hover:opacity-70"
          >
            <HomeIcon width={18} height={18} />
            <span>Home</span>
          </Link>
          <span
            className="text-sm font-medium text-blue-gray-light"
            aria-hidden="true"
          >
            <ChevronRightIcon width={16} height={16} />
          </span>
          <span className="text-sm font-medium text-blue-gray-light">
            Analytics
          </span>
        </div>
      </div>

      <div className="mb-8 flex flex-col gap-8 md:flex-row md:flex-wrap">
        <div
          data-testid="stat-card-total-posts"
          className="h-42 w-full rounded-[14px] border border-borderstroke bg-background p-6 shadow-none md:w-[389.33px]"
        >
          <div className="flex items-start justify-between gap-4">
            <p className="text-2xl font-medium leading-[150%] text-blue-gray">
              Total Posts
            </p>
            <div className="flex h-10 w-10 items-center justify-center rounded-[10px] bg-[#CDE6FC] text-blue-gray-light">
              <PostsStatIcon aria-hidden="true" />
            </div>
          </div>
          <p
            data-testid="stat-value-total-posts"
            className="mt-2 text-[48px] font-bold leading-[150%] text-blue-gray-light"
          >
            {data.totalPosts}
          </p>
        </div>

        <div
          data-testid="stat-card-total-comments"
          className="h-42 w-full rounded-[14px] border border-borderstroke bg-background p-6 shadow-none md:w-[389.33px]"
        >
          <div className="flex items-start justify-between gap-4">
            <p className="text-2xl font-medium leading-[150%] text-blue-gray">
              Total Comments
            </p>
            <div className="flex h-10 w-10 items-center justify-center rounded-[10px] bg-[#CDE6FC] text-blue-gray-light">
              <CommentsStatIcon aria-hidden="true" />
            </div>
          </div>
          <p
            data-testid="stat-value-total-comments"
            className="mt-2 text-[48px] font-bold leading-[150%] text-blue-gray-light"
          >
            {totalComments}
          </p>
        </div>
      </div>

      <div className="mb-8 grid grid-cols-1 gap-8 md:grid-cols-[576px_576px] md:justify-between">
        <div
          data-testid="chart-card-posts-by-category"
          className="mx-auto h-[362.74px] w-full max-w-xl rounded-lg border border-borderstroke bg-background p-4"
        >
          <h3
            data-testid="chart-posts-by-category-title"
            className="mb-4 flex h-13 items-center px-0 py-2 text-2xl font-semibold leading-[150%] text-blue-gray-light"
          >
            Posts by Category
          </h3>
          <div data-testid="chart-posts-by-category" className="h-[262.74px] w-full max-w-136 pl-0 pr-0">
            <svg
              viewBox={`0 0 ${SVG_CHART_WIDTH} ${CATEGORY_GRAPH_HEIGHT}`}
              className="h-full w-full"
              aria-label="Posts by Category chart"
              role="img"
            >
              {categoryTicks.map((tick, index) => {
                const y =
                  CATEGORY_BAR_AREA_HEIGHT - CATEGORY_PLOT_HEIGHT +
                  getPlotLineY(index, categoryTicks.length, CATEGORY_PLOT_HEIGHT);

                return (
                  <text
                    key={`category-tick-${tick}`}
                    x={AXIS_LABEL_WIDTH}
                    y={y}
                    fill="#5A6F7C"
                    fontFamily="Inter"
                    fontSize="12"
                    fontWeight="500"
                    textAnchor="end"
                    dominantBaseline="middle"
                  >
                    {tick}
                  </text>
                );
              })}

              <g transform={`translate(${AXIS_LABEL_WIDTH + CHART_AXIS_GAP} 0)`}>
                {categoryTicks.map((tick, index) => {
                  const y =
                    CATEGORY_BAR_AREA_HEIGHT - CATEGORY_PLOT_HEIGHT +
                    getPlotLineY(index, categoryTicks.length, CATEGORY_PLOT_HEIGHT);
                  const isBaseline = index === categoryTicks.length - 1;

                  return (
                    <line
                      key={`category-grid-${tick}`}
                      x1="0"
                      x2={CHART_SECTION_WIDTH}
                      y1={y}
                      y2={y}
                      stroke="#D3D1CE"
                      strokeWidth="1.5"
                      strokeDasharray={isBaseline ? undefined : "4 7"}
                    />
                  );
                })}

                <line
                  x1="0"
                  x2={CHART_SECTION_WIDTH}
                  y1={CATEGORY_BAR_AREA_HEIGHT - CATEGORY_PLOT_HEIGHT + categoryAverageY}
                  y2={CATEGORY_BAR_AREA_HEIGHT - CATEGORY_PLOT_HEIGHT + categoryAverageY}
                  stroke="#393CC9"
                  strokeWidth="1.5"
                  strokeDasharray="4 7"
                />

                {categoryValues.map((value, index) => {
                  const height = getBarHeight(value, categoryAxisMax, CATEGORY_PLOT_HEIGHT);
                  const x = index * (CATEGORY_BAR_WIDTH + CATEGORY_BAR_GAP);
                  const y = CATEGORY_BAR_AREA_HEIGHT - height;

                  return (
                    <rect
                      key={`category-bar-${CATEGORY_ORDER[index]}`}
                      x={x}
                      y={y}
                      width={CATEGORY_BAR_WIDTH}
                      height={height}
                      fill={NAVY}
                    />
                  );
                })}

                {CATEGORY_ORDER.map((category, index) => {
                  const x =
                    index * (CATEGORY_BAR_WIDTH + CATEGORY_BAR_GAP) + CATEGORY_BAR_WIDTH / 2;

                  return (
                    <text
                      key={`category-label-${category}`}
                      x={x}
                      y={CATEGORY_BAR_AREA_HEIGHT + CATEGORY_LABELS_HEIGHT / 2}
                      fill="#5A6F7C"
                      fontFamily="Inter"
                      fontSize="12"
                      fontWeight="500"
                      textAnchor="middle"
                      dominantBaseline="middle"
                    >
                      {category}
                    </text>
                  );
                })}
              </g>
            </svg>
          </div>
        </div>

        <div
          data-testid="chart-card-posts-day-of-week"
          className="mx-auto h-[362.74px] w-full max-w-xl rounded-lg border border-borderstroke bg-background p-4"
        >
          <h3
            data-testid="chart-posts-day-of-week-title"
            className="mb-4 flex h-13 items-center px-0 py-2 text-2xl font-semibold leading-[150%] text-blue-gray-light"
          >
            Posts Day of Week
          </h3>
          <div data-testid="chart-posts-day-of-week" className="h-[248.74px] w-full max-w-136 pl-0 pr-0">
            <svg
              viewBox={`0 0 ${SVG_CHART_WIDTH} ${DAY_GRAPH_HEIGHT}`}
              className="h-full w-full"
              aria-label="Posts Day of Week chart"
              role="img"
            >
              {dayTicks.map((tick, index) => {
                const y =
                  DAY_BAR_AREA_HEIGHT - DAY_PLOT_HEIGHT +
                  getPlotLineY(index, dayTicks.length, DAY_PLOT_HEIGHT);

                return (
                  <text
                    key={`day-tick-${tick}`}
                    x={AXIS_LABEL_WIDTH}
                    y={y}
                    fill="#5A6F7C"
                    fontFamily="Inter"
                    fontSize="12"
                    fontWeight="500"
                    textAnchor="end"
                    dominantBaseline="middle"
                  >
                    {tick}
                  </text>
                );
              })}

              <g transform={`translate(${AXIS_LABEL_WIDTH + CHART_AXIS_GAP} 0)`}>
                {dayTicks.map((tick, index) => {
                  const y =
                    DAY_BAR_AREA_HEIGHT - DAY_PLOT_HEIGHT +
                    getPlotLineY(index, dayTicks.length, DAY_PLOT_HEIGHT);
                  const isBaseline = index === dayTicks.length - 1;

                  return (
                    <line
                      key={`day-grid-${tick}`}
                      x1="0"
                      x2={DAY_GRID_WIDTH}
                      y1={y}
                      y2={y}
                      stroke="#D3D1CE"
                      strokeWidth="1.5"
                      strokeDasharray={isBaseline ? undefined : "4 7"}
                    />
                  );
                })}

                <line
                  x1="0"
                  x2={DAY_GRID_WIDTH}
                  y1={DAY_BAR_AREA_HEIGHT - DAY_PLOT_HEIGHT + dayAverageY}
                  y2={DAY_BAR_AREA_HEIGHT - DAY_PLOT_HEIGHT + dayAverageY}
                  stroke="#393CC9"
                  strokeWidth="1.5"
                  strokeDasharray="4 7"
                />

                {dayValues.map((value, index) => {
                  const height = getBarHeight(value, dayAxisMax, DAY_PLOT_HEIGHT);
                  const x = index * (DAY_BAR_WIDTH + DAY_BAR_GAP);
                  const y = DAY_BAR_AREA_HEIGHT - height;

                  return (
                    <rect
                      key={`day-bar-${DAY_ORDER[index]}`}
                      x={x}
                      y={y}
                      width={DAY_BAR_WIDTH}
                      height={height}
                      fill={NAVY}
                    />
                  );
                })}

                {DAY_ORDER.map((day, index) => {
                  const x = index * DAY_LABEL_SLOT_WIDTH + DAY_LABEL_SLOT_WIDTH / 2;

                  return (
                    <text
                      key={`day-label-${day}`}
                      x={x}
                      y={DAY_BAR_AREA_HEIGHT + DAY_LABELS_HEIGHT / 2}
                      fill="#5A6F7C"
                      fontFamily="Inter"
                      fontSize="12"
                      fontWeight="500"
                      textAnchor="middle"
                      dominantBaseline="middle"
                    >
                      {day}
                    </text>
                  );
                })}
              </g>
            </svg>
          </div>
        </div>
      </div>

      <div className="mb-4">
        <h3 className="text-[20px] font-bold leading-[150%] text-blue-gray-light">
          Top 10 Contributors
        </h3>
      </div>

      <div
        data-testid="contributors-card"
        className="overflow-hidden rounded-lg bg-background shadow-[0px_1px_3px_rgba(0,0,0,0.1),0px_1px_2px_-1px_rgba(0,0,0,0.1)]"
      >
        <div className="px-0 pb-0 pt-0">
          <div className="w-full overflow-x-auto">
            <table
              data-testid="contributors-table"
              className="min-w-full table-fixed text-base"
            >
              <colgroup>
                <col className="w-20.25" />
                <col className="w-[560.5px]" />
                <col className="w-[560.5px]" />
              </colgroup>
              <thead>
                <tr className="h-14 bg-borderstroke text-blue-gray">
                  <th className="h-14 border-b border-borderstroke px-4 py-4 text-left font-medium">
                    Ranks
                  </th>
                  <th className="h-14 border-b border-borderstroke px-4 py-4 text-left font-medium">
                    Name
                  </th>
                  <th className="h-14 border-b border-borderstroke px-4 py-4 text-left font-medium">
                    Posts
                  </th>
                </tr>
              </thead>
              <tbody>
                {data.topContributors.slice(0, 10).map((c, i) => (
                  <tr
                    key={`${c.name}-${i}`}
                    className="h-14 bg-background last:border-b-0"
                  >
                    <td className="h-14 border-b border-borderstroke px-4 py-4 text-left text-blue-gray-light">
                      {i + 1}
                    </td>
                    <td className="h-14 border-b border-borderstroke px-4 py-4 text-left text-blue-gray-light">
                      {c.name}
                    </td>
                    <td className="h-14 border-b border-borderstroke px-4 py-4 text-left text-blue-gray-light">
                      {c.count}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
