using System;
using System.Threading;
using WebSocket4Net;
using Newtonsoft.Json.Linq;
using System.Collections.Generic;

namespace EmotivDrivers.CortexClient {
    
    public enum SessionStatus {
        Opened = 0,
        Activated = 1,
        Closed = 2
    }
    
    // Event for subscribe  and unsubscribe
    public class MultipleResultEventArgs {
        public MultipleResultEventArgs(JArray successList, JArray failList) {
            SuccessList = successList;
            FailList = failList;
        }
        public JArray SuccessList { get; set; }
        public JArray FailList { get; set; }
    }

    // Event for createSession and updateSession
    public class SessionEventArgs {
        public SessionEventArgs(string sessionId, string status, string appId) {
            SessionId = sessionId;
            ApplicationId = appId;
            if (status == "opened")
                Status = SessionStatus.Opened;
            else if (status == "activated")
                Status = SessionStatus.Activated;
            else
                Status = SessionStatus.Closed;
        }
        public string SessionId { get; set; }
        public SessionStatus Status { get; set; }
        public string ApplicationId { get; set; }
    }
    
    public class StreamDataEventArgs {
        public StreamDataEventArgs(string sid, JArray data, double time, string streamName) {
            Sid = sid;
            Time = time;
            Data = data;
            StreamName = streamName;
        }
        public string Sid { get; private set; } // subscription id
        public double Time { get; private set; }
        public JArray Data { get; private set; }
        public string StreamName { get; private set; }
    }
    
    public class ErrorMsgEventArgs {
        public ErrorMsgEventArgs(int code, string messageError) {
            Code = code;
            MessageError = messageError;
        }
        public int Code { get; set; }
        public string MessageError { get; set; }
    }
    
    /// <summary>
    /// Handles all the communication between the users own application and the Cortex API
    /// The cortex client communicates by sending and receiving JSON objects.
    /// The connection is made by using web secure sockets (wss). The cortex API
    /// does not support un-secured web sockets.
    /// </summary>
    public sealed class CortexClient {
        
        /// <summary>
        /// --------------------------- VARIABLES ---------------------------
        /// </summary>
        private const string CortexURL = "wss://localhost:6868";

        private WebSocket webSocketClient;
        
        private int nextRequestId;
        private string currentMessage = string.Empty;
        private Dictionary<int, string> methodForRequestID;
        private bool isWebSocketClientConnected;
        
         /// <summary>
         /// --------------------------- EVENTS ---------------------------
         /// </summary>
        private AutoResetEvent MessageReceivedEvent = new AutoResetEvent(false);
        private AutoResetEvent OpenedEvent = new AutoResetEvent(false);
        private AutoResetEvent CloseEvent = new AutoResetEvent(false);

        public event EventHandler<bool> OnConnected;
        public event EventHandler<ErrorMsgEventArgs> OnErrorMsgReceived;
        public event EventHandler<StreamDataEventArgs> OnStreamDataReceived;
        public event EventHandler<List<Headset>> OnQueryHeadset;
        public event EventHandler<string> OnHeadsetConnected;
        public event EventHandler<bool> OnHeadsetDisConnected;
        public event EventHandler<bool> OnHasAccessRight;
        public event EventHandler<bool> OnRequestAccessDone;
        public event EventHandler<bool> OnAccessRightGranted;
        public event EventHandler<string> OnAuthorize;
        public event EventHandler<string> OnGetUserLogin;
        public event EventHandler<bool> OnEULAAccepted;
        public event EventHandler<string> OnUserLogin;
        public event EventHandler<string> OnUserLogout;
        public event EventHandler<SessionEventArgs> OnCreateSession;
        public event EventHandler<SessionEventArgs> OnUpdateSession;
        public event EventHandler<MultipleResultEventArgs> OnSubscribeData;
        public event EventHandler<MultipleResultEventArgs> OnUnSubscribeData;
        public event EventHandler<Record> OnCreateRecord;
        public event EventHandler<Record> OnStopRecord;
        public event EventHandler<Record> OnUpdateRecord;
        public event EventHandler<List<Record>> OnQueryRecords;
        public event EventHandler<MultipleResultEventArgs> OnDeleteRecords;
        public event EventHandler<JObject> OnInjectMarker;
        public event EventHandler<JObject> OnUpdateMarker;
        public event EventHandler<JObject> OnGetDetectionInfo;
        public event EventHandler<string> OnGetCurrentProfile;
        public event EventHandler<string> OnCreateProfile;
        public event EventHandler<string> OnLoadProfile;
        public event EventHandler<string> OnSaveProfile;
        public event EventHandler<bool> OnUnloadProfile;
        public event EventHandler<string> OnDeleteProfile;
        public event EventHandler<string> OnRenameProfile;
        public event EventHandler<JArray> OnQueryProfile;
        public event EventHandler<double> OnGetTrainingTime;
        public event EventHandler<JObject> OnTraining;
        
