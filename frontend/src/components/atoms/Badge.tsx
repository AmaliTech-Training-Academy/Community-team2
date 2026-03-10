const BADGE_MAP: Record<string, string> = {
  News: "bg-badge-green text-greentext border border-badge-greenborder",
  Events: "bg-badge-purple text-purpletext border border-badge-purpleborder",
  Discussion: "bg-badge-yellow text-yellowtext border border-badge-yellowborder",
  Alert: "bg-badge-red text-redtext border border-badge-redborder",
};

export function Badge({ category }: { category: string }) {
  const cls = BADGE_MAP[category] || "bg-gray-100 text-gray-600";
  return (
    <span
      data-testid={`badge-${category.toLowerCase().replace(/[^a-z]/g, "-")}`}
      data-category={category}
      className={`inline-flex max-w-full shrink-0 items-center rounded-md px-2.5 py-0.5 text-xs font-semibold ${cls}`}
      title={category}
    >
      <span className="block max-w-[40vw] wrap-break-word whitespace-normal leading-tight sm:max-w-[18rem] md:max-w-none">
        {category}
      </span>
    </span>
  );
}
