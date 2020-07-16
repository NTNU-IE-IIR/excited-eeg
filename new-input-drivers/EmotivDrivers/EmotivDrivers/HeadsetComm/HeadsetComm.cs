using System;
using System.Threading;
using System.Collections;
using Newtonsoft.Json.Linq;
using System.Collections.Generic;
using EmotivDrivers.CortexClient;

namespace EmotivDrivers.HeadsetComm {
    class HeadsetComm {

        static void Main(string[] args) {
            
            DataStream dataStream = new DataStream();
            dataStream.AddStreams("com");                          // You can add more streams to subscribe multiple streams
            dataStream.OnSubscribed += SubscribedOK;
            dataStream.OnComDataReceived += ComDataReceived;

            // Need a valid license key and activeSession when subscribe eeg data
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
            Console.WriteLine(command);
            Console.WriteLine(power);

            switch (command) {
                case "left":
                    Console.WriteLine("Left");
                    break;
                
                case "right":
                    Console.WriteLine("Right");
                    break;
                
                case "push":

                    break;
                
                case "pull":

                    break;
            }
        }
    }
}