using System;
using System.Collections.Generic;
using System.Linq;
using System.Timers;
using Newtonsoft.Json.Linq;

namespace EmotivDrivers.CortexClient {
    public class HeadsetFinder {

        private CortexClient cortexClient;
        private string headsetId;
        public string HeadsetId => headsetId;

        private Timer timer;

        private bool isHeadsetFound;

        public event EventHandler<string> OnHeadsetConnected;
        public event EventHandler<bool> OnHeadsetDisConnected;

        public HeadsetFinder() {
            cortexClient = CortexClient.Instance;
            headsetId = "";
            isHeadsetFound = false;
            
            SubscribeToEvents();
        }

        private void SubscribeToEvents() {
            cortexClient.OnQueryHeadset += QueryHeadsetOK;
            cortexClient.OnHeadsetConnected += HeadsetConnectedOK;
            cortexClient.OnHeadsetDisConnected += HeadsetDisconnectedOK;
        }

        private void HeadsetDisconnectedOK(object sender, bool e) {
            headsetId = "";
            OnHeadsetDisConnected(this, true);
        }

        private void HeadsetConnectedOK(object sender, string headsetId) {
            if (!String.IsNullOrEmpty(headsetId)) {
                this.headsetId = headsetId;
                OnHeadsetConnected(this, this.headsetId);
            }
        }

        private void QueryHeadsetOK(object sender, List<Headset> headsets) {
            if (headsets.Count > 0) {
                isHeadsetFound = true;
                
                timer.Stop();
                timer.Dispose();

                Headset headset = headsets.First<Headset>();

                if (headset.Status == "discovered") {
                    JObject flexMappings = new JObject();

                    if (headset.HeadsetId.IndexOf("Flex", StringComparison.OrdinalIgnoreCase) > 0) {
                        flexMappings = JObject.Parse(Config.FlexMapping);
                    }

                    cortexClient.ControlDevice("connect", headset.HeadsetId, flexMappings);
                }
                else if (headset.Status == "connected") {
                    this.headsetId = headset.HeadsetId;
                    OnHeadsetConnected(this, this.headsetId);
                }
                else if (headset.Status == "connecting") {
                    Console.WriteLine("Waiting for headset connection: " + headset.HeadsetId);
                }
            }
            else {
                isHeadsetFound = false;
                Console.WriteLine("No headsets available. Please connect headset to the machine");
            }
        }

        public void FindHeadset() {
            Console.WriteLine("Find headset");
            if (!isHeadsetFound) {
                SetTimer();
                cortexClient.QueryHeadsets("");
            }
        }

        private void SetTimer() {
            timer = new Timer(5000);

            timer.Elapsed += OnTimedEvent;
            timer.AutoReset = true;
            timer.Enabled = true;
        }

        private void OnTimedEvent(object sender, ElapsedEventArgs eventArgs) {
            if (!isHeadsetFound) {
                cortexClient.QueryHeadsets("");
            }
        }
    }
}