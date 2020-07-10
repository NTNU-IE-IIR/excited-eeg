using System;

namespace EmotivDrivers {
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

        private void SubscribeToEvents() {
            cortexClient.OnConnected += ConnectedOK;
            cortexClient.OnGetUserLogin += GetUserLoginOK;
            cortexClient.OnUserLogin += UserLoginOK;
            cortexClient.OnUserLogout += UserLogoutOK;
            cortexClient.OnHasAccessRight += HasAccessRightOK;
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
    }
}