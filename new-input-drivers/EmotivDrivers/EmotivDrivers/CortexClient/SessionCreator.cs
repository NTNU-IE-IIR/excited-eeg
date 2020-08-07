using System;
using System.Collections.Generic;
using EmotivDrivers.CortexClient;
using Newtonsoft.Json.Linq;

namespace EmotivDrivers.CortexClient {
    public class SessionCreator {
        private string sessionId;
        private string applicationId;
        private SessionStatus status;
        private CortexClient ctxClient;
        private string cortexToken;
        private string profileName;
        private string headsetId;
        private List<string> profileList;

        //event
        public event EventHandler<string> OnSessionCreated;
        public event EventHandler<string> OnSessionClosed;
        public event EventHandler<string> OnProfileLoaded;
        public event EventHandler<String> OnProfileQuery;

        //Constructor
        public SessionCreator() {
            this.sessionId = "";
            this.applicationId = "";
            //this.cortexToken = "";
            //this.profileName = "Arild";
            //this.headsetId = "";
            //this.profileList = new List<string>();
            
            this.ctxClient = CortexClient.Instance;

            this.ctxClient.OnCreateSession += CreateSessionOk;
            this.ctxClient.OnUpdateSession += UpdateSessionOk;
            //this.ctxClient.OnHeadsetConnected += HeadsetConnectedOK;
            //this.ctxClient.OnQueryProfile += QueryProfileOK;
            //this.ctxClient.OnLoadProfile += ProfileLoadedOK;

            //this.headsetFinder.OnHeadsetConnected += HeadsetConnectedOK;
        }

        private void CreateSessionOk(object sender, SessionEventArgs e) {
            Console.WriteLine("Session " + e.SessionId + " is created successfully.");
            this.sessionId = e.SessionId;
            this.status = e.Status;
            this.applicationId = e.ApplicationId;
            OnSessionCreated(this, this.sessionId);
        }
        private void UpdateSessionOk(object sender, SessionEventArgs e) {
            this.status = e.Status;
            if (this.status == SessionStatus.Closed) {
                OnSessionClosed(this, e.SessionId);
                this.sessionId = "";
                this.cortexToken = "";
            }
            else if (this.status == SessionStatus.Activated) {
                this.sessionId = e.SessionId;
                OnSessionCreated(this, this.sessionId);
            }
        }

        // Property
        public string SessionId {
            get { return this.sessionId; }
        }

        public SessionStatus Status {
            get { return this.status; }
        }

        public string CortexToken {
            get { return this.cortexToken; }
        }

        public string ApplicationId {
            get { return this.applicationId; }
        }

        // Create
        public void Create(string cortexToken, string headsetId, bool activeSession = false) {
            if (!String.IsNullOrEmpty(cortexToken) &&
                !String.IsNullOrEmpty(headsetId)) {
                this.cortexToken = cortexToken;
                string status = activeSession ? "active" : "open";
                this.ctxClient.CreateSession(CortexToken, headsetId, status);
            }
            else {
                Console.WriteLine("CreateSession: Invalid parameters");
            }
            
        }

        // Close Session
        public void CloseSession() {
            if (!String.IsNullOrEmpty(SessionId)) {
                this.ctxClient.UpdateSession(this.cortexToken, this.sessionId, "close");
            }
        }
        
        // Query profile
        //private void HeadsetConnectedOK(object sender, string headsetId) {
        //    if (!String.IsNullOrEmpty(headsetId)) {
        //        this.headsetId = headsetId;
        //        ctxClient.QueryProfile(cortexToken);
        //    }
        //}
        
        //private void QueryProfileOK(object sender, JArray profiles) {
        //    Console.WriteLine("Query profile OK: " + profiles);

        //    foreach (JObject element in profiles) {
        //        string name = (string) element["name"];
        //        profileList.Add(name);
        //    }

        //    OnProfileQuery(this, profileName);
        //}
        
        // Load profile
        //public void LoadProfile(string profileName, string cortexToken, string headsetId) {
        //    this.profileName = profileName;
        //    this.cortexToken = cortexToken;
        //    this.headsetId = headsetId;
        //    if (this.profileList.Contains(profileName))
        //       this.ctxClient.SetupProfile(cortexToken, profileName, "load", this.headsetId);
        //    else
        //        Console.WriteLine("The profile can not be loaded. The name " + profileName + " has not existed.");
        //}

        //private void ProfileLoadedOK(object sender, string loadedProfile) {
        //    if (this.profileName.Equals(loadedProfile)) {
        //        Console.WriteLine("Profile " + loadedProfile + " loaded.");
        //        OnProfileLoaded(this, headsetId);
        //    }
        //}
    }
}