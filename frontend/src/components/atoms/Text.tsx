import React from "react";


export type TextVariant =
  | "h-xl"    
  | "h-lg"    
  | "h-md"    
  | "h-sm"     
  | "body-lg"  
  | "body-md" 
  | "body-sm"  
  | "label";   

type TextElement =
  | "h1"
  | "h2"
  | "h3"
  | "h4"
  | "h5"
  | "h6"
  | "p"
  | "span"
  | "div"
  | "label"
  | "strong"
  | "em";


const DEFAULT_ELEMENT: Record<TextVariant, TextElement> = {
  "h-xl":    "h1",
  "h-lg":    "h2",
  "h-md":    "h3",
  "h-sm":    "h4",
  "body-lg": "p",
  "body-md": "p",
  "body-sm": "p",
  "label":   "label",
};



interface TextProps extends React.HTMLAttributes<HTMLElement> {

  variant: TextVariant;
 
  as?: TextElement;
  children: React.ReactNode;
}

export function Text({
  variant,
  as,
  className = "",
  children,
  ...rest
}: TextProps) {
  const Tag = (as ?? DEFAULT_ELEMENT[variant]) as React.ElementType;

  return (
    <Tag
      className={["text-" + variant, className].filter(Boolean).join(" ")}
      {...rest}
    >
      {children}
    </Tag>
  );
}
