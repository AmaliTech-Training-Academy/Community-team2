import { memo } from "react";
import SearchIcon from "../../assets/images/search.svg?react";
import ClearIcon from "../../assets/images/clear.svg?react";

interface SearchBarProps {
  value: string;
  onChange: (v: string) => void;
}

export const SearchBar = memo(function SearchBar({
  value,
  onChange,
}: SearchBarProps) {
  return (
    <div
      data-testid="search-bar"
      role="search"
      className="flex w-full items-center gap-2 md:h-10.5 md:w-158.75"
    >
      {/* Input with SVG icon + clear */}
      <div className="relative flex-1 min-w-0">
        <span className="pointer-events-none absolute left-3 top-1/2 flex h-4 w-4 -translate-y-1/2 items-center justify-center text-muted-icon">
          <SearchIcon aria-hidden="true" className="h-4 w-4" />
        </span>
        <input
          data-testid="search-input"
          type="search"
          aria-label="Search posts"
          className="h-10 w-full rounded-lg border border-badge-darkblue bg-url-field-bg pl-9 pr-10 text-body-lg text-blue-gray placeholder:text-muted-icon transition-colors focus:border-navy focus:outline-none md:h-10.5"
          placeholder="Search by title of post..."
          value={value}
          onChange={(e) => onChange(e.target.value)}
        />
        {value && (
          <button
            data-testid="search-clear-btn"
            onClick={() => onChange("")}
            className="absolute right-3 top-1/2 flex h-4 w-4 -translate-y-1/2 items-center justify-center text-muted-icon transition-colors hover:text-blue-gray"
            aria-label="Clear search"
            type="button"
          >
            <ClearIcon aria-hidden="true" className="h-4 w-4" />
          </button>
        )}
      </div>

      {/* Navy search submit button */}
      <button
        data-testid="search-submit-btn"
        className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-blue-gray-light text-white transition-opacity hover:opacity-90 md:h-10.5 md:w-10.5"
        aria-label="Search"
      >
        <SearchIcon aria-hidden="true" className="h-4 w-4" />
      </button>
    </div>
  );
});
