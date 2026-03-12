import { memo } from "react";
import type { Category } from "../../types";

interface CategoryFilterProps {
  active: string;
  categories: Category[];
  onSelect: (c: string) => void;
}

const BTN_BASE =
  "px-2.5 py-1 rounded-md text-body-sm font-medium border transition-all duration-150";
const BTN_ACTIVE = `${BTN_BASE} bg-badge-darkblue text-blue-gray border-blue-gray-dark`;
const BTN_INACTIVE = `${BTN_BASE} bg-white text-blue-gray-dark border-blue-gray-dark hover:border-gray-400`;

function FilterButton({
  label,
  testId,
  isActive,
  onSelect,
}: {
  label: string;
  testId: string;
  isActive: boolean;
  onSelect: () => void;
}) {
  if (isActive) {
    return (
      <button
        key={label}
        data-testid={testId}
        data-active="true"
        aria-pressed="true"
        onClick={onSelect}
        className={BTN_ACTIVE}
      >
        {label}
      </button>
    );
  }
  return (
    <button
      key={label}
      data-testid={testId}
      data-active="false"
      aria-pressed="false"
      onClick={onSelect}
      className={BTN_INACTIVE}
    >
      {label}
    </button>
  );
}

export const CategoryFilter = memo(function CategoryFilter({
  active,
  categories,
  onSelect,
}: CategoryFilterProps) {
  return (
    <div
      data-testid="category-filters"
      className="flex items-center gap-2 flex-wrap mb-6"
    >
      <span className="text-body-sm  text-blue-gray-light mr-1">
        Categories:
      </span>
      {["All", ...categories].map((c) => (
        <FilterButton
          key={c}
          label={c}
          testId={`category-filter-btn-${c.toLowerCase().replace(/[^a-z]/g, "-")}`}
          isActive={active === c}
          onSelect={() => onSelect(c)}
        />
      ))}
    </div>
  );
});
