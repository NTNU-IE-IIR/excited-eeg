﻿using System;
using System.Threading;
using System.IO;
using System.Collections;
using System.Text;
using Newtonsoft.Json.Linq;
using System.Collections.Generic;
using EmotivDrivers.CortexClient;

namespace EmotivDrivers.HeadsetComm {
    class HeadsetComm {
        const string OutFilePath = @"EEGLogger.csv";
        const string licenseID = "giK2jIkOy5x0Ry1xwixpdbAykYZi1Ebr3xjv7Asy";
        private static FileStream OutFileStream;

        static void Main(string[] args) {


            // Delete Output file if existed
            if (File.Exists(OutFilePath)) {
                File.Delete(OutFilePath);
            }
            OutFileStream = new FileStream(OutFilePath, FileMode.Append, FileAccess.Write);


            DataStream dataStream = new DataStream();
            dataStream.AddStreams("eeg");                          // You can add more streams to subscribe multiple streams
            dataStream.OnSubscribed += SubscribedOK;
            dataStream.OnEEGDataReceived += OnEEGDataReceived;

            // Need a valid license key and activeSession when subscribe eeg data
            dataStream.Start(licenseID, true);

            Console.WriteLine("Press Esc to flush data to file and exit");
            while (Console.ReadKey().Key != ConsoleKey.Escape) { }

            // Unsubcribe stream
            dataStream.UnSubscribe();
            Thread.Sleep(5000);

            // Close Session
            dataStream.CloseSession();
            Thread.Sleep(5000);
            // Close Out Stream
            OutFileStream.Dispose();
        }

        private static void SubscribedOK(object sender, Dictionary<string, JArray> e) {
            foreach (string key in e.Keys) {
                if (key == "eeg") {
                    // print header
                    ArrayList header = e[key].ToObject<ArrayList>();
                    //add timeStamp to header
                    header.Insert(0, "Timestamp");
                    WriteDataToFile(header);
                }
            }
        }

        // Write Header and Data to File
        private static void WriteDataToFile(ArrayList data) {
            int i = 0;
            for (; i < data.Count - 1; i++) {
                byte[] val = Encoding.UTF8.GetBytes(data[i].ToString() + ", ");

                if (OutFileStream != null)
                    OutFileStream.Write(val, 0, val.Length);
                else
                    break;
            }
            // Last element
            byte[] lastVal = Encoding.UTF8.GetBytes(data[i].ToString() + "\n");
            if (OutFileStream != null) {
                OutFileStream.Write(lastVal, 0, lastVal.Length);
            }
        }

        private static void OnEEGDataReceived(object sender, ArrayList eegData) {
            WriteDataToFile(eegData);
        }

    }
}