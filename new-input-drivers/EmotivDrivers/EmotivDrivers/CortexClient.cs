using System;
using System.Collections.Generic;
using System.Threading;
using WebSocket4Net;
using Newtonsoft.Json.Linq;
using SuperSocket.ClientEngine;

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
            NextRequestId = 1;
            WebSocketClient = new WebSocket(CortexURL);
            MethodForRequestID = new Dictionary<int, string>();
            
            SubscribeToEvents();
        }

        private void SubscribeToEvents() {
            WebSocketClient.Opened += new EventHandler(WebSocketClientOpened);
            
            WebSocketClient.Error += new EventHandler<SuperSocket.ClientEngine.ErrorEventArgs>(WebSocketClientError);
            
            WebSocketClient.Closed += new EventHandler(WebSocketClientClosed);
        }
        
        private void WebSocketClientOpened(object sender, EventArgs eventArgs) {
            OpenedEvent.Set();
        }

        private void WebSocketClientError(object sender, SuperSocket.ClientEngine.ErrorEventArgs eventArgs) {
            Console.Write(eventArgs.Exception.GetType() + ":" + 
                          eventArgs.Exception.Message + 
                          Environment.NewLine + 
                          eventArgs.Exception.StackTrace);

            if (eventArgs.Exception.InnerException != null) {
                Console.WriteLine(eventArgs.Exception.InnerException.GetType());
            }
        }

        private void WebSocketClientClosed(object sender, EventArgs eventArgs) {
            CloseEvent.Set();
        }
    }
}