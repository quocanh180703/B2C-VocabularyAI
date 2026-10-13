import { useState } from "react";

type PageState = "create" | "recording" | "result";

export function SpeakingPracticePage() {
  const [pageState, setPageState] = useState<PageState>("create");
  const [topic, setTopic] = useState("");
  const [level, setLevel] = useState("B1");
  const [sessionId, setSessionId] = useState<number | null>(null);
  const [isRecording, setIsRecording] = useState(false);
  const [duration, setDuration] = useState(0);
  const [result, setResult] = useState<any>(null);

  // Step 1: Create Session
  const handleCreateSession = () => {
    if (!topic.trim()) {
      alert("Vui lòng nhập chủ đề");
      return;
    }
    // Simulate API call - in real app, call backend
    const newSessionId = Math.floor(Math.random() * 10000);
    setSessionId(newSessionId);
    setPageState("recording");
    alert(`✅ Phiên tạo thành công!\nID: ${newSessionId}\nChủ đề: ${topic}\nMức độ: ${level}`);
  };

  // Step 2: Recording
  const handleStartRecording = () => {
    setIsRecording(true);
    setDuration(0);
    
    // Simulate recording timer
    const interval = setInterval(() => {
      setDuration((prev) => {
        if (prev >= 30) {
          clearInterval(interval);
          setIsRecording(false);
          return prev;
        }
        return prev + 1;
      });
    }, 1000);
  };

  const handleStopRecording = () => {
    setIsRecording(false);
    // Simulate API call - send audio to backend
    const mockResult = {
      transcript: "Hello, my name is Anh. I am from Vietnam. I like learning English.",
      scores: {
        grammar: 85,
        vocabulary: 80,
        fluency: 75,
        content: 90,
        overall: 82.5,
      },
      feedback: "Good pronunciation! Try to speak more naturally and avoid long pauses.",
    };
    setResult(mockResult);
    setPageState("result");
    alert("✅ Gửi xong! Đang phân tích...");
  };

  const handleRetry = () => {
    setDuration(0);
    setResult(null);
    setPageState("recording");
  };

  const handleNewSession = () => {
    setTopic("");
    setLevel("B1");
    setSessionId(null);
    setDuration(0);
    setResult(null);
    setPageState("create");
  };

  // PAGE 1: Create Session
  if (pageState === "create") {
    return (
      <div style={{ padding: "40px", maxWidth: "600px", margin: "0 auto", fontFamily: "Arial" }}>
        <h1>Speaking Practice</h1>
        <p style={{ color: "#666" }}>Luyện nói tiếng Anh cùng AI</p>

        <div style={{ marginTop: "30px", padding: "20px", border: "1px solid #ddd", borderRadius: "8px", backgroundColor: "#f9f9f9" }}>
          <h2 style={{ marginTop: 0 }}>Tạo phiên luyện tập</h2>

          <div style={{ marginTop: "20px" }}>
            <label style={{ display: "block", fontWeight: "bold", marginBottom: "8px" }}>
              Chủ đề *
            </label>
            <input
              type="text"
              value={topic}
              onChange={(e) => setTopic(e.target.value)}
              placeholder="vd: Tự giới thiệu, Mô tả gia đình"
              style={{
                width: "100%",
                padding: "10px",
                boxSizing: "border-box",
                border: "1px solid #ccc",
                borderRadius: "4px",
                fontSize: "14px",
              }}
            />
            <p style={{ fontSize: "12px", color: "#999", margin: "4px 0 0 0" }}>3-255 ký tự</p>
          </div>

          <div style={{ marginTop: "20px" }}>
            <label style={{ display: "block", fontWeight: "bold", marginBottom: "8px" }}>
              Mức độ *
            </label>
            <select
              value={level}
              onChange={(e) => setLevel(e.target.value)}
              style={{
                width: "100%",
                padding: "10px",
                boxSizing: "border-box",
                border: "1px solid #ccc",
                borderRadius: "4px",
                fontSize: "14px",
              }}
            >
              <option value="A1">A1 - Beginner</option>
              <option value="A2">A2 - Elementary</option>
              <option value="B1">B1 - Intermediate</option>
              <option value="B2">B2 - Upper Intermediate</option>
              <option value="C1">C1 - Advanced</option>
              <option value="C2">C2 - Proficiency</option>
            </select>
          </div>

          <button
            onClick={handleCreateSession}
            style={{
              width: "100%",
              padding: "12px",
              marginTop: "20px",
              backgroundColor: "#3b82f6",
              color: "white",
              border: "none",
              borderRadius: "6px",
              fontSize: "16px",
              fontWeight: "bold",
              cursor: "pointer",
              transition: "background-color 0.2s",
            }}
            onMouseEnter={(e) => (e.currentTarget.style.backgroundColor = "#2563eb")}
            onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = "#3b82f6")}
          >
            Bắt đầu luyện tập
          </button>
        </div>
      </div>
    );
  }

  // PAGE 2: Recording
  if (pageState === "recording") {
    return (
      <div style={{ padding: "40px", maxWidth: "600px", margin: "0 auto", fontFamily: "Arial" }}>
        <h1>Speaking Practice - Ghi Âm</h1>
        <p style={{ color: "#666" }}>
          Chủ đề: <strong>{topic}</strong> | Mức độ: <strong>{level}</strong>
        </p>

        <div style={{ marginTop: "30px", padding: "20px", border: "1px solid #ddd", borderRadius: "8px", backgroundColor: "#f9f9f9", textAlign: "center" }}>
          <h3>Ghi Âm Audio</h3>

          <div style={{ marginTop: "30px", fontSize: "48px", fontWeight: "bold", color: isRecording ? "#ef4444" : "#3b82f6" }}>
            {String(Math.floor(duration / 60)).padStart(2, "0")}:{String(duration % 60).padStart(2, "0")}
          </div>

          {isRecording && (
            <div style={{ marginTop: "20px", display: "flex", gap: "10px", justifyContent: "center" }}>
              <div
                style={{
                  width: "12px",
                  height: "12px",
                  backgroundColor: "#ef4444",
                  borderRadius: "50%",
                  animation: "pulse 1s infinite",
                }}
              />
              <span style={{ color: "#ef4444", fontWeight: "bold" }}>Đang ghi âm...</span>
            </div>
          )}

          <div style={{ marginTop: "30px", display: "flex", gap: "10px", justifyContent: "center" }}>
            {!isRecording ? (
              <button
                onClick={handleStartRecording}
                style={{
                  padding: "12px 24px",
                  backgroundColor: "#ef4444",
                  color: "white",
                  border: "none",
                  borderRadius: "6px",
                  fontSize: "16px",
                  fontWeight: "bold",
                  cursor: "pointer",
                }}
              >
                🎤 Bắt đầu ghi âm
              </button>
            ) : (
              <>
                <button
                  onClick={handleStopRecording}
                  style={{
                    padding: "12px 24px",
                    backgroundColor: "#22c55e",
                    color: "white",
                    border: "none",
                    borderRadius: "6px",
                    fontSize: "16px",
                    fontWeight: "bold",
                    cursor: "pointer",
                  }}
                >
                  ⏹️ Dừng & Gửi
                </button>
              </>
            )}
          </div>

          <p style={{ marginTop: "20px", fontSize: "12px", color: "#999" }}>
            Ghi âm 15-120 giây, nói tiếng Anh tự nhiên
          </p>
        </div>
      </div>
    );
  }

  // PAGE 3: Result
  if (pageState === "result" && result) {
    return (
      <div style={{ padding: "40px", maxWidth: "600px", margin: "0 auto", fontFamily: "Arial" }}>
        <h1>✅ Kết Quả Phân Tích</h1>
        <p style={{ color: "#666" }}>
          Chủ đề: <strong>{topic}</strong> | Thời gian: <strong>{duration}s</strong>
        </p>

        {/* Transcript */}
        <div style={{ marginTop: "30px", padding: "15px", backgroundColor: "#eff6ff", borderLeft: "4px solid #3b82f6", borderRadius: "4px" }}>
          <h3 style={{ marginTop: 0 }}>Bản Chép (Whisper)</h3>
          <p style={{ marginBottom: 0 }}>"{result.transcript}"</p>
        </div>

        {/* Scores */}
        <div style={{ marginTop: "20px", display: "grid", gridTemplateColumns: "1fr 1fr", gap: "15px" }}>
          <div style={{ padding: "15px", backgroundColor: "#f3f4f6", borderRadius: "4px", textAlign: "center" }}>
            <div style={{ fontSize: "12px", color: "#666" }}>Grammar</div>
            <div style={{ fontSize: "24px", fontWeight: "bold", color: "#3b82f6" }}>{result.scores.grammar}</div>
          </div>
          <div style={{ padding: "15px", backgroundColor: "#f3f4f6", borderRadius: "4px", textAlign: "center" }}>
            <div style={{ fontSize: "12px", color: "#666" }}>Vocabulary</div>
            <div style={{ fontSize: "24px", fontWeight: "bold", color: "#3b82f6" }}>{result.scores.vocabulary}</div>
          </div>
          <div style={{ padding: "15px", backgroundColor: "#f3f4f6", borderRadius: "4px", textAlign: "center" }}>
            <div style={{ fontSize: "12px", color: "#666" }}>Fluency</div>
            <div style={{ fontSize: "24px", fontWeight: "bold", color: "#3b82f6" }}>{result.scores.fluency}</div>
          </div>
          <div style={{ padding: "15px", backgroundColor: "#f3f4f6", borderRadius: "4px", textAlign: "center" }}>
            <div style={{ fontSize: "12px", color: "#666" }}>Content</div>
            <div style={{ fontSize: "24px", fontWeight: "bold", color: "#3b82f6" }}>{result.scores.content}</div>
          </div>
        </div>

        {/* Overall Score */}
        <div style={{ marginTop: "20px", padding: "20px", backgroundColor: "#ecfdf5", borderLeft: "4px solid #22c55e", borderRadius: "4px", textAlign: "center" }}>
          <div style={{ fontSize: "14px", color: "#666" }}>Điểm Tổng Thể</div>
          <div style={{ fontSize: "40px", fontWeight: "bold", color: "#22c55e" }}>{result.scores.overall}</div>
        </div>

        {/* Feedback */}
        <div style={{ marginTop: "20px", padding: "15px", backgroundColor: "#fef3c7", borderLeft: "4px solid #f59e0b", borderRadius: "4px" }}>
          <h3 style={{ marginTop: 0 }}>Phản Hồi</h3>
          <p style={{ marginBottom: 0 }}>{result.feedback}</p>
        </div>

        {/* Buttons */}
        <div style={{ marginTop: "30px", display: "flex", gap: "10px" }}>
          <button
            onClick={handleRetry}
            style={{
              flex: 1,
              padding: "12px",
              backgroundColor: "#3b82f6",
              color: "white",
              border: "none",
              borderRadius: "6px",
              fontSize: "16px",
              fontWeight: "bold",
              cursor: "pointer",
            }}
          >
            🔄 Thử Lại
          </button>
          <button
            onClick={handleNewSession}
            style={{
              flex: 1,
              padding: "12px",
              backgroundColor: "#6b7280",
              color: "white",
              border: "none",
              borderRadius: "6px",
              fontSize: "16px",
              fontWeight: "bold",
              cursor: "pointer",
            }}
          >
            ➕ Phiên Mới
          </button>
        </div>
      </div>
    );
  }

  return <div>Loading...</div>;
}

export default SpeakingPracticePage;
