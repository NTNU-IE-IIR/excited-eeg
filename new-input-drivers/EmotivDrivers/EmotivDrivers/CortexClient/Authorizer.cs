using System;

namespace EmotivDrivers.CortexClient {
    
    /// <summary>
    /// Class used to handle the authorization of a connection to the Emotiv cortex API.
    /// It communicates with the cortex client by using event's.
    /// In this class you can:
    ///     Check if your connection to the cortex API is OK.
    ///     Check if the login of a Emotiv user to the cortex API is correct.
    ///     Check if a login process of a Emotiv user to the cortex API was OK.
    ///     Check if a logout process of a Emotiv user to the cortex API was OK.
    ///     Check if a user has access ot the cortex API.
    ///     Confirmation if a Emotiv user have or does not have access to the cortex API.
    ///     Confirmation that cortex API access rights have been granted to a Emotiv user.
    ///     Check if the total authorization process was OK
    ///     Check if a user has accepted EULA
    /// </summary>
    public class Authorizer {
        
        /// <summary>
        /// --------------------------- VARIABLES ---------------------------
        /// </summary>
        private CortexClient cortexClient;
        
        private string cortexToken;
        private string emotivId;
        private string licenseId;

        private bool isEulaAccepted;
        private bool hasAccessRight;
        
        //Default value
        private ushort debitNo = 5; 
        
        /// <summary>
        /// --------------------------- EVENTS ---------------------------
        /// </summary>
        public event EventHandler<string> OnAuthorized;
        
        /// <summary>
        /// --------------------------- CONSTRUCTORS ---------------------------
        /// </summary>
        public Authorizer() {
            cortexClient = CortexClient.Instance;
            cortexToken = "";
            emotivId = "";
            isEulaAccepted = false;
            hasAccessRight = false;
            
            SubscribeToEvents();
        }
        
        /// <summary>
        /// --------------------------- METHODS ---------------------------
        /// </summary>
        
        /// <summary>
        /// Starts the authorization process for a new connection between
        /// the cortex client and the cortex API. 
        /// </summary>
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
            else {
                cortexClient.RequestAccess();
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