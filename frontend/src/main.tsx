import React from 'react';
import ReactDOM from 'react-dom/client';
import { ToastProvider } from './components/atoms/Toast';
import { AppRouter } from './router/AppRouter';
import { useAuthStore } from './features/auth/authStore';
import './index.css';

// Validate the persisted JWT before rendering anything.
// If the token is expired, the store is wiped and the router will redirect
// to /login.  This runs synchronously before the first paint so there is
// no flash of authenticated content.
useAuthStore.getState().rehydrateAndValidate();

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ToastProvider>
      <AppRouter />
    </ToastProvider>
  </React.StrictMode>
);
