using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json.Linq;

namespace EmotivDrivers.CortexClient {
    public class DataStreamExample {

        private CortexClient cortexClient;
        
        private List<string> streams;
        public List<string> Streams {
            get => streams;
            set => streams = value;
        }
        
        private string cortexToken;
        
        private string sessionId;
        public string SessionId => sessionId;

        private bool isSessionActive;

        private HeadsetFinder headsetFinder;
        private Authorizer authorizer;
        private SessionCreator sessionCreator;

        public event EventHandler<ArrayList> OnMotionDataReceived;
        public event EventHandler<ArrayList> OnEEGDataReceived;
        public event EventHandler<ArrayList> OnPerformanceDataReceived;
        public event EventHandler<ArrayList> OnBandPowerDataReceived;
        public event EventHandler<Dictionary<string, JArray>> OnSubscribed;

        public DataStreamExample() {
            authorizer = new Authorizer();
            headsetFinder = new HeadsetFinder();
            sessionCreator = new SessionCreator();
            cortexToken = "";
            sessionId = "";
            isSessionActive = false;
            
            streams = new List<string>();
            
            cortexClient = CortexClient.Instance;
            
            SubscribeToEvents();
        }

        private void SubscribeToEvents() {
            cortexClient.OnErrorMsgReceived += MessageErrorReceived;
            cortexClient.OnStreamDataReceived += StreamDataReceived;
            cortexClient.OnSubscribeData += SubscribeDataOK;
            cortexClient.OnUnSubscribeData += UnSubscribeDataOK;
            
            authorizer.OnAuthorized += AuthorizedOK;
            headsetFinder.OnHeadsetConnected += HeadsetConnectedOK;
            sessionCreator.OnSessionCreated += SessionCreatedOK;
            sessionCreator.OnSessionClosed += SessionClosedOK;
        }

        private void MessageErrorReceived(object sender, ErrorMsgEventArgs eventArgs) {
            Console.WriteLine("Message error received, code: " + eventArgs.Code + ", message: " + eventArgs.MessageError);
        }

        private void StreamDataReceived(object sender, StreamDataEventArgs eventArgs) {
            Console.WriteLine(eventArgs.StreamName + " data received.");

            ArrayList data = eventArgs.Data.ToObject<ArrayList>();
            data.Insert(0, eventArgs.Time);

            if (eventArgs.StreamName == "eeg") {
                OnEEGDataReceived(this, data);
            }
            else if (eventArgs.StreamName == "mot") {
                OnMotionDataReceived(this, data);
            }
            else if (eventArgs.StreamName == "met") {
                OnPerformanceDataReceived(this, data);
            }
            else if (eventArgs.StreamName == "pow") {
                OnBandPowerDataReceived(this, data);
            }
        }

        private void SubscribeDataOK(object sender, MultipleResultEventArgs eventArgs) {
            foreach (JObject element in eventArgs.FailList) {
                string streamName = (string) element["streamName"];
                int code = (int) element["code"];
                string errorMessage = (string) element["message"];

                Console.WriteLine("Subscribing to stream " + streamName + " was unsuccessful. Code: " + code + ", message: " + errorMessage);

                if (streams.Contains(streamName)) {
                    streams.Remove(streamName);
                }
            }
            
            Dictionary<string, JArray> header = new Dictionary<string, JArray>();

            foreach (JObject element in eventArgs.SuccessList) {
                string streamName = (string) element["streamName"];
                JArray cols = (JArray) element["cols"];
                header.Add(streamName, cols);
            }

            if (header.Count > 0) {
                OnSubscribed(this, header);
            }
            else {
                Console.WriteLine("No subscribe stream available");
            }
        }

        private void UnSubscribeDataOK(object sender, MultipleResultEventArgs eventArgs) {
            foreach (JObject element in eventArgs.SuccessList) {
                string streamName = (string) element["streamName"];

                if (streams.Contains(streamName)) {
                    streams.Remove(streamName);
                }
            }

            foreach (JObject element in eventArgs.FailList) {
                string streamName = (string) element["streamName"];
                int code = (int) element["code"];
                string errorMessage = (string) element["message"];
                
                Console.WriteLine("Unsubscription from stream: " + streamName + " unsuccessfully. Code: " + code + ", message: " + errorMessage);
            }
        }

        private void AuthorizedOK(object sender, string cortexToken) {
            if (!String.IsNullOrEmpty(cortexToken)) {
                this.cortexToken = cortexToken;
                headsetFinder.FindHeadset();
            }
        }

        private void HeadsetConnectedOK(object sender, string headsetId) {
            System.Threading.Thread.Sleep(1500);
            
            sessionCreator.Create(cortexToken, headsetId, isSessionActive);
        }

        private void SessionCreatedOK(object sender, string sessionId) {
            this.sessionId = sessionId;
            cortexClient.Subscribe(cortexToken, sessionId, Streams);
        }

        private void SessionClosedOK(object sender, string sessionId) {
            Console.WriteLine("The session " + sessionId + " has closed successfully");
        }

        public void AddStreams(string stream) {
            if (!streams.Contains(stream)) {
                streams.Add(stream);
            }
        }

        public void Start(string licenseID = "", bool activeSession = false) {
            this.isSessionActive = activeSession;
            authorizer.Start(licenseID);
        }
    }
}