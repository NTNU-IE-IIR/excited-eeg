namespace EmotivDrivers.CortexClient {
    static class Config {
        public static string AppClientId = "giK2jIkOy5x0Ry1xwixpdbAykYZi1Ebr3xjv7Asy";
        public static string AppClientSecret =
            "SzXU4drfTJuAshSb9wInyvE3MYx5Z0jAZR1Au0b2ETXp0F8T7wpbTYXzAqezvJxYM7u9UJndXkiKdUDD6hE5h0G5ZbEQdtQcCn43PJXzme9DHsS95alVLQtCDGgql4Ot";
        
        // If you use an Epoc Flex headset, then you must put your configuration here
        public static string FlexMapping = @"{
                                  'CMS':'TP8', 'DRL':'P6',
                                  'RM':'TP10','RN':'P4','RO':'P8'}";
    }

    public static class WarningCode {
        public const int StreamStop = 0;
        public const int SessionAutoClosed = 1;
        public const int UserLogin = 2;
        public const int UserLogout = 3;
        public const int ExtenderExportSuccess = 4;
        public const int ExtenderExportFailed = 5;
        public const int UserNotAcceptLicense = 6;
        public const int UserNotHaveAccessRight = 7;
        public const int UserRequestAccessRight = 8;
        public const int AccessRightGranted = 9;
        public const int AccessRightRejected = 10;
        public const int CannotDetectOSUSerInfo = 11;
        public const int CannotDetectOSUSername = 12;
        public const int ProfileLoaded = 13;
        public const int ProfileUnloaded = 14;
        public const int CortexAutoUnloadProfile = 15;
        public const int UserLoginOnAnotherOsUser = 16;
        public const int EULAAccepted = 17;
        public const int StreamWritingClosed = 18;
        public const int HeadsetConnected = 104;
    }
}
