using System;
using System.Threading;
using System.Collections;
using Newtonsoft.Json.Linq;
using System.Collections.Generic;
using System.Windows.Forms;
using EmotivDrivers.CortexSystem;
using EmotivDrivers.GUI;

namespace EmotivDrivers.HeadsetComm {
    public class HeadsetComm {

        public HeadsetComm() { }
        
        public void StartHeadsetCommunications() {
            DataStream dataStream = new DataStream();
            dataStream.AddStreams("com");                          
            dataStream.OnSubscribed += SubscribedOK;
            dataStream.OnComDataReceived += ComDataReceived;
                    
            // Need a valid license key and activeSession when subscribe com data
            dataStream.Start("", true);
                        
            while (Console.ReadKey().Key != ConsoleKey.Escape) {}
                        
            // Unsubcribe stream
            dataStream.UnSubscribe();
            Thread.Sleep(5000);
                    
            // Close Session
            dataStream.CloseSession();
            Thread.Sleep(5000);
        }

        private static void SubscribedOK(object sender, Dictionary<string, JArray> e) {
            foreach (string key in e.Keys) {
                if (key == "com") {
                    // print header
                    ArrayList header = e[key].ToObject<ArrayList>();
                    //add timeStamp to header
                    header.Insert(0, "Timestamp");
                }
            }
        }

        private static void ComDataReceived(object sender, ArrayList comData) {
            string command = comData[1].ToString();
            string power = comData[2].ToString();

            switch (command) {
                case "neutral":
                    Console.WriteLine("Neutral");
                    break;
                
                case "left":
                    Console.WriteLine("Left");
                    break;
                
                case "right":
                    Console.WriteLine("Right");
                    break;
                
                case "push":
                    Console.WriteLine("Push");
                    break;
                
                case "pull":
                    Console.WriteLine("Pull");
                    break;
            }
        }
    }
}