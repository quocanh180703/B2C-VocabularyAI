import { ReactNode } from "react";
import { IconName } from "@/types";

interface IconProps {
  name: IconName;
  size?: "sm" | "md" | "lg";
}

const iconPaths: Record<IconName, ReactNode> = {
  home: <><path d="m3 10 9-7 9 7" /><path d="M5 9v11h14V9M9 20v-7h6v7" /></>,
  book: <><path d="M4 4.5A2.5 2.5 0 0 1 6.5 2H11v18H6.5A2.5 2.5 0 0 0 4 22Z" /><path d="M20 4.5A2.5 2.5 0 0 0 17.5 2H13v18h4.5A2.5 2.5 0 0 1 20 22Z" /></>,
  cards: <><rect x="4" y="3" width="16" height="18" rx="2" /><path d="M8 8h8M8 12h5" /></>,
  quiz: <><path d="M9 3h6l1 2h3v16H5V5h3Z" /><path d="m9 13 2 2 4-5" /></>,
  spark: <><path d="m12 2 1.5 5.5L19 9l-5.5 1.5L12 16l-1.5-5.5L5 9l5.5-1.5Z" /><path d="m19 16 .7 2.3L22 19l-2.3.7L19 22l-.7-2.3L16 19l2.3-.7Z" /></>,
  mic: <><rect x="9" y="2" width="6" height="12" rx="3" /><path d="M5 11a7 7 0 0 0 14 0M12 18v4M8 22h8" /></>,
  chart: <><path d="M4 20V10M10 20V4M16 20v-7M22 20H2" /></>,
  settings: <><circle cx="12" cy="12" r="3" /><path d="M19.4 15a1.7 1.7 0 0 0 .3 1.9l.1.1-2.8 2.8-.1-.1a1.7 1.7 0 0 0-1.9-.3 1.7 1.7 0 0 0-1 1.6v.2h-4V21a1.7 1.7 0 0 0-1-1.6 1.7 1.7 0 0 0-1.9.3l-.1.1L4.2 17l.1-.1a1.7 1.7 0 0 0 .3-1.9A1.7 1.7 0 0 0 3 14H2.8v-4H3a1.7 1.7 0 0 0 1.6-1 1.7 1.7 0 0 0-.3-1.9L4.2 7 7 4.2l.1.1A1.7 1.7 0 0 0 9 4.6a1.7 1.7 0 0 0 1-1.6v-.2h4V3a1.7 1.7 0 0 0 1 1.6 1.7 1.7 0 0 0 1.9-.3l.1-.1L19.8 7l-.1.1a1.7 1.7 0 0 0-.3 1.9 1.7 1.7 0 0 0 1.6 1h.2v4H21a1.7 1.7 0 0 0-1.6 1Z" /></>,
  search: <><circle cx="11" cy="11" r="7" /><path d="m20 20-4-4" /></>,
  bell: <><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4" /></>,
  fire: <path d="M13 22c4.5-.7 7-3.7 7-7.8 0-3.2-1.8-6.2-5.1-8.7.1 2-1 3.7-2.2 4.6.1-3.2-1.8-6.2-4.2-8.1.2 4-4.5 6.4-4.5 12.2 0 4.1 2.5 7.1 7 7.8-1.8-1.2-2.4-3.5-.7-5.8.3 1.5 1.2 2.2 1.9 2.6-.2-1.9.8-3.5 2.1-4.8.3 2.4 2.4 4.8-1.3 8Z" />,
  target: <><circle cx="12" cy="12" r="9" /><circle cx="12" cy="12" r="4" /><path d="m15 9 6-6M17 3h4v4" /></>,
  clock: <><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 2" /></>,
  arrow: <><path d="M5 12h14M14 7l5 5-5 5" /></>,
  volume: <><path d="M5 10v4h4l5 4V6L9 10ZM17 9a4 4 0 0 1 0 6M19 6a8 8 0 0 1 0 12" /></>,
  star: <path d="m12 3 2.8 5.7 6.2.9-4.5 4.4 1.1 6.2-5.6-2.9-5.6 2.9 1.1-6.2L3 9.6l6.2-.9Z" />,
  check: <path d="m5 12 4 4L19 6" />,
  close: <path d="m6 6 12 12M18 6 6 18" />,
  brain: <><path d="M9.5 4A3.5 3.5 0 0 0 6 7.5v.7a3 3 0 0 0-1 5.7V15a4 4 0 0 0 4 4h1V5.5A1.5 1.5 0 0 0 8.5 4" /><path d="M14.5 4A3.5 3.5 0 0 1 18 7.5v.7a3 3 0 0 1 1 5.7V15a4 4 0 0 1-4 4h-1V5.5A1.5 1.5 0 0 1 15.5 4M7 10h3M14 9h3M7 15h3M14 14h3" /></>,
  menu: <path d="M4 7h16M4 12h16M4 17h16" />,
  chevron: <path d="m9 18 6-6-6-6" />,
  send: <path d="m3 3 18 9-18 9 4-9Zm4 9h14" />,
  sun: <><circle cx="12" cy="12" r="4" /><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" /></>,
  moon: <path d="M20.5 15.5A8.5 8.5 0 0 1 8.5 3.5a8.5 8.5 0 1 0 12 12Z" />,
  user: <><circle cx="12" cy="8" r="4" /><path d="M4 21a8 8 0 0 1 16 0" /></>,
  mail: <><rect x="3" y="5" width="18" height="14" rx="2" /><path d="m4 7 8 6 8-6" /></>,
  lock: <><rect x="5" y="10" width="14" height="11" rx="2" /><path d="M8 10V7a4 4 0 0 1 8 0v3M12 14v3" /></>,
  eye: <><path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6S2 12 2 12Z" /><circle cx="12" cy="12" r="2.5" /></>,
};

export function Icon({ name, size = "md" }: IconProps) {
  return (
    <svg
      className={`icon icon-${size}`}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {iconPaths[name]}
    </svg>
  );
}
