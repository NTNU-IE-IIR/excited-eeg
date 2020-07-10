using System;
using System.Collections.Generic;
using System.Threading;
using WebSocket4Net;
using Newtonsoft.Json.Linq;
using SuperSocket.ClientEngine;

namespace EmotivDrivers {

    public class ErrorMsgEventArgs {
        public int ErrorCode { get; set; }
        public string MessageError { get; set; }

        public ErrorMsgEventArgs(int errorCode, string messageError) {
            ErrorCode = errorCode;
            MessageError = messageError;
        }
    }

    public class StreamDataEventArgs {
        public string Sid { get; private set; }
        public double Time { get; private set; }
        public JArray Data { get; private set; }
        public string StreamName { get; private set; }

        public StreamDataEventArgs(string sid, JArray data, double time, string streamName) {
            Sid = sid;
            Time = time;
            Data = data;
            StreamName = streamName;
        }
    }
    
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

        public event EventHandler<ErrorMsgEventArgs> OnErrorMsgReceived;
        public event EventHandler<StreamDataEventArgs> OnStreamDataReceived; 
        
        /// <summary>
        /// Constructors
        /// </summary>
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

        private void WebSocketClientMessageReceived(object sender, MessageReceivedEventArgs eventArgs) {
            currentMessage = eventArgs.Message;
            MessageReceivedEvent.Set();

            JObject response = JObject.Parse(eventArgs.Message);

            if (response["id"] != null) {
                int id = (int) response["id"];
                string method = methodForRequestID[id];
                methodForRequestID.Remove(id);

                if (response["error"] != null) {
                    JObject error = (JObject) response["error"];
                    int errorCode = (int) error["code"];
                    string messageError = (string) error["message"];
                    Console.WriteLine("Received: " + messageError);
                    OnErrorMsgReceived(this, new ErrorMsgEventArgs(errorCode, messageError));
                }
                else {
                    JToken data = response["result"];
                    HandleResponse(method, data);
                }
            }
            else if (response["sid"] != null) {
                string sid = (string) response["sid"];
                double time = 0;

                if (response["time"] != null) {
                    time = (double) response["time"];
                }

                foreach (JProperty property in response.Properties()) {
                    if (property.Name != "sid" && property.Name != "time") {
                        OnStreamDataReceived(this, new StreamDataEventArgs(sid, (JArray) property.Value, time, property.Name));
                    }
                }
            }
            else if (response["warning"] != null) {
                JObject warning = (JObject) response["warning"];
                string messageWarning = "";
                int warningCode = -1;

                if (warning["code"] != null) {
                    warningCode = (int) warning["code"];
                }
                if (warning["message"].Type == JTokenType.String) {
                    messageWarning = warning["message"].ToString();
                }
                else if (warning["message"].Type == JTokenType.Object) {
                    Console.WriteLine("Received Warning Object");
                }
                HandleWarning(warningCode, messageWarning);
            }
        }

        private void HandleResponse(string method, JToken data) {
            //TODO: ARILD FYLLER UT DENNE
        }

        private void HandleWarning(int warningCode, string message) {
            //TODO ARILD FYLLER UT DENNE
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