namespace EmotivDrivers {
    public class Authorizer {
        private CortexClient cortexClient;
        
        private string cortexToken;
        private string emotivId;
        private string licenseId;

        private bool isEulaAccepted;
        private bool hasAccessRight;

        private ushort debitNo = 5; //Default value
    }
}