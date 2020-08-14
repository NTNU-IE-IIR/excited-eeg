using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;

namespace EmotivDrivers.CortexSystem {
    /// <summary>
    /// Responsible for recording data from the Emotiv devices.
    /// </summary>
    public class Record {
        
        /// <summary>
        /// --------------------------- VARIABLES ---------------------------
        /// </summary>
        private string uuid;
        private string applicationId;
        private string licenseId;
        private string title;
        private string description;
        private string startDateTime;
        private string endDateTime;
        private JArray markers;
        private List<string> tags;

        /// <summary>
        /// --------------------------- PROPERTIES ---------------------------
        /// </summary>
        public string Uuid {
            get { return this.uuid; }
            set { this.uuid = value; }
        }

        public string ApplicationId {
            get { return this.applicationId; }
            set { this.applicationId = value; }
        }

        public string LicenseId {
            get { return this.licenseId; }
            set { this.licenseId = value; }
        }

        public string Title {
            get { return this.title; }
            set { this.title = value; }
        }

        public string Description {
            get { return this.description; }
            set { this.description = value; }
        }

        public string StartDateTime {
            get { return this.startDateTime; }
            set { this.startDateTime = value; }
        }

        public string EndDateTime {
            get { return this.endDateTime; }
            set { this.endDateTime = value; }
        }

        public JArray Markers {
            get { return this.markers; }
            set { this.markers = value; }
        }

        public List<string> Tags {
            get { return this.tags; }
            set { this.tags = value; }
        }
        
        /// <summary>
        /// --------------------------- CONSTRUCTORS ---------------------------
        /// </summary>
        public Record() {}
        
        public Record(JObject obj) {
            uuid = (string)obj["uuid"];
            this.licenseId = (string)obj["licenseId"];
            this.applicationId = (string)obj["applicationId"];
            this.title = (string)obj["title"];
            this.description = (string)obj["description"];
            this.startDateTime = (string)obj["startDatetime"];
            this.endDateTime = (string)obj["endDatetime"];
            this.markers = (JArray)obj["markers"];
            this.tags = obj["tags"].ToObject<List<string>>();
        }
        
        /// <summary>
        /// --------------------------- METHODS ---------------------------
        /// </summary>
        
        /// <summary>
        /// Prints out what is currently being recorded
        /// </summary>
        public void PrintOut() {
            Console.WriteLine("id: " + uuid + ", title: " + this.title + ", startDatetime: " + this.startDateTime + ", endDatetime: " + this.endDateTime);
        }

    }
}