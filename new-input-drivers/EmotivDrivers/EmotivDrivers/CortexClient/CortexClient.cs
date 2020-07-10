using System;
using System.Threading;
using WebSocket4Net;
using Newtonsoft.Json.Linq;
using System.Collections.Generic;

namespace EmotivDrivers {
    
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

    public class CortexClient {

        private const string CortexURL = "wss://localhost:6868";

        private WebSocket webSocketClient;
        
        private int nextRequestId;
        private string CurrentMessage = string.Empty;
        private Dictionary<int, string> idRequest;
        
         //Events
        private AutoResetEvent MessageReceiveEvent = new AutoResetEvent(false);
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

        public static void Main(string[] args) {
            CortexClient client = new CortexClient();
            
        }
        // Build a request message
        private void SendTextMessage(JObject param, string method, bool hasParam = true) {
            JObject request = new JObject(
            new JProperty("jsonrpc", "2.0"),
            new JProperty("id", nextRequestId),
            new JProperty("method", method));

            if (hasParam)
            {
                request.Add("params", param);
            }
            Console.WriteLine("Send " + method);
            //Console.WriteLine(request.ToString());

            // send the json message
            webSocketClient.Send(request.ToString());

            this.idRequest.Add(nextRequestId, method);
            nextRequestId++;
        }
        // Handle receieved message 
        private void WebSocketClientMessageReceived(object sender, MessageReceivedEventArgs e) {
            this.CurrentMessage = e.Message;
            this.MessageReceiveEvent.Set();
            //Console.WriteLine("Received: " + e.Message);

            JObject response = JObject.Parse(e.Message);

            if (response["id"] != null) {
                int id = (int)response["id"];
                string method = this.idRequest[id];
                this.idRequest.Remove(id);
                
                if (response["error"] != null) {
                    JObject error = (JObject)response["error"];
                    int code = (int)error["code"];
                    string messageError = (string)error["message"];
                    Console.WriteLine("Received: " + messageError);
                    //Send Error message event
                    OnErrorMsgReceived(this, new ErrorMsgEventArgs(code, messageError));
                }
                else {
                    // handle response
                    JToken data = response["result"];
                    HandleResponse(method, data);
                }
            }
            else if (response["sid"] != null) {
                string sid = (string)response["sid"];
                double time = 0;
                if (response["time"] != null)
                    time = (double)response["time"];

                foreach (JProperty property in response.Properties()) {
                    //Console.WriteLine(property.Name + " - " + property.Value);
                    if (property.Name != "sid" &&
                        property.Name != "time") {
                        OnStreamDataReceived(this, new StreamDataEventArgs(sid, (JArray)property.Value, time, property.Name));
                    }
                }
            }
            else if (response["warning"] != null) {
                JObject warning = (JObject)response["warning"];
                string messageWarning = "";
                int code = -1;
                if (warning["code"] != null) {
                    code = (int)warning["code"];
                }
                if (warning["message"].Type == JTokenType.String) {
                    messageWarning = warning["message"].ToString();
                }
                else if (warning["message"].Type == JTokenType.Object) {
                    Console.WriteLine("Received Warning Object");
                }
                HandleWarning(code, messageWarning);
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
        private void WebSocketClientClosed(object sender, EventArgs e) {
            this.CloseEvent.Set();
        }

        private void WebSocketClientOpened(object sender, EventArgs e) {
            this.OpenedEvent.Set();
        }

        private void WebSocketClientError(object sender, SuperSocket.ClientEngine.ErrorEventArgs e) {
            Console.WriteLine(e.Exception.GetType() + ":" + e.Exception.Message + Environment.NewLine + e.Exception.StackTrace);

            if (e.Exception.InnerException != null) {
                Console.WriteLine(e.Exception.InnerException.GetType());
            }
        }
    }
}