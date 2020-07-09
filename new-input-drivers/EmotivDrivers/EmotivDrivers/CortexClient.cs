﻿using System;
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

        private string currentMessage = string.Empty;
        private Dictionary<int, string> methodForRequestID;

        private WebSocket webSocketClient;
        private int nextRequestId;
        private bool isWebSocketClientConnected;
        
        public bool IsWebSocketClientConnected {
            get => isWebSocketClientConnected;
        }

        /// <summary>
        /// Events
        /// </summary>
        private AutoResetEvent MessageReceivedEvent = new AutoResetEvent(false);
        private AutoResetEvent OpenedEvent = new AutoResetEvent(false);
        private AutoResetEvent CloseEvent = new AutoResetEvent(false);
        
        static CortexClient() {}

        private CortexClient() {
            nextRequestId = 1;
            webSocketClient = new WebSocket(CortexURL);
            methodForRequestID = new Dictionary<int, string>();
            
            SubscribeToEvents();
        }
        
        public static CortexClient Instance { get; } = new CortexClient();

        private void SendWebSocketMessage(JObject param, string method, bool hasParam) {
            JObject request = new JObject(
                new JProperty("jsonrpc", "2.0"), 
                new JProperty("id", nextRequestId), 
                new JProperty("method", method));

            if (hasParam) {
                request.Add("params", param);
            }
            
            Console.WriteLine("Send " + method);
            
            webSocketClient.Send(request.ToString());
            methodForRequestID.Add(nextRequestId, method);
            nextRequestId++;
        }
        
        private void SubscribeToEvents() {
            webSocketClient.Opened += new EventHandler(WebSocketClientOpened);
            
            webSocketClient.Error += new EventHandler<SuperSocket.ClientEngine.ErrorEventArgs>(WebSocketClientError);
            
            webSocketClient.Closed += new EventHandler(WebSocketClientClosed);
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