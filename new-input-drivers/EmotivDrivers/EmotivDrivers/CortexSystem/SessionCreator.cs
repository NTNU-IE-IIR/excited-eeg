using System;
using System.Collections.Generic;
using Newtonsoft.Json.Linq;

namespace EmotivDrivers.CortexSystem {
    
    /// <summary>
    /// Used to create and handle sessions between the cortex client and the cortex API
    /// </summary>
    public class SessionCreator {
        
        /// <summary>
        /// --------------------------- VARIABLES ---------------------------
        /// </summary>
        private string sessionId;
        private string applicationId;
        private SessionStatus status;
        private CortexClient ctxClient;
        private string cortexToken;
        
        /// <summary>
        /// --------------------------- PROPERTIES ---------------------------
        /// </summary>
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

        /// <summary>
        /// --------------------------- EVENTS ---------------------------
        /// </summary>
        public event EventHandler<string> OnSessionCreated;
        public event EventHandler<string> OnSessionClosed;

        /// <summary>
        /// --------------------------- CONSTRUCTORS ---------------------------
        /// </summary>
        public SessionCreator() {
            this.sessionId = "";
            this.applicationId = "";

            this.ctxClient = CortexClient.Instance;

            this.ctxClient.OnCreateSession += CreateSessionOk;
            this.ctxClient.OnUpdateSession += UpdateSessionOk;
        }
        
        /// <summary>
        /// --------------------------- METHODS ---------------------------
        /// </summary>
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
        
        /// <summary>
        /// Create a new session between the cortex client and cortex API
        /// </summary>
        /// <param name="cortexToken">The session token</param>
        /// <param name="headsetId">The Id of the headset in the session</param>
        /// <param name="activeSession">If there is a active session</param>
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

        /// <summary>
        /// Closes a open session
        /// </summary>
        public void CloseSession() {
            if (!String.IsNullOrEmpty(SessionId)) {
                this.ctxClient.UpdateSession(this.cortexToken, this.sessionId, "close");
            }
        }
    }
}