import React, { useState, useEffect, useRef, useCallback } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import axios from "axios";
import "./App.css";

function App() {
  const [connected, setConnected] = useState(false);
  const [messages, setMessages] = useState([]);
  const [messageInput, setMessageInput] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [token, setToken] = useState("");
  const [error, setError] = useState("");
  const stompClient = useRef(null);

  const connectWebSocket = useCallback(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS("http://localhost:8080/ws"),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      debug: (str) => {
        console.log("STOMP: " + str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = (frame) => {
      setConnected(true);
      console.log("Connected: " + frame);

      client.subscribe("/topic/public", (message) => {
        const newMessage = JSON.parse(message.body);
        setMessages((prevMessages) => [...prevMessages, newMessage]);
      });

      client.subscribe(`/user/${username}/queue/private`, (message) => {
        const newMessage = JSON.parse(message.body);
        setMessages((prevMessages) => [...prevMessages, newMessage]);
      });
    };

    client.onDisconnect = () => {
      setConnected(false);
      console.log("Disconnected");
    };

    client.activate();
    stompClient.current = client;
  }, [token, username]);

  useEffect(() => {
    if (token) {
      connectWebSocket();
    }
    return () => {
      if (stompClient.current) {
        stompClient.current.deactivate();
      }
    };
  }, [token, connectWebSocket]);

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post("http://localhost:8080/authenticate", {
        username,
        password,
      });
      setToken(response.data.jwt);
      setError("");
    } catch (err) {
      setError("Login failed. Please check your credentials.");
      console.error("Login error:", err);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    try {
      await axios.post("http://localhost:8080/users/register", {
        username,
        password,
      });
      setError("Registration successful! Please login.");
    } catch (err) {
      setError("Registration failed. Please try again.");
      console.error("Registration error:", err);
    }
  };

  const sendMessage = (e) => {
    e.preventDefault();
    if (stompClient.current && messageInput) {
      const message = {
        sender: username,
        content: messageInput,
      };
      stompClient.current.publish({
        destination: "/app/chat.public",
        body: JSON.stringify(message),
      });
      setMessageInput("");
    }
  };

  return (
    <div className="App">
      {!token ? (
        <div className="auth-container">
          <h2>Chat Application</h2>
          {error && <div className="error">{error}</div>}
          <form onSubmit={handleLogin} className="auth-form">
            <input
              type="text"
              placeholder="Username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
            />
            <input
              type="password"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            <button type="submit">Login</button>
            <button type="button" onClick={handleRegister}>
              Register
            </button>
          </form>
        </div>
      ) : (
        <div className="chat-container">
          <h2>Welcome, {username}!</h2>
          <div className="messages">
            {messages.map((msg, index) => (
              <div
                key={index}
                className={`message ${msg.sender === username ? "own" : ""}`}
              >
                <strong>{msg.sender}:</strong> {msg.content}
              </div>
            ))}
          </div>
          <form onSubmit={sendMessage} className="message-form">
            <input
              type="text"
              value={messageInput}
              onChange={(e) => setMessageInput(e.target.value)}
              placeholder="Type a message..."
            />
            <button type="submit">Send</button>
          </form>
        </div>
      )}
    </div>
  );
}

export default App;
