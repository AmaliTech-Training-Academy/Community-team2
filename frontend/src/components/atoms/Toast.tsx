import React, { createContext, useCallback, useContext, useState } from "react";
import type { Toast as ToastType } from "../../types";

const ToastCtx = createContext<(msg: string, type?: ToastType["type"]) => void>(
  () => {},
);
export const useToast = () => useContext(ToastCtx);

const ICONS = { success: "✓", error: "✕", warning: "!" };

const TOAST_STYLES: Record<
  ToastType["type"],
  { wrap: string; iconBox: string; text: string; close: string }
> = {
  success: {
    wrap: "bg-green-50 border-green-200",
    iconBox: "bg-green-600 text-white",
    text: "text-green-700",
    close: "text-green-700 hover:opacity-70",
  },
  error: {
    wrap: "bg-red-50 border-red-200",
    iconBox: "bg-red-600 text-white",
    text: "text-red-700",
    close: "text-red-700 hover:opacity-70",
  },
  warning: {
    wrap: "bg-amber-50 border-amber-200",
    iconBox: "bg-amber-500 text-white",
    text: "text-amber-800",
    close: "text-amber-800 hover:opacity-70",
  },
};

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<ToastType[]>([]);
  const add = useCallback(
    (message: string, type: ToastType["type"] = "success") => {
      const id = Date.now();
      setToasts((t) => [...t, { id, message, type }]);
      setTimeout(() => setToasts((t) => t.filter((x) => x.id !== id)), 3500);
    },
    [],
  );
  const remove = (id: number) => setToasts((t) => t.filter((x) => x.id !== id));

  return (
    <ToastCtx.Provider value={add}>
      {children}
      <div
        data-testid="toast-container"
        className="fixed top-4 right-4 z-9999 flex flex-col gap-3 pointer-events-none"
      >
        {toasts.map((t) => {
          const s = TOAST_STYLES[t.type];
          return (
            <div
              key={t.id}
              data-testid={`toast-${t.type}`}
              data-toast-id={t.id}
              className={`toast-in flex items-center gap-4 rounded-xl px-5 py-4 shadow-lg border ${s.wrap} min-w-80 max-w-96 pointer-events-auto`}
            >
              <div
                className={`w-9 h-9 rounded-lg flex items-center justify-center shrink-0 ${s.iconBox}`}
                aria-hidden="true"
              >
                <span className="text-lg leading-none font-bold">
                  {ICONS[t.type]}
                </span>
              </div>

              <div className={`flex-1 text-body-lg ${s.text}`}>{t.message}</div>

              <button
                data-testid="toast-close-btn"
                onClick={() => remove(t.id)}
                className={`ml-auto w-8 h-8 rounded-lg flex items-center justify-center ${s.close}`}
                aria-label="Close"
              >
                x
              </button>
            </div>
          );
        })}
      </div>
    </ToastCtx.Provider>
  );
}
