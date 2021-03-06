﻿using System;

namespace EmotivDrivers.CortexSystem {
    
    /// <summary>
    /// Different utilises used for the cortex client
    /// </summary>
    public class Utils {
        
        public static Int64 GetEpochTimeNow() {
            TimeSpan time = DateTime.UtcNow - new DateTime(1970, 1, 1);
            Int64 timeSinceEpoch = (Int64) time.TotalMilliseconds;
            return timeSinceEpoch;
        }

        public static string GenerateUuidProfileName(string prefix) {
            return prefix + "-" + GetEpochTimeNow();
        }
    }
}