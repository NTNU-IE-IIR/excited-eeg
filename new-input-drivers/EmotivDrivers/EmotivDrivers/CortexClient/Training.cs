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
        }
    }
}