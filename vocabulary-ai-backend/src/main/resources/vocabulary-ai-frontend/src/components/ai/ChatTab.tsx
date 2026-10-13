import { useEffect, useRef } from "react";
import { Card } from "@/components/common/Card";
import { Badge } from "@/components/common/Badge";
import { Icon } from "@/components/common/Icon";
import {
  useConversations,
  useConversationDetail,
  useSendMessage,
  useCreateConversation,
} from "@/hooks/useAI";
import { useState } from "react";

export function ChatTab() {
  const [activeConversationId, setActiveConversationId] = useState<string | null>(null);
  const [messageText, setMessageText] = useState("");
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Queries and mutations
  const { data: conversations = [], isPending: conversationsLoading } = useConversations();
  const { data: conversationDetail, isPending: detailLoading } =
    useConversationDetail(activeConversationId);
  const { mutate: sendMessage, isPending: messageSending } = useSendMessage();
  const { mutate: createConversation, isPending: creatingConversation } =
    useCreateConversation();

  // Auto-scroll to latest message
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [conversationDetail?.messages]);

  const handleCreateConversation = () => {
    createConversation(
      { title: "Learning Practice", type: "LEARNING" },
      {
        onSuccess: (conversation) => {
          setActiveConversationId(String(conversation.id));
        },
      }
    );
  };

  const handleSendMessage = () => {
    if (!messageText.trim() || !activeConversationId) return;

    sendMessage(
      {
        conversationId: activeConversationId,
        request: { content: messageText },
      },
      {
        onSuccess: () => {
          setMessageText("");
        },
      }
    );
  };

  // Show loading while fetching conversations
  if (conversationsLoading) {
    return (
      <Card className="chat-card">
        <div className="chat-loading">
          <Icon name="spark" />
          <p>Loading conversations...</p>
        </div>
      </Card>
    );
  }

  // Show empty state with create button
  if (conversations.length === 0 && !activeConversationId) {
    return (
      <Card className="chat-card">
        <div className="chat-empty">
          <div className="chat-intro">
            <span className="ai-icon">
              <Icon name="spark" />
            </span>
            <div>
              <strong>VocabularyAI Tutor</strong>
              <p>Online · Ready to practice</p>
            </div>
          </div>
          <p>Start a new conversation to practice English with the AI tutor.</p>
          <button
            onClick={handleCreateConversation}
            disabled={creatingConversation}
            className="primary-button"
          >
            {creatingConversation ? "Creating..." : "Start New Conversation"}
          </button>
        </div>
      </Card>
    );
  }

  return (
    <Card className="chat-card">
      <div className="chat-container">
        {/* Sidebar - Conversations list */}
        {conversations.length > 0 && (
          <div className="chat-sidebar">
            <h3>Conversations</h3>
            <div className="conversations-list">
              {conversations.map((conv) => (
                <button
                  key={conv.id}
                  className={`conversation-item ${activeConversationId === String(conv.id) ? "active" : ""}`}
                  onClick={() => setActiveConversationId(String(conv.id))}
                >
                  {conv.title}
                </button>
              ))}
            </div>
            <button
              onClick={handleCreateConversation}
              disabled={creatingConversation}
              className="new-conversation-btn"
            >
              {creatingConversation ? "Creating..." : "+ New Conversation"}
            </button>
          </div>
        )}

        {/* Main chat area */}
        <div className="chat-main">
          {activeConversationId && conversationDetail ? (
            <>
              <div className="chat-header">
                <span className="ai-icon">
                  <Icon name="spark" />
                </span>
                <div>
                  <strong>{conversationDetail.title}</strong>
                  <p>Learning context: {conversationDetail.type}</p>
                </div>
              </div>

              <div className="messages">
                {detailLoading ? (
                  <div className="loading-spinner">
                    <Icon name="spark" />
                    <p>Loading messages...</p>
                  </div>
                ) : (
                  <>
                    {conversationDetail.messages.map((msg) => (
                      <div
                        key={msg.id}
                        className={`message ${msg.role === "USER" ? "user" : "ai"}`}
                      >
                        {msg.content}
                      </div>
                    ))}
                    <div ref={messagesEndRef} />
                  </>
                )}
              </div>

              <label className="chat-input">
                <input
                  value={messageText}
                  onChange={(e) => setMessageText(e.target.value)}
                  onKeyPress={(e) => {
                    if (e.key === "Enter" && !e.shiftKey) {
                      e.preventDefault();
                      handleSendMessage();
                    }
                  }}
                  placeholder="Ask your English question..."
                  disabled={messageSending}
                />
                <button
                  aria-label="Send"
                  onClick={handleSendMessage}
                  disabled={!messageText.trim() || messageSending}
                >
                  <Icon name={messageSending ? "loading" : "send"} />
                </button>
              </label>
            </>
          ) : (
            <div className="chat-empty-state">
              <Icon name="spark" />
              <p>Select a conversation to start chatting</p>
            </div>
          )}
        </div>
      </div>
    </Card>
  );
}
