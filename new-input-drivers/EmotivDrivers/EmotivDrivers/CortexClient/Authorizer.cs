using System;

namespace EmotivDrivers.CortexClient {
    public class Authorizer {
        private CortexClient cortexClient;
        
        private string cortexToken;
        private string emotivId;
        private string licenseId;

        private bool isEulaAccepted;
        private bool hasAccessRight;

        private ushort debitNo = 5; //Default value

        public event EventHandler<string> OnAuthorized;

        public Authorizer() {
            cortexClient = CortexClient.Instance;
            cortexToken = "";
            emotivId = "";
            isEulaAccepted = false;
            hasAccessRight = false;
            
            SubscribeToEvents();
        }

        public void Start(string licenseID) {
            licenseID = "";
            this.licenseId = licenseID;
            cortexClient.Open();
        }
        
        private void SubscribeToEvents() {
            cortexClient.OnConnected += ConnectedOK;
            cortexClient.OnGetUserLogin += GetUserLoginOK;
            cortexClient.OnUserLogin += UserLoginOK;
            cortexClient.OnUserLogout += UserLogoutOK;
            cortexClient.OnHasAccessRight += HasAccessRightOK;
            cortexClient.OnRequestAccessDone += RequestAccessDone;
            cortexClient.OnAccessRightGranted += AccessRightGrantedOK;
            cortexClient.OnAuthorize += AuthorizedOK;
            cortexClient.OnEULAAccepted += EULAAcceptedOK;
        }

        private void ConnectedOK(object sender, bool isConnected) {
            if (isConnected) {
                cortexClient.GetUserLogin();
            }
            else {
                Console.WriteLine("Can not connect to Cortex. Please restart cortex service");
            }
        }

        private void GetUserLoginOK(object sender, string emotivId) {
            if (!String.IsNullOrEmpty(emotivId)) {
                this.emotivId = emotivId;
                
                //check if have access right
                cortexClient.HasAccessRights();
            }
        }

        private void UserLoginOK(object sender, string message) {
            if (String.IsNullOrEmpty(this.emotivId)) {
                cortexClient.GetUserLogin();
            }
        }

        private void UserLogoutOK(object sender, string message) {
            Console.WriteLine(message);
            this.emotivId = "";
            this.cortexToken = "";
            this.isEulaAccepted = false;
            this.hasAccessRight = false;
        }

        private void HasAccessRightOK(object sender, bool hasAccessRight) {
            if (hasAccessRight) {
                cortexClient.Authorize(this.licenseId, this.debitNo);
            }
        }

        private void RequestAccessDone(object sender, bool hasAccessRight) {
            if (hasAccessRight) {
                Console.WriteLine("The user has access right to this application.");
            }
            else {
                Console.WriteLine("The user has not granted access right to this application." +
                                  "Please use EMOTIV App to proceed.");
            }
        }

        private void AccessRightGrantedOK(object sender, bool isGranted) {
            if (isGranted) {
                if (String.IsNullOrEmpty(this.cortexToken)) {
                    cortexClient.Authorize(this.licenseId, this.debitNo);
                }
            }
            else {
                Console.WriteLine("The access right to the Application has been rejected");
            }
        }

        private void AuthorizedOK(object sender, string cortexToken) {
            if (!String.IsNullOrEmpty(cortexToken)) {
                Console.WriteLine("Authorize successfully.");
                this.cortexToken = cortexToken;
                this.isEulaAccepted = true;
                OnAuthorized(this, this.cortexToken);
            }
            else {
                this.isEulaAccepted = false;
                Console.WriteLine("User has not accepted EULA. Please accept EULA on EMOTIV App to proceed");
            }
        }

        private void EULAAcceptedOK(object sender, bool isEULAAccepted) {
            this.isEulaAccepted = isEULAAccepted;
            if (isEULAAccepted) {
                cortexClient.Authorize(this.licenseId, this.debitNo);
            }
            else {
                Console.WriteLine("User has not accepted EULA. Please accept EULA on EMOTIV App to proceed");
            }
        }
    }
}