        /// <summary>
        /// --------------------------- CONSTRUCTORS ---------------------------
        /// </summary>
        static CortexClient() {}

        private CortexClient() {
            nextRequestId = 1;
            webSocketClient = new WebSocket(CortexURL);
            methodForRequestID = new Dictionary<int, string>();
            
            SubscribeToEvents();
        }
        
        /// <summary>
        /// Makes sure that classes are connected to the right instance of the cortex client
        /// </summary>
        public static CortexClient Instance { get; } = new CortexClient();
        
        private void SubscribeToEvents() {
            webSocketClient.Opened += new EventHandler(WebSocketClientOpened);
            webSocketClient.Error += new EventHandler<SuperSocket.ClientEngine.ErrorEventArgs>(WebSocketClientError);
            webSocketClient.Closed += new EventHandler(WebSocketClientClosed);
            webSocketClient.MessageReceived += new EventHandler<MessageReceivedEventArgs>(WebSocketClientMessageReceived);
        }
        
        /// <summary>
        /// Sends an JSON request message to the cortex server
        /// </summary>
        /// <param name="param">Different parameters if needed in the request message</param>
        /// <param name="method">The method requested by the cortex server</param>
        /// <param name="hasParam">If the request message contains any parameters</param>
        private void SendWebSocketMessage(JObject param, string method, bool hasParam) {
            JObject request = new JObject(
                new JProperty("jsonrpc", "2.0"), 
                new JProperty("id", nextRequestId), 
                new JProperty("method", method));

            if (hasParam) {
                request.Add("params", param);
            }
            
            Console.WriteLine("Send " + method);
            
            webSocketClient.Send(request.ToString());
            methodForRequestID.Add(nextRequestId, method);
            nextRequestId++;
        }
        
        /// <summary>
        /// This function is fired when the web socket client receives an message
        /// </summary>
        /// <param name="sender">The object calling</param>
        /// <param name="eventArgs">The event args containing the message data</param>
        private void WebSocketClientMessageReceived(object sender, MessageReceivedEventArgs eventArgs) {
            currentMessage = eventArgs.Message;
            MessageReceivedEvent.Set();

            JObject response = JObject.Parse(eventArgs.Message);

            if (response["id"] != null) {
                int id = (int) response["id"];
                string method = methodForRequestID[id];
                methodForRequestID.Remove(id);

                if (response["error"] != null) {
                    JObject error = (JObject) response["error"];
                    int errorCode = (int) error["code"];
                    string messageError = (string) error["message"];
                    Console.WriteLine("Received: " + messageError);
                    OnErrorMsgReceived(this, new ErrorMsgEventArgs(errorCode, messageError));
                }
                else {
                    JToken data = response["result"];
                    HandleResponse(method, data);
                }
            }
            else if (response["sid"] != null) {
                string sid = (string) response["sid"];
                double time = 0;

                if (response["time"] != null) {
                    time = (double) response["time"];
                }

                foreach (JProperty property in response.Properties()) {
                    if (property.Name != "sid" && property.Name != "time") {
                        OnStreamDataReceived(this, new StreamDataEventArgs(sid, (JArray) property.Value, time, property.Name));
                    }
                }
            }
            else if (response["warning"] != null) {
                JObject warning = (JObject) response["warning"];
                string messageWarning = "";
                int warningCode = -1;

                if (warning["code"] != null) {
                    warningCode = (int) warning["code"];
                }
                if (warning["message"].Type == JTokenType.String) {
                    messageWarning = warning["message"].ToString();
                }
                else if (warning["message"].Type == JTokenType.Object) {
                    Console.WriteLine("Received Warning Object");
                }
                HandleWarning(warningCode, messageWarning);
            }
        }
        
