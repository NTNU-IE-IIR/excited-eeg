using System;
using System.Collections.Generic;

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
            cortexClient.OnSubscribeData += SubscribeDataOk;
            cortexClient.OnCreateProfile += ProfileCreatedOk;
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

        private void GetDetectionOk() {
            
        }
    }
}