import { useState, useEffect, useCallback } from "react";
import CloseIcon from "../../assets/images/close.svg?react";

interface ImageViewerProps {
  src: string;
  alt: string;
  className?: string;
  "data-testid"?: string;
}

export function ImageViewer({
  src,
  alt,
  className,
  "data-testid": testId,
}: ImageViewerProps) {
  const [isOpen, setIsOpen] = useState(false);

  const handleClose = useCallback(() => {
    setIsOpen(false);
  }, []);

  useEffect(() => {
    if (!isOpen) return;

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        handleClose();
      }
    };

    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, [isOpen, handleClose]);

  return (
    <>
      {/* Thumbnail */}
      <img
        data-testid={testId || "image-viewer-thumbnail"}
        src={src}
        alt={alt}
        onClick={() => setIsOpen(true)}
        className={
          className ||
          "max-w-sm h-auto rounded-lg object-contain cursor-pointer hover:opacity-90 transition-opacity"
        }
      />

      {/* Fullscreen Modal */}
      {isOpen && (
        <div
          data-testid="image-viewer-overlay"
          className="fixed inset-0 bg-black/80 z-500 flex items-center justify-center p-4"
          onClick={(e) => e.target === e.currentTarget && handleClose()}
        >
          {/* Close Button */}
          <button
            data-testid="image-viewer-close-btn"
            onClick={handleClose}
            className="absolute top-4 right-4 z-50 text-white hover:opacity-70 transition-opacity bg-black/50 rounded-full p-2 flex items-center justify-center"
            aria-label="Close image viewer"
            title="Close (Esc)"
          >
            <CloseIcon width={24} height={24} />
          </button>

          {/* Image Container */}
          <div className="relative max-w-4xl max-h-[90vh] flex items-center justify-center">
            <img
              data-testid="image-viewer-fullscreen"
              src={src}
              alt={alt}
              className="max-w-full max-h-full object-contain"
            />
          </div>
        </div>
      )}
    </>
  );
}
