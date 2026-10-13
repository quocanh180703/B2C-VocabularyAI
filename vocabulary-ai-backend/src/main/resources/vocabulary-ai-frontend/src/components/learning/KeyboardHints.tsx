interface KeyboardHintsProps {
  reveal?: boolean;
}

export function KeyboardHints({ reveal = true }: KeyboardHintsProps) {
  return (
    <div className="keyboard-hints">
      <span>
        <kbd>Space</kbd> {reveal ? "Reveal" : "Start"}
      </span>
      <span>
        <kbd>←</kbd> Didn't know
      </span>
      <span>
        <kbd>→</kbd> Knew it
      </span>
    </div>
  );
}
