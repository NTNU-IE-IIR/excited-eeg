using System;
using System.Collections.Generic;
using System.Linq;
using Newtonsoft.Json.Linq;
using System.Timers;

namespace EmotivDrivers.CortexClient {
    public class HeadsetFinder {
        private CortexClient ctxClient;
        private string headsetId; // headset id of connected device
        private Timer aTimer;

        private bool isFoundHeadset;

        // Event
        public event EventHandler<string> OnHeadsetConnected;
        public event EventHandler<bool> OnHeadsetDisConnected;

        public HeadsetFinder() {
            this.ctxClient = CortexClient.Instance;
            this.headsetId = "";
            this.isFoundHeadset = false;
            this.ctxClient.OnQueryHeadset += QueryHeadsetOK;
            this.ctxClient.OnHeadsetConnected += HeadsetConnectedOK;
            this.ctxClient.OnHeadsetDisConnected += HeadsetDisconnectedOK;
        }

        private void HeadsetDisconnectedOK(object sender, bool e) {
            this.headsetId = "";
            OnHeadsetDisConnected(this, true);
        }

        private void HeadsetConnectedOK(object sender, string headsetId) {
            if (!String.IsNullOrEmpty(headsetId)) {
                this.headsetId = headsetId;
                OnHeadsetConnected(this, this.headsetId);
            }
        }

        private void QueryHeadsetOK(object sender, List<Headset> headsets) {
            if ( headsets.Count > 0) {
                this.isFoundHeadset = true;
                //Turn off timer
                this.aTimer.Stop();
                this.aTimer.Dispose();

                Headset headset = headsets.First<Headset>();
                if (headset.Status == "discovered") {
                    JObject flexMappings = new JObject();
                    if (headset.HeadsetId.IndexOf("FLEX", StringComparison.OrdinalIgnoreCase) > 0) {
                        // For an Epoc Flex headset, we need a mapping
                        flexMappings = JObject.Parse(Config.FlexMapping);
                    }
                    this.ctxClient.ControlDevice("connect", headset.HeadsetId, flexMappings);
                }
                else if (headset.Status == "connected") {
                    this.headsetId = headset.HeadsetId;
                    OnHeadsetConnected(this, this.headsetId);
                }
                else if (headset.Status == "connecting") {
                    Console.WriteLine(" Waiting for headset connection " + headset.HeadsetId);
                }
            }
            else {
                this.isFoundHeadset = false;
                Console.WriteLine(" No headset available. Please connect headset to the machine");
            }
        }

        // Property
        public string HeadsetId {
            get { return this.headsetId; }
        }

        public void FindHeadset() {
            Console.WriteLine("FindHeadset");
            if (!this.isFoundHeadset) {
                SetTimer(); // set timer for query headset
                this.ctxClient.QueryHeadsets("");
            }            
        }

        // Create Timer for headset finding
        private void SetTimer() {
            // Create a timer with 5 seconds
            this.aTimer = new Timer(5000);

            // Hook up the Elapsed event for the timer. 
            this.aTimer.Elapsed += OnTimedEvent;
            this.aTimer.AutoReset = true;
            this.aTimer.Enabled = true;
        }

        private void OnTimedEvent(object sender, ElapsedEventArgs e) {
            if (!this.isFoundHeadset) {
                // Still not found headset
                // Query headset again
                this.ctxClient.QueryHeadsets("");
            }
        }
    }
}