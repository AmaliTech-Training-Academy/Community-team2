import React, { useEffect, useRef } from "react";
import {
  Chart,
  BarController,
  BarElement,
  CategoryScale,
  LinearScale,
  Tooltip,
  Legend,
  Filler,
  type ChartConfiguration,
} from "chart.js";
import { useAnalyticsStore } from "../features/analytics/analyticsStore";
import { Spinner } from "../components/atoms/Spinner";
import HomeIcon from "../assets/images/home.svg?react";
import PostsStatIcon from "../assets/images/posts-stat.svg?react";
import CommentsStatIcon from "../assets/images/comments-stat.svg?react";

Chart.register(
  BarController,
  BarElement,
  CategoryScale,
  LinearScale,
  Tooltip,
  Legend,
  Filler,
);

Chart.defaults.font.family = '"Inter", sans-serif';
Chart.defaults.color = "#6B7280";

const NAVY = "#1B2B3A";
const CAT_COLORS = [
  NAVY,
  "#F97316",
  "#22C55E",
  "#EAB308",
  "#3B82F6",
  "#8B5CF6",
  "#EC4899",
];

function useChart<T extends ChartConfiguration>(
  ref: React.RefObject<HTMLCanvasElement | null>,
  config: T | null,
) {
  const chartRef = useRef<Chart | null>(null);

  useEffect(() => {
    if (!ref.current || !config) return;

    chartRef.current?.destroy();
    chartRef.current = new Chart(ref.current, config);

    return () => {
      chartRef.current?.destroy();
      chartRef.current = null;
    };
  }, [config]);
}

