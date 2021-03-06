﻿using System;
using System.Net.Sockets;
using System.Windows.Forms;
using EmotivDrivers.GUI;
using EmotivDrivers.HeadsetComm;
using WebSocketSharp;

namespace EmotivDrivers.AppConnection {
    
    /// <summary>
    /// Class used for connecting the cortex client to the android keyboard application.
    /// The keyboard server uses TCP-port: 43879 and are connected using web sockets.
    ///
    /// THE KEYBOARD APPLICATION SERVER CURRENTLY ONLY SUPPORTS UN-SECURED WEB SOCKETS.
    /// DO NOT SEND ANY SENSITIVE DATA THROUGH THIS CONNECTION.
    /// </summary>
    public class ApplicationConnection {
        
        /// <summary>
        /// --------------------------- VARIABLES ---------------------------
        /// </summary>
        
        //You need to change the IP address to the device the keyboard application is running on
        private static string keyboardServerURL = "ws://192.168.0.47:43879/input";
        
        /// <summary>
        /// --------------------------- CONSTRUCTORS ---------------------------
        /// </summary>
        static ApplicationConnection() {
            
        }

        private ApplicationConnection() {
            SubscribeToEvents();
        }
        
        /// <summary>
        /// Makes sure that classes are connected to the right instance of the cortex client
        /// </summary>
        public static ApplicationConnection Instance { get; } = new ApplicationConnection();
        
        /// <summary>
        /// --------------------------- METHODS ---------------------------
        /// </summary>
        public void SendMessageToKeyboardServer(string message) {
            try {
                using (var webSocket = new WebSocket(keyboardServerURL)) {
                    webSocket.OnMessage += (sender, e) => Console.WriteLine("Keyboard server says: " + e.Data);

                    webSocket.Connect();
                    webSocket.Send(message);
                    webSocket.Close();
                }
            }
            catch (SocketException e) {
                Console.WriteLine("Could not connect to the EEG-keyboard");
                throw;
            }
        }

        private static void SubscribeToEvents() {
            SetIPGUI.SetIPEvent += UpdateKeyboardAddress;
        }
        
        [STAThread]
        static void Main(string[] args) {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new SetIPGUI());
        }
        
        private static void UpdateKeyboardAddress(object sender, SetIPEventArgs eventArgs) {
            keyboardServerURL = "ws://" + eventArgs.Ip + ":43879/input"; 
            Console.WriteLine("Keyboard server address set to: " + keyboardServerURL);
        }
    }
}