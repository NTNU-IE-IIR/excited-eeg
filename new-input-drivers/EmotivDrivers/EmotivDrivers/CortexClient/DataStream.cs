using Newtonsoft.Json.Linq;
using System;
using System.Collections;
using System.Collections.Generic;

namespace EmotivDrivers.CortexClient {
    
    /// <summary>
    /// Used to subscribe to a data stream from a emotiv device.
    /// </summary>
    public class DataStream {
        
        /// <summary>
        /// --------------------------- VARIABLES ---------------------------
        /// </summary>
        private CortexClient ctxClient;
        private List<string> streams;
        private string cortexToken;
        private string sessionId;
        private bool isActiveSession;
        private string profileName;

        private string headsetId;

        private HeadsetFinder headsetFinder;
        private Authorizer authorizer;
        private SessionCreator sessionCreator;
        private ProfileHandler profileHandler;

        public List<string> Streams {
            get { return this.streams; }
            set { this.streams = value; }
        }

        public string SessionId {
            get { return this.sessionId; }
        }

        /// <summary>
        /// --------------------------- EVENTS ---------------------------
        /// </summary>
        public event EventHandler<ArrayList> OnComDataReceived; // command word data
        public event EventHandler<ArrayList> OnMotionDataReceived; // motion data
        public event EventHandler<ArrayList> OnEEGDataReceived; // eeg data
        public event EventHandler<ArrayList> OnDevDataReceived; // contact quality data
        public event EventHandler<ArrayList> OnPerfDataReceived; // performance metric
        public event EventHandler<ArrayList> OnBandPowerDataReceived; // band power
        public event EventHandler<Dictionary<string, JArray>> OnSubscribed;

        /// <summary>
        /// --------------------------- CONSTRUCTORS ---------------------------
        /// </summary>
        public DataStream() {

            authorizer = new Authorizer();
            headsetFinder = new HeadsetFinder();
            sessionCreator = new SessionCreator();
            profileHandler = new ProfileHandler();
            cortexToken = "";
            sessionId = "";
            isActiveSession = false;
            this.profileName = "Arild";

            streams = new List<string>();
            // Event register
            ctxClient = CortexClient.Instance;
            
            SubscribeToEvents();
        }

        /// <summary>
        /// --------------------------- METHODS ---------------------------
        /// </summary>
        private void SubscribeToEvents() {
            this.ctxClient.OnErrorMsgReceived += MessageErrorRecieved;
            this.ctxClient.OnStreamDataReceived += StreamDataReceived;
            this.ctxClient.OnSubscribeData += SubscribeDataOK;
            this.ctxClient.OnUnSubscribeData += UnSubscribeDataOK;

            this.authorizer.OnAuthorized += AuthorizedOK;
            this.headsetFinder.OnHeadsetConnected += HeadsetConnectedOK;
            this.sessionCreator.OnSessionCreated += SessionCreatedOk;
            this.sessionCreator.OnSessionClosed += SessionClosedOK;
            this.profileHandler.OnProfileQuery += ProfileQueryOK;
            this.profileHandler.OnProfileLoaded += ProfileLoadedOK;
        }
        
        private void SessionClosedOK(object sender, string sessionId) {
            Console.WriteLine("The Session " + sessionId + " has closed successfully.");
        }

        private void UnSubscribeDataOK(object sender, MultipleResultEventArgs e) {
            foreach (JObject ele in e.SuccessList) {
                string streamName = (string)ele["streamName"];
                if (this.streams.Contains(streamName)) {
                    this.streams.Remove(streamName);
                }
            }
            foreach (JObject ele in e.FailList) {
                string streamName = (string)ele["streamName"];
                int code = (int)ele["code"];
                string errorMessage = (string)ele["message"];
                Console.WriteLine("UnSubscribe stream " + streamName + " unsuccessfully." + " code: " + code + " message: " + errorMessage);
            }
        }