export default function DashboardPage() {
  const data = useAnalyticsStore((s) => s.data);
  const loading = useAnalyticsStore((s) => s.loading);
  const fetch = useAnalyticsStore((s) => s.fetch);

  const barCanvasRef = useRef<HTMLCanvasElement>(null);
  const dayCanvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    fetch();
  }, [fetch]);

  const barConfig: ChartConfiguration<"bar"> | null = data
    ? {
        type: "bar",
        data: {
          labels: Object.keys(data.categoryBreakdown),
          datasets: [
            {
              label: "Posts",
              data: Object.values(data.categoryBreakdown),
              backgroundColor: CAT_COLORS,
              borderRadius: 6,
              borderSkipped: false,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false },
            tooltip: {
              callbacks: {
                label: (ctx) => `Count: ${ctx.parsed.y}`,
              },
            },
          },
          scales: {
            y: {
              beginAtZero: true,
              ticks: { stepSize: 1 },
              grid: { color: "#F3F4F6" },
            },
            x: { grid: { display: false } },
          },
        },
      }
    : null;

  const dayConfig: ChartConfiguration<"bar"> | null = data
    ? {
        type: "bar",
        data: {
          labels: data.dayActivity.map((d) => d.day),
          datasets: [
            {
              label: "Posts",
              data: data.dayActivity.map((d) => d.count),
              backgroundColor: NAVY,
              borderRadius: 6,
              borderSkipped: false,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false },
            tooltip: {
              callbacks: {
                label: (ctx) => `Count: ${ctx.parsed.y}`,
              },
            },
          },
          scales: {
            y: {
              beginAtZero: true,
              ticks: { stepSize: 1 },
              grid: { color: "#F3F4F6" },
            },
            x: { grid: { display: false } },
          },
        },
      }
    : null;

  useChart(barCanvasRef, barConfig);
  useChart(dayCanvasRef, dayConfig);

  if (loading) return <Spinner />;
  if (!data) return null;

  const totalComments = data.totalComments ?? 0;

  return (
    <div data-testid="dashboard-page" className="fade-in">
      <div className="mb-6">
        <div className="inline-flex items-center gap-3 bg-white border border-borderstroke rounded-lg px-4 py-2.5">
          <span className="flex items-center gap-2 text-body-sm font-semibold text-blue-gray-dark">
            <HomeIcon width={18} height={18} />
            <span>Home</span>
          </span>
          <span className="text-body-sm font-semibold text-blue-gray-dark">
            &gt;
          </span>
          <span className="text-body-sm font-semibold text-blue-gray-dark">
            Analytics
          </span>
        </div>
      </div>

      <div className="flex flex-wrap gap-6 mb-8">
        <div
          data-testid="stat-card-total-posts"
          className="bg-white border border-borderstroke rounded-lg p-6 w-full sm:w-72"
        >
          <div className="flex items-start justify-between gap-4">
            <p className="text-body-lg font-medium text-blue-gray">
              Total Posts
            </p>
            <div className="w-8 h-8 rounded-lg bg-blue-100 text-blue-gray-dark flex items-center justify-center">
              <PostsStatIcon aria-hidden="true" />
            </div>
          </div>
          <p
            data-testid="stat-value-total-posts"
            className="text-h-md text-blue-gray-dark mt-3"
          >
            {data.totalPosts}
          </p>
        </div>

        <div
          data-testid="stat-card-total-comments"
          className="bg-white border border-borderstroke rounded-lg p-6 w-full sm:w-72"
        >
          <div className="flex items-start justify-between gap-4">
            <p className="text-body-lg font-medium text-blue-gray">
              Total Comments
            </p>
            <div className="w-8 h-8 rounded-lg bg-blue-100 text-blue-gray-dark flex items-center justify-center">
              <CommentsStatIcon aria-hidden="true" />
            </div>
          </div>
          <p
            data-testid="stat-value-total-comments"
            className="text-h-md text-blue-gray-dark mt-3"
          >
            {totalComments}
          </p>
        </div>
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
        <div
          data-testid="chart-card-posts-by-category"
          className="bg-white border border-borderstroke rounded-lg p-6"
        >
          <h3
            data-testid="chart-posts-by-category-title"
            className="text-body-lg font-semibold text-blue-gray-dark mb-4"
          >
            Posts by Category
          </h3>
          <div className="relative h-56">
            <canvas data-testid="chart-posts-by-category" ref={barCanvasRef} />
          </div>
        </div>

        <div
          data-testid="chart-card-posts-day-of-week"
          className="bg-white border border-borderstroke rounded-lg p-6"
        >
          <h3
            data-testid="chart-posts-day-of-week-title"
            className="text-body-lg font-semibold text-blue-gray-dark mb-4"
          >
            Posts Day of Week
          </h3>
          <div className="relative h-56">
            <canvas data-testid="chart-posts-day-of-week" ref={dayCanvasRef} />
          </div>
        </div>
      </div>

      <div
        data-testid="contributors-card"
        className="bg-white border border-borderstroke rounded-lg"
      >
        <h3 className="text-body-lg font-semibold text-blue-gray-dark px-6 pt-5 pb-3">
          Top 10 Contributors
        </h3>
        <div className="border-t border-borderstroke" />
        <div className="px-6 pb-6 pt-4">
          <div className="w-full overflow-x-auto">
            <table
              data-testid="contributors-table"
              className="min-w-full text-body-sm"
            >
              <thead>
                <tr className="bg-gray-100 text-blue-gray-dark">
                  <th className="text-left font-semibold px-4 py-3 w-20">
                    Ranks
                  </th>
                  <th className="text-left font-semibold px-4 py-3">Name</th>
                  <th className="text-left font-semibold px-4 py-3 w-28">
                    Posts
                  </th>
                </tr>
              </thead>
              <tbody>
                {data.topContributors.slice(0, 10).map((c, i) => (
                  <tr
                    key={`${c.name}-${i}`}
                    className="border-b border-gray-100 last:border-0"
                  >
                    <td className="px-4 py-3 text-blue-gray-dark">{i + 1}</td>
                    <td className="px-4 py-3 text-blue-gray-dark">{c.name}</td>
                    <td className="px-4 py-3 text-blue-gray-dark">{c.count}</td>
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
