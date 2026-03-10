import React from 'react';
import ReactDOM from 'react-dom/client';
import { ToastProvider } from './components/atoms/Toast';
import { AppRouter } from './router/AppRouter';
import { useAuthStore } from './features/auth/authStore';
import './index.css';


useAuthStore.getState().rehydrateAndValidate();

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ToastProvider>
      <AppRouter />
    </ToastProvider>
  </React.StrictMode>
);
