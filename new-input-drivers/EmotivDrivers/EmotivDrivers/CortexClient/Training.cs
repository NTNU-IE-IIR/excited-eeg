using System;
using System.Collections.Generic;
using Newtonsoft.Json.Linq;

namespace EmotivDrivers.CortexClient {
    public class Training {

        private CortexClient cortexClient;
        private string profileName;
        private string cortexToken;
        private string sessionId;
        private string detection;
        private bool isProfileLoaded;
        private string headsetId;
        private List<string> availableActions;

        private HeadsetFinder headsetFinder;
        private Authorizer authorizer;
        private SessionCreator sessionCreator;
        private List<string> profileList;

        public event EventHandler<string> OnProfileLoaded;
        public event EventHandler<bool> OnUnProfileLoaded;
        public event EventHandler<bool> OnTrainingSucceeded;
        public event EventHandler<bool> OnReadyForTraining; 
        public Training() {
            authorizer = new Authorizer();
            headsetFinder = new HeadsetFinder();
            sessionCreator = new SessionCreator();
            cortexToken = "";
            sessionId = "";
            isProfileLoaded = false;
            availableActions = new List<string>();
            profileList = new List<string>();
            
            cortexClient = CortexClient.Instance;

            SubscribeToEvents();
        }

        private void SubscribeToEvents() {
            cortexClient.OnErrorMsgReceived += MessageErrorReceived;
            cortexClient.OnGetDetectionInfo += GetDetectionOk;
            cortexClient.OnStreamDataReceived += StreamDataReceived;
            cortexClient.OnSubscribeData += SubscribeDataOK;
            cortexClient.OnCreateProfile += ProfileCreatedOK;
            cortexClient.OnLoadProfile += ProfileLoadedOk;
            cortexClient.OnSaveProfile += ProfileSavedOk;
            cortexClient.OnUnloadProfile += ProfileUnloadedOk;
            cortexClient.OnTraining += TrainingOk;
            cortexClient.OnQueryProfile += QueryProfileOk;

            authorizer.OnAuthorized += AuthorizerOK;
            headsetFinder.OnHeadsetConnected += HeadsetConnectedOK;
            sessionCreator.OnSessionCreated += SessionCreatedOk;
            sessionCreator.OnSessionClosed += SessionClosedOK;
        }

        private void MessageErrorReceived(object sender, ErrorMsgEventArgs eventArgs) {
            Console.WriteLine("Message error received, code: " + eventArgs.Code + ", message: " + eventArgs.MessageError);
        }

        private void GetDetectionOk(object sender, JObject rsp) {
            Console.WriteLine("Get detection info ok: " + rsp);

            availableActions = rsp["actions"].ToObject<List<string>>();

            cortexClient.QueryProfile(cortexToken);
        }

        private void StreamDataReceived(object sender, StreamDataEventArgs eventArgs) {
            if (eventArgs.StreamName == "sys") {
                List<string> data = eventArgs.Data.ToObject<List<string>>();
                JArray dataEvent = eventArgs.Data;
                string detection = dataEvent[0].ToString();
                string eventType = dataEvent[1].ToString();

                if (detection == "mentalCommand") {
                    if (eventType == "MC_Started") {
                        Console.WriteLine("Start training...");
                    }
                    else if (eventType == "MC_Succeeded") {
                        OnTrainingSucceeded(this, true);
                    }
                    else if (eventType == "MC_Completed" ||
                             eventType == "MC_Rejected" ||
                             eventType == "MC_DataErased" ||
                             eventType == "MC_Reset") {
                        cortexClient.SetupProfile(cortexToken, profileName, "save", headsetId);
                    }
                }
                else if (detection == "facialExpression") {
                    if (eventType == "FE_Started") {
                        Console.WriteLine("Start training...");
                    }
                    else if (eventType == "FE_Succeeded") {
                        OnTrainingSucceeded(this, true);
                    }
                    else if (eventType == "FE_Completed" ||
                             eventType == "FE_Rejected" ||
                             eventType == "FE_DataErased" ||
                             eventType == "FE_Reset") {
                        cortexClient.SetupProfile(cortexToken, profileName, "save", headsetId);
                    }
                }
            }
        }

        private void SubscribeDataOK(object sender, MultipleResultEventArgs eventArgs) {
            bool found = false;
            Dictionary<string, JArray> header = new Dictionary<string, JArray>();

            foreach (JObject element in eventArgs.SuccessList) {
                string streamName = (string) element["streamName"];

                if (streamName == "sys") {
                    found = true;
                    Console.WriteLine(element);
                }
            }

            if (found) {
                OnReadyForTraining(this, true);
            }
            else {
                Console.WriteLine("Cannot subscribe to training event");
            }
        }

        private void ProfileCreatedOK(object sender, string profileName) {
            Console.WriteLine("The profile " + profileName + " is created successfully. Please load the profile to use it");

            if (!profileList.Contains(profileName)) {
                profileList.Add(profileName);
            }
        }
    }
}