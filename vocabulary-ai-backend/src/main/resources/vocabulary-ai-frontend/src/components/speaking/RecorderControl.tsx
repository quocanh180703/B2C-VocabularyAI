import { Icon } from "@/components/common/Icon";

type RecorderStage = "ready" | "recording" | "processing";

interface RecorderControlProps {
  stage: RecorderStage;
  onToggleRecording: () => void;
}

export function RecorderControl({ stage, onToggleRecording }: RecorderControlProps) {
  const messages = {
    ready: {
      title: "Record your answer",
      subtitle: "Tap the microphone when you're ready",
    },
    recording: {
      title: "Recording... 00:18",
      subtitle: "Tap again to finish",
    },
    processing: {
      title: "Analyzing your answer...",
      subtitle: "Speech to text complete · AI evaluation in progress",
    },
  };

  const msg = messages[stage];

  return (
    <div className={`recorder ${stage}`}>
      <button
        aria-label={stage === "recording" ? "Stop recording" : "Start recording"}
        onClick={onToggleRecording}
      >
        <span>
          <Icon name={stage === "processing" ? "spark" : "mic"} size="lg" />
        </span>
      </button>
      <h3>{msg.title}</h3>
      <p>{msg.subtitle}</p>
      {stage === "recording" && (
        <div className="wave">
          {Array.from({ length: 24 }).map((_, i) => (
            <span key={i} />
          ))}
        </div>
      )}
    </div>
  );
}
