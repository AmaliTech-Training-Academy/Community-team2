export function Spinner() {
  return (
    <div
      data-testid="loading-spinner"
      className="flex justify-center items-center py-16"
    >
      <div className="spinner w-6 h-6 border-[3px] border-gray-200 border-t-navy rounded-full" />
    </div>
  );
}
