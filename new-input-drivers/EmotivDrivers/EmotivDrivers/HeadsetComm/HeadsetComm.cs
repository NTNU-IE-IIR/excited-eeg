using System;
using System.Threading;
using System.Collections;
using Newtonsoft.Json.Linq;
using System.Collections.Generic;
using System.Diagnostics;
using System.Windows.Forms;
using EmotivDrivers.CortexClient;
using EmotivDrivers.GUI;

namespace EmotivDrivers.HeadsetComm {
    public class HeadsetComm {

        private static float[] previousTriggerTime = {0,0,0,0,0};
        private static float currentTimeStamp = 0;
        private static int neutral = 0;
        private static int left = 1;
        private static int right = 2;
        private static int push = 3;
        private static int pull = 4;
        private static float commandInterval = 1.5f;
        private static float triggerThreshold = 0.30f;

        public HeadsetComm() {
        }
        
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
            float power = float.Parse(comData[2].ToString());

            if (power >= triggerThreshold) {

                currentTimeStamp = (float) (DateTime.Now - DateTime.MinValue).TotalMilliseconds;

                switch (command)
                {
                    case "neutral":
                        if (currentTimeStamp - previousTriggerTime[neutral] >= commandInterval)
                        {
                            previousTriggerTime[neutral] = currentTimeStamp;
                            Console.WriteLine("Sending command: Neutral.");
                        }

                        break;

                    case "left":
                        if (currentTimeStamp - previousTriggerTime[left] >= commandInterval) {
                            previousTriggerTime[left] = currentTimeStamp;
                            Console.WriteLine("Sending command: Left.");
                        }
                        break;

                    case "right":
                        if (currentTimeStamp - previousTriggerTime[right] >= commandInterval) {
                            previousTriggerTime[right] = currentTimeStamp;
                            Console.WriteLine("Sending command: Right.");
                        }
                        break;

                    case "push":
                        if (currentTimeStamp - previousTriggerTime[push] >= commandInterval) {
                            previousTriggerTime[push] = currentTimeStamp;
                            Console.WriteLine("Sending command: Push.");
                        }
                        break;

                    case "pull":
                        if (currentTimeStamp - previousTriggerTime[pull] >= commandInterval) {
                            previousTriggerTime[pull] = currentTimeStamp;
                            Console.WriteLine("Sending command: Pull.");
                        }
                        break;
                }
            }
        }
    }
}