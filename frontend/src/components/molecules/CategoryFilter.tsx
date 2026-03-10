import { CATEGORIES } from "../../types";

interface CategoryFilterProps {
  active: string;
  onSelect: (c: string) => void;
}

export function CategoryFilter({ active, onSelect }: CategoryFilterProps) {
  return (
    <div
      data-testid="category-filters"
      className="flex items-center gap-2 flex-wrap mb-6"
    >
      <span className="text-body-sm  text-blue-gray-light mr-1">
        Categories:
      </span>
      {["All", ...CATEGORIES].map((c) => (
        <button
          key={c}
          data-testid={`category-filter-btn-${c.toLowerCase().replace(/[^a-z]/g, "-")}`}
          data-active={active === c}
          onClick={() => onSelect(c)}
          className={`px-2.5 py-1 rounded-md text-body-sm font-medium border transition-all duration-150 ${
            active === c
              ? "bg-badge-darkblue text-blue-gray border-blue-gray-dark"
              : "bg-white text-blue-gray-dark border-blue-gray-dark hover:border-gray-400"
          }`}
        >
          {c}
        </button>
      ))}
    </div>
  );
}
