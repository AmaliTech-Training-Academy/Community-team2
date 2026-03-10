import { useState, useRef, useEffect, useCallback } from "react";
import type { Category, Post } from "../../types";
import { CATEGORIES } from "../../types";
import { useAuthStore } from "../../features/auth/authStore";
import { usePostsStore } from "../../features/posts/postsStore";
import { useToast } from "../atoms/Toast";
import { ImageUpload } from "../atoms/ImageUpload";
import { useImageUpload } from "../../hooks/useImageUpload";
import HomeIcon from "../../assets/images/home.svg?react";

interface PostModalProps {
  post?: Post;
  onClose: () => void;
  onSaved?: (p: Post) => void;
}

type FormState = { title: string; category: Category | ""; body: string };
type FormErrors = Partial<Record<keyof FormState, string>>;

export function PostModal({ post, onClose, onSaved }: PostModalProps) {
  const user = useAuthStore((s) => s.user);
  const createPost = usePostsStore((s) => s.createPost);
  const updatePost = usePostsStore((s) => s.updatePost);
  const toast = useToast();

  const [form, setForm] = useState<FormState>({
    title: post?.title || "",
    category: post?.category || "",
    body: post?.body || "",
  });
  const [errors, setErrors] = useState<FormErrors>({});
  const [loading, setLoading] = useState(false);
  const [catOpen, setCatOpen] = useState(false);

  // ── Image upload state ──────────────────────────────────────────────────────
  const imgUpload = useImageUpload({ maxSizeMB: 5 });
  // When editing a post that already has an image, show it but allow replacement
  const existingImageUrl = post?.imageUrl;

  const catRef = useRef<HTMLDivElement>(null);
  const formRef = useRef(form);
  formRef.current = form;

  useEffect(() => {
    const fn = (e: MouseEvent) => {
      if (catRef.current && !catRef.current.contains(e.target as Node))
        setCatOpen(false);
    };
    document.addEventListener("mousedown", fn);
    return () => document.removeEventListener("mousedown", fn);
  }, []);

  useEffect(() => {
    const prevOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = prevOverflow;
    };
  }, []);

  const setField = useCallback(
    <K extends keyof FormState>(k: K, v: FormState[K]) => {
      setForm((f) => ({ ...f, [k]: v }));
    },
    [],
  );

  const validate = useCallback((): FormErrors => {
    const { title, category, body } = formRef.current;
    const e: FormErrors = {};
    if (!title.trim()) e.title = "Title is required";
    if (!category) e.category = "Please select a category";
    if (!body.trim()) e.body = "Content is required";
    return e;
  }, []);

  const handleSubmit = useCallback(async () => {
    const errs = validate();
    if (Object.keys(errs).length) {
      setErrors(errs);
      return;
    }
    setLoading(true);
    try {
      const { title } = formRef.current;
      const category = formRef.current.category as Category;
      const body = formRef.current.body;

      // Resolve the image to attach.
      // Priority: new upload > existing (preserved on edit) > none
      const imageUrl: string | undefined =
        imgUpload.image?.dataUrl ??
        (post?.imageUrl && !imgUpload.image ? post.imageUrl : undefined);

      let saved: Post;
      if (post) {
        await updatePost(post.id, { title, category, body, imageUrl });
        saved = { ...post, title, category, body, imageUrl } as Post;
        toast("Post updated successfully");
      } else {
        saved = await createPost({
          title,
          category,
          body,
          imageUrl,
          author: user!.name,
          authorId: user!.id,
        } as any);
        toast("Post created successfully");
      }
      onSaved?.(saved);
      onClose();
    } catch (err: unknown) {
      toast(String(err), "error");
    } finally {
      setLoading(false);
    }
  }, [
    validate,
    post,
    updatePost,
    createPost,
    user,
    toast,
    onSaved,
    onClose,
    imgUpload.image,
  ]);

  const toggleCat = useCallback(() => setCatOpen((o) => !o), []);
  const selectCat = useCallback(
    (c: Category) => {
      setField("category", c);
      setCatOpen(false);
    },
    [setField],
  );

  return (
    <div
      data-testid="post-modal-overlay"
      className="fixed inset-0 bg-transparent md:bg-black/45 z-500 flex items-start md:items-center justify-center p-0 md:p-4"
      onClick={(e) => e.target === e.currentTarget && onClose()}
    >
      <div
        data-testid="post-modal"
        className="modal-in bg-white w-full h-full md:h-auto md:rounded-2xl md:shadow-2xl md:max-w-md overflow-hidden md:max-h-[90vh] flex flex-col"
      >
        {/* Header */}
        <div className="px-6 pt-6 pb-4 shrink-0">
          {/* Mobile breadcrumb */}
          <div className="md:hidden mb-6">
            <div className="inline-flex items-center gap-3 bg-white border border-borderstroke rounded-lg px-4 py-2.5">
              <button
                type="button"
                onClick={onClose}
                className="flex items-center gap-2 text-body-sm font-semibold text-blue-gray-dark hover:opacity-70 transition-opacity bg-transparent border-none cursor-pointer p-0"
                aria-label="Go back"
              >
                <HomeIcon width={18} height={18} />
                <span>Home</span>
              </button>
              <span className="text-body-sm font-semibold text-blue-gray-dark">
                &gt;
              </span>
              <span className="text-body-sm font-semibold text-blue-gray-dark">
                {post ? "Edit Post" : "Create Post"}
              </span>
            </div>
          </div>

          <div className="flex items-center justify-between">
            <h2
              data-testid="post-modal-title"
              className="text-h-md md:text-[18px] md:leading-5.5 font-bold text-blue-gray-dark"
            >
              {post ? "Edit Post" : "Create New Post"}
            </h2>
            <button
              data-testid="post-modal-close-btn"
              onClick={onClose}
              className="hidden md:flex w-8 h-8 items-center justify-center text-blue-gray-dark hover:opacity-70 transition-opacity"
              aria-label="Close"
            >
              x
            </button>
          </div>
        </div>

        {/* Scrollable body */}
        <div className="px-6 pb-6 flex flex-col gap-5 overflow-y-auto">
          {/* Title */}
          <div>
            <label className="block text-body-sm font-semibold text-blue-gray-dark mb-2">
              Post Title
            </label>
            <input
              data-testid="post-title-input"
              className={`w-full px-4 py-3 border rounded-lg text-body-lg bg-primary text-blue-gray-dark placeholder:text-gray-400 focus:outline-none transition-colors ${
                errors.title
                  ? "border-red-400 bg-red-50"
                  : "border-borderstroke focus:border-blue-gray"
              }`}
              placeholder="Enter a clear, descriptive title"
              value={form.title}
              onChange={(e) => setField("title", e.target.value)}
            />
            {errors.title && (
              <p
                data-testid="post-title-error"
                className="text-body-sm text-red-500 mt-1"
              >
                ⚠ {errors.title}
              </p>
            )}
          </div>

          {/* Category */}
          <div>
            <label className="block text-body-sm font-semibold text-blue-gray-dark mb-2">
              Category
            </label>
            <div className="relative" ref={catRef}>
              <div
                data-testid="post-category-select"
                className={`w-full px-4 py-3 border rounded-lg text-body-lg cursor-pointer flex justify-between items-center bg-primary ${
                  errors.category
                    ? "border-red-400 bg-red-50"
                    : "border-borderstroke"
                }`}
                onClick={toggleCat}
              >
                <span
                  className={
                    form.category ? "text-blue-gray-dark" : "text-gray-400"
                  }
                >
                  {form.category || "Select"}
                </span>
                <span className="text-blue-gray text-body-sm font-semibold">
                  {catOpen ? "▴" : "▾"}
                </span>
              </div>
              {catOpen && (
                <div
                  data-testid="post-category-dropdown"
                  className="slide-down absolute top-full left-0 right-0 z-50 bg-white border border-borderstroke rounded-lg shadow-lg overflow-hidden mt-2"
                >
                  {CATEGORIES.map((c) => (
                    <div
                      key={c}
                      data-testid={`post-category-option-${c.toLowerCase().replace(/[^a-z]/g, "-")}`}
                      className="px-4 py-3 text-body-lg text-blue-gray-dark cursor-pointer hover:bg-gray-100 transition-colors"
                      onClick={() => selectCat(c)}
                    >
                      {c}
                    </div>
                  ))}
                </div>
              )}
            </div>
            {errors.category && (
              <p
                data-testid="post-category-error"
                className="text-body-sm text-red-500 mt-1"
              >
                ⚠ {errors.category}
              </p>
            )}
          </div>

          {/* Content */}
          <div>
            <textarea
              data-testid="post-body-input"
              className={`w-full px-4 py-3 border rounded-lg text-body-lg bg-primary text-blue-gray-dark placeholder:text-gray-400 focus:outline-none transition-colors resize-none min-h-52 md:min-h-40 ${
                errors.body
                  ? "border-red-400 bg-red-50"
                  : "border-borderstroke focus:border-blue-gray"
              }`}
              placeholder="Share the details of your post…"
              value={form.body}
              onChange={(e) => setField("body", e.target.value)}
            />
            {errors.body && (
              <p
                data-testid="post-body-error"
                className="text-body-sm text-red-500 mt-1"
              >
                ⚠ {errors.body}
              </p>
            )}
          </div>

          {/* Image upload */}
          <div className="hidden md:block">
            <ImageUpload
              image={imgUpload.image}
              error={imgUpload.error}
              isDragging={imgUpload.isDragging}
              inputRef={imgUpload.inputRef}
              onOpenPicker={imgUpload.openPicker}
              onDrop={imgUpload.handleDrop}
              onDragOver={imgUpload.handleDragOver}
              onDragLeave={imgUpload.handleDragLeave}
              onFileChange={imgUpload.handleFile}
              onClear={imgUpload.clearImage}
              existingUrl={existingImageUrl}
              hideLabel
            />
          </div>

          {/* Actions */}
          <div className="flex gap-4 pt-2">
            <button
              data-testid="post-modal-cancel-btn"
              onClick={onClose}
              className="flex-1 h-11 text-body-lg font-semibold text-blue-gray-dark bg-white border border-blue-gray-dark rounded-lg hover:opacity-80 transition-opacity"
            >
              Cancel
            </button>
            <button
              data-testid="post-modal-submit-btn"
              onClick={handleSubmit}
              disabled={loading}
              className="flex-1 h-11 text-body-lg font-semibold text-white bg-blue-gray-dark rounded-lg hover:opacity-90 transition-opacity disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              {loading && (
                <span className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              )}
              {loading
                ? post
                  ? "Saving…"
                  : "Creating…"
                : post
                  ? "Save Changes"
                  : "Create Post"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
