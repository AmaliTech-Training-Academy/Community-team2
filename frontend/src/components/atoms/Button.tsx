import React from 'react';

type Variant = 'primary' | 'secondary' | 'danger';
type Size = 'sm' | 'md';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  size?: Size;
  loading?: boolean;
  children: React.ReactNode;
}

const BASE = 'inline-flex items-center justify-center gap-1.5 rounded-lg font-semibold border transition-all duration-150 disabled:opacity-60 disabled:cursor-not-allowed';
const VARIANTS = {
  primary: 'bg-navy text-white border-transparent hover:bg-[#1a3347]',
  secondary: 'bg-gray-100 text-gray-700 border-gray-200 hover:bg-gray-200',
  danger: 'bg-red-50 text-red-600 border-red-200 hover:bg-red-100',
};
const SIZES = { sm: 'px-3 py-1.5 text-xs', md: 'px-4 py-2.5 text-sm' };

export function Button({ variant = 'primary', size = 'md', loading, children, className = '', ...props }: ButtonProps) {
  return (
    <button className={`${BASE} ${VARIANTS[variant]} ${SIZES[size]} ${className}`} disabled={loading || props.disabled} {...props}>
      {loading ? 'Loading…' : children}
    </button>
  );
}
