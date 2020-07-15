using System;

namespace EmotivDrivers.CortexClient {
    /// <summary>
    /// Responsible for handling sessions between the cortex API and cortex client
    /// In this class you can:
    ///     Create a new session
    ///     Update a session
    ///     Close a session
    /// </summary>
    public class SessionCreator {

        private string sessionId;
        public string SessionId => sessionId;

        private string aplicationId;
        public string AplicationId => aplicationId;

        private SessionStatus status;
        public SessionStatus Status => status;

        private CortexClient cortexClient;

        private string cortexToken;
        public string CortexToken => cortexToken;

        public event EventHandler<string> OnSessionCreated;
        public event EventHandler<string> OnSessionClosed;

        public SessionCreator() {
            sessionId = "";
            aplicationId = "";
            cortexToken = "";
            
            cortexClient = CortexClient.Instance;
            
            SubscribeToEvents();
        }

        private void SubscribeToEvents() {
            cortexClient.OnCreateSession += CreateSessionOk;
            cortexClient.OnUpdateSession += UpdateSessionOk;
        }

        private void CreateSessionOk(object sender, SessionEventArgs eventArgs) {
            Console.WriteLine("Session " + eventArgs.SessionId + " is created successfully.");
            sessionId = eventArgs.SessionId;
            status = eventArgs.Status;
            aplicationId = eventArgs.ApplicationId;
            OnSessionCreated(this, sessionId);
        }

        private void UpdateSessionOk(object sender, SessionEventArgs eventArgs) {
            status = eventArgs.Status;
            if (status == SessionStatus.Closed) {
                OnSessionClosed(this, eventArgs.SessionId);
                sessionId = "";
                cortexToken = "";
            }
            else if (status == SessionStatus.Activated) {
                sessionId = eventArgs.SessionId;
                OnSessionCreated(this, sessionId);
            }
        }

        public void Create(string cortexToken, string headsetId, bool activeSession = false) {
            if (!String.IsNullOrEmpty(cortexToken) && !String.IsNullOrEmpty(headsetId)) {
                this.cortexToken = cortexToken;
                string status = activeSession ? "active" : "open";
                cortexClient.CreateSession(CortexToken, headsetId, status);
            }
        }

        public void CloseSession() {
            if (!String.IsNullOrEmpty(SessionId)) {
                cortexClient.UpdateSession(cortexToken, sessionId, "close");
            }
        }
    }
}