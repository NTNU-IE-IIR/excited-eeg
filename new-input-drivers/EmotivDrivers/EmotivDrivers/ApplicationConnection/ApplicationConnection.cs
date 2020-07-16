using System;
using System.Xml.Serialization;
using WebSocketSharp;

namespace EmotivDrivers {
    public class ApplicationConnection {
        
        private static string keyboardServerURL = "ws://localhost:43879/input";

        static ApplicationConnection() {}

        private ApplicationConnection() {}
        
        public static ApplicationConnection Instance { get; } = new ApplicationConnection();
        

        public void SendMessageToKeyboardServer(string message) {
            using (var webSocket = new WebSocket(keyboardServerURL)) {
                webSocket.OnMessage += (sender, e) => Console.WriteLine("Keyboard server says: " + e.Data);
                
                webSocket.Connect();
                webSocket.Send(message);
                webSocket.Close();
            }
        }
    }
}