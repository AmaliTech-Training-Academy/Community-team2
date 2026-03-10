function SearchIcon({ className = "" }: { className?: string }) {
  return (
    <svg
      width="16"
      height="16"
      viewBox="0 0 16 16"
      fill="none"
      aria-hidden="true"
      className={className}
    >
      <circle cx="7" cy="7" r="4.5" stroke="currentColor" strokeWidth="1.5" />
      <path
        d="M10.5 10.5L13.5 13.5"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
      />
    </svg>
  );
}

interface SearchBarProps {
  value: string;
  onChange: (v: string) => void;
}

export function SearchBar({ value, onChange }: SearchBarProps) {
  return (
    <div data-testid="search-bar" className="flex items-center gap-2">
      {/* Input with SVG icon + clear */}
      <div className="relative flex-1 min-w-0">
        <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none">
          <SearchIcon />
        </span>
        <input
          data-testid="search-input"
          className="w-full pl-9 pr-8 h-10 border border-borderstroke rounded-lg text-body-lg bg-white text-blue-gray-dark placeholder:text-gray-400 focus:outline-none focus:border-navy transition-colors"
          placeholder="Search by title of post..."
          value={value}
          onChange={(e) => onChange(e.target.value)}
        />
        {value && (
          <button
            data-testid="search-clear-btn"
            onClick={() => onChange("")}
            className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 text-lg leading-none"
          >
            ×
          </button>
        )}
      </div>

      {/* Navy search submit button */}
      <button
        data-testid="search-submit-btn"
        className="shrink-0 w-10 h-10 flex items-center justify-center bg-blue-gray-dark rounded-lg text-white hover:opacity-90 transition-opacity"
        aria-label="Search"
      >
        <SearchIcon />
      </button>
    </div>
  );
}
