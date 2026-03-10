import React from "react";
import type { UploadedImage } from "../../hooks/useImageUpload";

interface ImageUploadProps {
  image: UploadedImage | null;
  error: string | null;
  isDragging: boolean;
  inputRef: React.RefObject<HTMLInputElement>;
  onOpenPicker: () => void;
  onDrop: (e: React.DragEvent) => void;
  onDragOver: (e: React.DragEvent) => void;
  onDragLeave: () => void;
  onFileChange: (file: File) => void;
  onClear: () => void;
  
  existingUrl?: string;

  hideLabel?: boolean;
}

export function ImageUpload({
  image,
  error,
  isDragging,
  inputRef,
  onOpenPicker,
  onDrop,
  onDragOver,
  onDragLeave,
  onFileChange,
  onClear,
  existingUrl,
  hideLabel,
}: ImageUploadProps) {
  const previewSrc = image?.dataUrl ?? existingUrl ?? null;

  return (
    <div data-testid="image-upload-section">
      {!hideLabel && (
        <label className="block text-xs font-medium text-gray-700 mb-1.5">
          Image{" "}
          <span className="text-gray-400 font-normal">
            (optional · max 5 MB)
          </span>
        </label>
      )}

    
      <input
        data-testid="image-file-input"
        ref={inputRef}
        type="file"
        accept="image/jpeg,image/png,image/webp,image/gif"
        className="hidden"
        aria-label="Choose image file"
        title="Choose image file"
        onChange={(e) => {
          const f = e.target.files?.[0];
          if (f) onFileChange(f);
        }}
      />

      {previewSrc ? (
       
        <div
          data-testid="image-preview-container"
          className="relative rounded-lg overflow-hidden border border-borderstroke bg-primary"
        >
          <img
            data-testid="image-preview"
            src={previewSrc}
            alt="Post image preview"
            className="w-full max-h-56 object-cover"
          />
          
          <div className="absolute top-2 right-2 flex gap-1.5">
            <button
              data-testid="image-change-btn"
              type="button"
              onClick={onOpenPicker}
              className="text-[11px] font-semibold bg-white/90 backdrop-blur-sm text-gray-700 px-2.5 py-1 rounded-md shadow-sm hover:bg-white transition-colors border border-gray-200"
            >
              Change
            </button>
            <button
              data-testid="image-remove-btn"
              type="button"
              onClick={onClear}
              className="text-[11px] font-semibold bg-red-500/90 backdrop-blur-sm text-white px-2.5 py-1 rounded-md shadow-sm hover:bg-red-500 transition-colors"
            >
              Remove
            </button>
          </div>
          {image && (
            <div className="px-3 py-1.5 text-[11px] text-gray-500 bg-white border-t border-gray-100 flex items-center gap-1.5">
              <span className="truncate">{image.file.name}</span>
              <span className="text-gray-400 shrink-0">
                · {image.sizeLabel}
              </span>
            </div>
          )}
        </div>
      ) : (
       
        <div
          data-testid="image-dropzone"
          onDrop={onDrop}
          onDragOver={onDragOver}
          onDragLeave={onDragLeave}
          onClick={onOpenPicker}
          aria-label="Upload image"
          className={`
            rounded-lg border cursor-pointer transition-all duration-150
            ${hideLabel ? "h-40" : "py-7"}
            ${
              isDragging
                ? "border-blue-gray bg-primary scale-[1.01]"
                : "border-borderstroke bg-primary hover:opacity-80"
            }
          `}
        >
          {!hideLabel && (
            <div className="text-center">
              <p className="text-sm font-medium text-gray-600">
                {isDragging
                  ? "Drop to attach image"
                  : "Click to upload or drag & drop"}
              </p>
              <p className="text-xs text-gray-400 mt-0.5">
                JPEG, PNG, WebP, GIF · up to 5 MB
              </p>
            </div>
          )}
        </div>
      )}

      {error && (
        <p
          data-testid="image-upload-error"
          className="text-xs text-red-500 mt-1.5 flex items-center gap-1"
        >
          <span>⚠</span> {error}
        </p>
      )}
    </div>
  );
}
