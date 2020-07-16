using System;
using EmotivDrivers.CortexClient;

namespace EmotivDrivers.CortexClient {
    public class SessionCreator {
        private string sessionId;
        private string applicationId;
        private SessionStatus status;
        private CortexClient ctxClient;
        private string cortexToken;

        //event
        public event EventHandler<string> OnSessionCreated;
        public event EventHandler<string> OnSessionClosed;

        //Constructor
        public SessionCreator() {
            this.sessionId = "";
            this.applicationId = "";
            this.cortexToken = "";

            this.ctxClient = CortexClient.Instance;

            this.ctxClient.OnCreateSession += CreateSessionOk;
            this.ctxClient.OnUpdateSession += UpdateSessionOk;
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
    }
}