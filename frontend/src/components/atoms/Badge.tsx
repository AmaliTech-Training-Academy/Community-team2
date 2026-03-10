const BADGE_MAP: Record<string, string> = {
  Events: "bg-violet-100 text-violet-700",
  "Lost & Found": "bg-red-100 text-red-600",
  Recommendations: "bg-emerald-100 text-emerald-700",
  "Help Requests": "bg-amber-100 text-amber-700",
  News: "bg-blue-100 text-blue-700",
};

export function Badge({ category }: { category: string }) {
  const cls = BADGE_MAP[category] || "bg-gray-100 text-gray-600";
  return (
    <span
      data-testid={`badge-${category.toLowerCase().replace(/[^a-z]/g, "-")}`}
      data-category={category}
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold min-w-0 max-w-full ${cls}`}
    >
      <span className="truncate max-w-35 sm:max-w-45 md:max-w-none">
        {category}
      </span>
    </span>
  );
}
