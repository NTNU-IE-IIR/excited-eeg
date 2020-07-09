using System;
using System.Collections.Generic;
using System.Threading;
using WebSocket4Net;
using Newtonsoft.Json.Linq;

namespace EmotivDrivers {
    
    public sealed class CortexClient {

        private const string CortexURL = "wss://localhost:6868";
        private const string ClientId = "giK2jIkOy5x0Ry1xwixpdbAykYZi1Ebr3xjv7Asy";
        private const string ClientSecret =
            "SzXU4drfTJuAshSb9wInyvE3MYx5Z0jAZR1Au0b2ETXp0F8T7wpbTYXzAqezvJxYM7u9UJndXkiKdUDD6hE5h0G5ZbEQdtQcCn43PJXzme9DHsS95alVLQtCDGgql4Ot";

        private string CurrentMessage = string.Empty;
        private Dictionary<int, string> MethodForRequestID;

        private WebSocket WebSocketClient;
        private int NextRequestId;
        private bool IsWebSocketClientConnected;
        
        // Events
        private AutoResetEvent MessageReceivedEvent = new AutoResetEvent(false);
        private AutoResetEvent OpenedEvent = new AutoResetEvent(false);
        private AutoResetEvent CloseEvent = new AutoResetEvent(false);
        
        static CortexClient() {}

        private CortexClient() {
            
        }
    }
}