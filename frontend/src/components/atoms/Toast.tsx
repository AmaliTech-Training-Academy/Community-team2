import {
  createContext,
  useCallback,
  useContext,
  useState,
  type ReactNode,
} from "react";
import type { Toast as ToastType } from "../../types";
import SuccessIcon from "../../assets/images/success.svg?react";
import CloseIcon from "../../assets/images/close.svg?react";

const ToastCtx = createContext<(msg: string, type?: ToastType["type"]) => void>(
  () => {},
);
export const useToast = () => useContext(ToastCtx);

const TOAST_STYLES: Record<
  ToastType["type"],
  { wrap: string; iconBox: string; text: string; close: string }
> = {
  success: {
    wrap: "bg-[#DEF7EC] ",
    iconBox: "bg-[#046C4E] ",
    text: "text-[#046C4E]",
    close: "text-[#046C4E] ",
  },
  error: {
    wrap: "bg-red-50 ",
    iconBox: "bg-red-600 text-white",
    text: "text-red-700",
    close: "text-red-700 ",
  },
  warning: {
    wrap: "bg-amber-50 ",
    iconBox: "bg-amber-500 text-white",
    text: "text-amber-800",
    close: "text-amber-800 ",
  },
};

export function ToastProvider({ children }: { children: ReactNode }) {
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
              className={`toast-in flex items-center gap-4 rounded-xl px-5 py-4 shadow-lg  ${s.wrap} min-w-80 max-w-96 pointer-events-auto`}
            >
              <div
                className={`w-9 h-9 rounded-lg flex items-center justify-center shrink-0 ${s.iconBox}`}
                aria-hidden="true"
              >
                {t.type === "success" ? (
                  <SuccessIcon aria-hidden="true" className="h-8 w-8" />
                ) : (
                  <span className="text-lg leading-none font-bold">
                    {t.type === "error" ? "✕" : "!"}
                  </span>
                )}
              </div>

              <div className={`flex-1 text-body-lg ${s.text}`}>{t.message}</div>

              <button
                data-testid="toast-close-btn"
                onClick={() => remove(t.id)}
                className={`ml-auto w-8 h-8 rounded-lg flex items-center justify-center ${s.close}`}
                aria-label="Close"
              >
                <CloseIcon
                  aria-hidden="true"
                  className="h-4 w-4 text-[#046C4E]"
                />
              </button>
            </div>
          );
        })}
      </div>
    </ToastCtx.Provider>
  );
}