        // handle Response
        private void HandleResponse(string method, JToken data) {
            Console.WriteLine("handleResponse: " + method);
            if (method == "queryHeadsets") {
                List<Headset> headsetLists = new List<Headset>();
                foreach (JObject item in data) {
                    headsetLists.Add(new Headset(item));
                }
                OnQueryHeadset(this, headsetLists);

            }
            else if (method == "controlDevice") {
                string command = (string)data["command"];
                if (command == "connect") {
                    string message = (string)data["message"];
                    string headsetId = "";

                    Console.WriteLine("ConnectHeadset " + message);
                    if (message.Contains("Start connecting to device")) {
                        //"Start connecting to device " + headsetId
                        headsetId = message.Substring(27);
                    }
                    else if (message.Contains("The device")) {
                        //"The device " + headsetId + " has been connected or is connecting";
                        string tmp = message.Replace(" has been connected or is connecting", "");
                        headsetId = tmp.Substring(11);
                    }
                    OnHeadsetConnected(this, headsetId);
                }
                else if (command == "disconnect") {
                    OnHeadsetDisConnected(this, true);
                }
            }
            else if (method == "getUserLogin") {
                JArray users = (JArray)data;
                string username = "";
                if (users.Count > 0) {
                    foreach (JObject user in users) {
                        if (user["currentOSUId"].ToString() == user["loggedInOSUId"].ToString()) {
                            username = user["username"].ToString();
                        }
                    }
                }
                OnGetUserLogin(this, username);
            }
            else if (method == "hasAccessRight") {
                bool hasAccessRight = (bool)data["accessGranted"];
                OnHasAccessRight(this, hasAccessRight);
            }
            else if (method == "requestAccess") {
                bool hasAccessRight = (bool)data["accessGranted"];
                OnRequestAccessDone(this, hasAccessRight);
            }
            else if (method == "authorize") {
                string token = (string)data["cortexToken"];
                bool eulaAccepted = true;
                if (data["warning"] != null) {
                    JObject warning = (JObject)data["warning"];
                    eulaAccepted = !((int)warning["code"] == WarningCode.UserNotAcceptLicense);
                    token = "";
                }
                OnAuthorize(this, token);
            }
            else if (method == "createSession") {
                string sessionId = (string)data["id"];
                string status = (string)data["status"];
                string appId = (string)data["appId"];
                OnCreateSession(this, new SessionEventArgs(sessionId, status, appId));
            }
            else if (method == "updateSession") {
                string sessionId = (string)data["id"];
                string status = (string)data["status"];
                string appId = (string)data["appId"];
                OnUpdateSession(this, new SessionEventArgs(sessionId, status, appId));
            }
            else if (method == "createRecord") {
                Record record = new Record((JObject)data["record"]);
                OnCreateRecord(this, record);
            }
            else if (method == "stopRecord") {
                Record record = new Record((JObject)data["record"]);
                OnStopRecord(this, record);
            }
            else if (method == "updateRecord") {
                Record record = new Record((JObject)data);
                OnUpdateRecord(this, record);
            }
            else if (method == "queryRecords") {
                int count = (int)data["count"];
                JArray records = (JArray)data["records"];
                List<Record> recordLists = new List<Record>();
                foreach(JObject ele in records) {
                    recordLists.Add(new Record(ele));
                }
                OnQueryRecords(this, recordLists);
            }
            else if (method == "deleteRecord") {
                JArray successList = (JArray)data["success"];
                JArray failList = (JArray)data["failure"];
                OnDeleteRecords(this, new MultipleResultEventArgs(successList, failList));
            }
            else if (method == "unsubscribe") {
                JArray successList = (JArray)data["success"];
                JArray failList = (JArray)data["failure"];
                OnUnSubscribeData(this, new MultipleResultEventArgs(successList, failList));
            }
            else if (method == "subscribe") {
                JArray successList = (JArray)data["success"];
                JArray failList = (JArray)data["failure"];
                OnSubscribeData(this, new MultipleResultEventArgs(successList, failList));

            }
            else if (method == "injectMarker") {
                JObject marker = (JObject)data["marker"];
                OnInjectMarker(this, marker);
            }
            else if (method == "updateMarker") {
                JObject marker = (JObject)data["marker"];
                OnUpdateMarker(this, marker);
            }
            else if (method == "getDetectionInfo") {
                OnGetDetectionInfo(this, (JObject)data);
            }
            else if (method == "getCurrentProfile") {
                if (data["name"] == null)
                    OnGetCurrentProfile(this, "");
                else
                    OnGetCurrentProfile(this, (string)data["name"]);
            }
            else if (method == "setupProfile") {
                string action = (string)data["action"];
                string profileName = (string)data["name"];
                if (action == "create") {
                    OnCreateProfile(this, profileName);
                }
                else if (action == "load") {
                    OnLoadProfile(this, profileName);
                }
                else if (action == "save") {
                    OnSaveProfile(this, profileName);
                }
                else if (action == "unload") {
                    OnUnloadProfile(this, true);
                }
                else if (action == "rename") {
                    OnRenameProfile(this, profileName);
                }
                else if (action == "delete") {
                    OnDeleteProfile(this, profileName);
                }
            }
            else if (method == "queryProfile") {
                OnQueryProfile(this, (JArray)data);
            }
            else if (method == "training") {
                OnTraining(this, (JObject)data);
            }
            else if (method == "getTrainingTime") {
                OnGetTrainingTime(this, (double)data["time"]);
            }

        }

