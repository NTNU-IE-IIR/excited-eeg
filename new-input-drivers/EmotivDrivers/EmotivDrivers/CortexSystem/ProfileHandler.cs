﻿using System;
using System.Collections.Generic;
using Newtonsoft.Json.Linq;

namespace EmotivDrivers.CortexSystem {
    public class ProfileHandler {
        
        private CortexClient ctxClient;
        private string cortexToken;
        private string profileName;
        private string headsetId;
        private List<string> profileList;
        public List<string> ProfileList {
            get => profileList;
            set => profileList = value;
        }

        //events
        public event EventHandler<string> OnProfileLoaded;
        public event EventHandler<List<string>> OnProfileQuery;
        
        //constructor
        public ProfileHandler() {
            this.cortexToken = "";
            this.profileName = "";
            this.headsetId = "";
            this.profileList = new List<string>();
            
            this.ctxClient = CortexClient.Instance;
            
            this.ctxClient.OnQueryProfile += QueryProfileOK;
            this.ctxClient.OnLoadProfile += ProfileLoadedOK;
        }
        
        private void QueryProfileOK(object sender, JArray profiles) {
            Console.WriteLine("Query profile OK.");

            foreach (JObject element in profiles) {
                string name = (string) element["name"];
                profileList.Add(name);
            }

            OnProfileQuery(this, profileList);
        }
        
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

        private void ProfileLoadedOK(object sender, string loadedProfile) {
            if (this.profileName.Equals(loadedProfile)) {
                Console.WriteLine("Profile loaded: " + loadedProfile);
                OnProfileLoaded(this, loadedProfile);
            }
        }
    }
}