        private void SubscribeDataOK(object sender, MultipleResultEventArgs e) {
            foreach (JObject ele in e.FailList) {
                string streamName = (string)ele["streamName"];
                int code = (int)ele["code"];
                string errorMessage = (string)ele["message"];
                Console.WriteLine("Subscribe stream " + streamName + " unsuccessfully." + " code: " + code + " message: " + errorMessage);
                if (this.streams.Contains(streamName)) {
                    this.streams.Remove(streamName);
                }
            }
            Dictionary<string, JArray> header = new Dictionary<string, JArray>();
            foreach (JObject ele in e.SuccessList) {
                string streamName = (string)ele["streamName"];
                JArray cols = (JArray)ele["cols"];
                header.Add(streamName, cols);
            }
            if (header.Count > 0) {
                OnSubscribed(this, header);
            }
            else {
                Console.WriteLine("No Subscribe Stream Available");
            }
        }

        private void SessionCreatedOk(object sender, string sessionId) {
            // subscribe
            this.sessionId = sessionId;
            this.ctxClient.Subscribe(this.cortexToken, this.sessionId, Streams);
        }

        private void HeadsetConnectedOK(object sender, string headsetId) {
            Console.WriteLine("HeadsetConnectedOK " + headsetId);
            this.headsetId = headsetId;
            this.ctxClient.QueryProfile(this.cortexToken);
        }
        
        private void ProfileQueryOK(object sender, List<string> profileList) {
            this.profileHandler.LoadProfile(this.profileName, this.cortexToken, this.headsetId);
        }
        
        private void ProfileLoadedOK(object sender, string loadedProfile) {
            this.sessionCreator.Create(this.cortexToken, headsetId, this.isActiveSession);
        }

        private void AuthorizedOK(object sender, string cortexToken) {
            if (!String.IsNullOrEmpty(cortexToken)) {
                this.cortexToken = cortexToken;
                // find headset
                this.headsetFinder.FindHeadset();
            }
        }

        private void StreamDataReceived(object sender, StreamDataEventArgs e) {
            Console.WriteLine(e.StreamName + " data received.");
            ArrayList data = e.Data.ToObject<ArrayList>();
            // insert timestamp to datastream
            data.Insert(0, e.Time);
            if (e.StreamName == "com") {
                OnComDataReceived(this, data);
            }
            else if (e.StreamName == "eeg") {
                OnEEGDataReceived(this, data); 
            }
            else if (e.StreamName == "mot") {
                OnMotionDataReceived(this, data);
            }
            else if (e.StreamName == "met") {
                OnPerfDataReceived(this, data);
            }
            else if (e.StreamName == "pow") {
                OnBandPowerDataReceived(this, data);
            }
        }
        
        private void MessageErrorRecieved(object sender, ErrorMsgEventArgs e) {
            Console.WriteLine("MessageErrorRecieved :code " + e.Code + " message " + e.MessageError);
        }

        /// <summary>
        /// Adds a new stream to our list of subscribed streams
        /// </summary>
        /// <param name="stream">Name of the stream we want to add</param>
        public void AddStreams(string stream) {
            if (!this.streams.Contains(stream)) {
                this.streams.Add(stream);
            }
        }
        
        /// <summary>
        /// Starts a new data stream session
        /// </summary>
        /// <param name="licenseID"></param>
        /// <param name="activeSession">If a session is active or not</param>
        public void Start(string licenseID="", bool activeSession = false) {
            this.isActiveSession = activeSession;
            this.authorizer.Start(licenseID);
        }

        /// <summary>
        /// Unsubscribe from all data streams
        /// </summary>
        /// <param name="streams">The list of streams we want to unsubscribe from</param>
        public void UnSubscribe(List<string> streams = null) {
            if (streams == null) {
                // unsubscribe all data
                this.ctxClient.UnSubscribe(this.cortexToken, this.sessionId, this.streams);
            }
            else 
                this.ctxClient.UnSubscribe(this.cortexToken, this.sessionId, streams);
        }
        
        /// <summary>
        /// Close current data streams session
        /// </summary>
        public void CloseSession() {
            this.sessionCreator.CloseSession();
        }
    }
}