        // handle warning response
        private void HandleWarning(int code, string message) {
            Console.WriteLine("handleWarning: " + code + " message: " + message);
            if (code == WarningCode.AccessRightGranted) {
                // granted access right
                OnAccessRightGranted(this, true);
            }
            else if (code == WarningCode.AccessRightRejected) {
                OnAccessRightGranted(this, false);
            }
            else if (code == WarningCode.EULAAccepted) {
                OnEULAAccepted(this, true);
            }
            else if (code == WarningCode.UserLogin) {
                OnUserLogin(this, message);
            }
            else if (code == WarningCode.UserLogout) {
                OnUserLogout(this, message);
            }
        }
        
        // controlDevice
        // required params: command
        // command = {"connect", "disconnect", "refresh"}
        // mappings is required if connect to epoc flex
        public void ControlDevice(string command, string headsetId, JObject mappings) {
            JObject param = new JObject();
            param.Add("command", command);
            if (!String.IsNullOrEmpty(headsetId)) {
                param.Add("headset", headsetId);
            }
            if (mappings.Count > 0) {
                param.Add("mappings", mappings);
            }
            SendWebSocketMessage(param, "controlDevice", true);
        }
        
        // Subscribe Data
        // Required params: session, cortexToken, streams
        public void Subscribe(string cortexToken, string sessionId, List<string> streams) {
            JObject param = new JObject();
            param.Add("session", sessionId);
            param.Add("cortexToken", cortexToken);
            param.Add("streams", JToken.FromObject(streams));
            SendWebSocketMessage(param, "subscribe", true);
        }

        // UnSubscribe Data
        // Required params: session, cortexToken, streams
        public void UnSubscribe(string cortexToken, string sessionId, List<string> streams) {
            JObject param = new JObject();
            param.Add("session", sessionId);
            param.Add("cortexToken", cortexToken);
            param.Add("streams", JToken.FromObject(streams));
            SendWebSocketMessage(param, "unsubscribe", true);
        }
        
        // CreateSession
        // Required params: cortexToken, status
        public void CreateSession(string cortexToken, string headsetId, string status) {
            JObject param = new JObject();
            if (!String.IsNullOrEmpty(headsetId)) {
                param.Add("headset", headsetId);
            }
            param.Add("cortexToken", cortexToken);
            param.Add("status", status);
            SendWebSocketMessage(param, "createSession", true);
        }

