import { useState, useCallback, useRef } from "react";

export interface UploadedImage {
  
  dataUrl: string;
  
  file: File;
  sizeLabel: string;
}

interface UseImageUploadOptions {
  maxSizeMB?: number;
  accept?: string[];
}

interface UseImageUploadReturn {
  image: UploadedImage | null;
  error: string | null;
  isDragging: boolean;
  inputRef: React.RefObject<HTMLInputElement>;
  openPicker: () => void;
  handleFile: (file: File) => void;
  handleDrop: (e: React.DragEvent) => void;
  handleDragOver: (e: React.DragEvent) => void;
  handleDragLeave: () => void;
  clearImage: () => void;
}

const DEFAULT_ACCEPT = ["image/jpeg", "image/png", "image/webp", "image/gif"];
const DEFAULT_MAX_MB = 5;

function humanSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export function useImageUpload(
  opts: UseImageUploadOptions = {},
): UseImageUploadReturn {
  const maxBytes = (opts.maxSizeMB ?? DEFAULT_MAX_MB) * 1024 * 1024;
  const accept = opts.accept ?? DEFAULT_ACCEPT;

  const [image, setImage] = useState<UploadedImage | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  const handleFile = useCallback(
    (file: File) => {
      setError(null);

      if (!accept.includes(file.type)) {
        setError(
          `Unsupported file type. Please upload a JPEG, PNG, WebP, or GIF.`,
        );
        return;
      }
      if (file.size > maxBytes) {
        setError(
          `File too large. Maximum size is ${opts.maxSizeMB ?? DEFAULT_MAX_MB} MB.`,
        );
        return;
      }

      const reader = new FileReader();
      reader.onload = () => {
        setImage({
          dataUrl: reader.result as string,
          file,
          sizeLabel: humanSize(file.size),
        });
      };
      reader.onerror = () =>
        setError("Failed to read the file. Please try again.");
      reader.readAsDataURL(file);
    },
    [accept, maxBytes, opts.maxSizeMB],
  );

  const openPicker = useCallback(() => inputRef.current?.click(), []);

  const handleDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault();
      setIsDragging(false);
      const file = e.dataTransfer.files[0];
      if (file) handleFile(file);
    },
    [handleFile],
  );

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  }, []);

  const handleDragLeave = useCallback(() => setIsDragging(false), []);

  const clearImage = useCallback(() => {
    setImage(null);
    setError(null);
    if (inputRef.current) inputRef.current.value = "";
  }, []);

  return {
    image,
    error,
    isDragging,
    inputRef,
    openPicker,
    handleFile,
    handleDrop,
    handleDragOver,
    handleDragLeave,
    clearImage,
  };
}
