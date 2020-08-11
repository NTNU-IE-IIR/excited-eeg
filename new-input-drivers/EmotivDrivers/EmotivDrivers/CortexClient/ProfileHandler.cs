using System;
using System.Collections.Generic;
using Newtonsoft.Json.Linq;

namespace EmotivDrivers.CortexClient {
    /// <summary>
    /// Used to for handling user profile selection, loading, and unloading
    /// </summary>
    public class ProfileHandler {
        
        /// <summary>
        /// --------------------------- VARIABLES ---------------------------
        /// </summary>
        private CortexClient ctxClient;
        private string cortexToken;
        private string profileName;
        private string headsetId;
        private List<string> profileList;
        
        /// <summary>
        /// --------------------------- EVENTS ---------------------------
        /// </summary>
        public event EventHandler<string> OnProfileLoaded;
        public event EventHandler<String> OnProfileQuery;
        
        /// <summary>
        /// --------------------------- CONSTRUCTORS ---------------------------
        /// </summary>
        public ProfileHandler() {
            this.cortexToken = "";
            this.profileName = "";
            this.headsetId = "";
            this.profileList = new List<string>();
            
            this.ctxClient = CortexClient.Instance;
            
            this.ctxClient.OnQueryProfile += QueryProfileOK;
            this.ctxClient.OnLoadProfile += ProfileLoadedOK;
        }
        
        /// <summary>
        /// --------------------------- METHODS ---------------------------
        /// </summary>
        private void QueryProfileOK(object sender, JArray profiles) {
            Console.WriteLine("Query profile OK.");

            foreach (JObject element in profiles) {
                string name = (string) element["name"];
                profileList.Add(name);
            }

            OnProfileQuery(this, profileName);
        }

        private void ProfileLoadedOK(object sender, string loadedProfile) {
            if (this.profileName.Equals(loadedProfile))
            {
                Console.WriteLine("Profile loaded: " + loadedProfile);
                OnProfileLoaded(this, loadedProfile);
            }
        }

        /// <summary>
        /// Loads a user profile to a given headset
        /// </summary>
        /// <param name="profileName">The profile to be loaded</param>
        /// <param name="cortexToken">The currently valid cortex token</param>
        /// <param name="headsetId">The id of the given headset</param>
        public void LoadProfile(string profileName, string cortexToken, string headsetId) {
            this.profileName = profileName;
            this.cortexToken = cortexToken;
            this.headsetId = headsetId;
            
            if (this.profileList.Contains(profileName)){
                this.ctxClient.SetupProfile(cortexToken, profileName, "load", this.headsetId);
            }
            else {
                Console.WriteLine("The profile can not be loaded. The name " + profileName + " has not existed.");
            }
        }
        
        /// <summary>
        /// Unloads a user profile from the currently active headset
        /// </summary>
        /// <param name="profileName">The profile to be unloaded</param>
        public void UnLoadProfile(string profileName)
        {
            if (this.profileList.Contains(profileName))
                this.ctxClient.SetupProfile(this.cortexToken, profileName, "unload", this.headsetId);
            else
                Console.WriteLine("The profile can not be unloaded. The name " + profileName + " has not existed.");
        }
    }
}