        // UpdateSession
        // Required params: session, status, cortexToken
        public void UpdateSession(string cortexToken, string sessionId, string status) {
            JObject param = new JObject();
            param.Add("session", sessionId);
            param.Add("cortexToken", cortexToken);
            param.Add("status", status);
            SendWebSocketMessage(param, "updateSession", true);
        }
        
        // QueryHeadset
        public void QueryHeadsets(string headsetId) {
            JObject param = new JObject();
            if (!String.IsNullOrEmpty(headsetId)) {
                param.Add("id", headsetId);
            }
            SendWebSocketMessage(param, "queryHeadsets", false);
        }
        
        // Training - Profile
        // getDetectionInfo
        // Required params: detection
        public void GetDetectionInfo(string detection) {
            JObject param = new JObject();
            param.Add("detection", detection);
            SendWebSocketMessage(param, "getDetectionInfo", true);
        }
        
        // getCurrentProfile
        // Required params: cortexToken, headset
        public void GetCurrentProfile(string cortexToken, string headsetId) {
            JObject param = new JObject();
            param.Add("cortexToken", cortexToken);
            param.Add("headset", headsetId);
            SendWebSocketMessage(param, "getCurrentProfile", true);
        }
        
        // setupProfile
        // Required params: cortexToken, profile, status
        public void SetupProfile(string cortexToken, string profile, string status, string headsetId = null, string newProfileName = null)
        {
            JObject param = new JObject();
            param.Add("profile", profile);
            param.Add("cortexToken", cortexToken);
            param.Add("status", status);
            if (headsetId != null) {
                param.Add("headset", headsetId);
            }
            if (newProfileName != null) {
                param.Add("newProfileName", newProfileName);
            }
            SendWebSocketMessage(param, "setupProfile", true);
        }
        
        // queryProfile
        // Required params: cortexToken
        public void QueryProfile(string cortexToken) {
            JObject param = new JObject();
            param.Add("cortexToken", cortexToken);
            SendWebSocketMessage(param, "queryProfile", true);
        }
        
        
        // websocket methods
        private void WebSocketClientClosed(object sender, EventArgs eventArgs) {
            this.CloseEvent.Set();
        }

        private void WebSocketClientOpened(object sender, EventArgs eventArgs) {
            this.OpenedEvent.Set();
        }

        private void WebSocketClientError(object sender, SuperSocket.ClientEngine.ErrorEventArgs eventArgs) {
            Console.WriteLine(eventArgs.Exception.GetType() + ":" + eventArgs.Exception.Message + Environment.NewLine + eventArgs.Exception.StackTrace);

            if (eventArgs.Exception.InnerException != null) {
                Console.WriteLine(eventArgs.Exception.InnerException.GetType());
            }
        }
        
        public void Open() {
            webSocketClient.Open();

            if (OpenedEvent.WaitOne(10000)) {
                Console.WriteLine("Failed to Opened session on time");
            }    
            if (webSocketClient.State == WebSocketState.Open) {
                isWebSocketClientConnected = true;
                OnConnected(this, true);
            }
            else {
                isWebSocketClientConnected = false;
                OnConnected(this, false);
            }
        }

        public void GetUserLogin() {
            JObject param = new JObject();
            SendWebSocketMessage(param, "getUserLogin", false);
        }

        public void HasAccessRights() {
            JObject param = new JObject(
                new JProperty("clientId", Config.AppClientId), 
                new JProperty("clientSecret", Config.AppClientSecret));
            
            SendWebSocketMessage(param, "hasAccessRight", true);
        }

        public void Authorize(string licenseId, int debitNumber) {
            JObject param = new JObject();
            param.Add("clientId", Config.AppClientId);
            param.Add("clientSecret", Config.AppClientSecret);

            if (!string.IsNullOrEmpty(licenseId)) {
                param.Add("license", licenseId);
            }
            
            param.Add("debit", debitNumber);
            SendWebSocketMessage(param, "authorize", true);
        }

        public void RequestAccess() {
            JObject param = new JObject(
                new JProperty("clientId", Config.AppClientId), 
                new JProperty("clientSecret", Config.AppClientSecret));
            SendWebSocketMessage(param, "requestAccess", true);
        }
    }
}