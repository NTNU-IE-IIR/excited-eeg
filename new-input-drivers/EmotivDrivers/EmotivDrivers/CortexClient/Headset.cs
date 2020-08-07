using Newtonsoft.Json.Linq;
using System.Collections;
using System;

namespace EmotivDrivers.CortexClient {
    
    /// <summary>
    /// Used to store all information about a connected emotiv headset
    /// </summary>
    public class Headset {
        /// <summary>
        /// --------------------------- VARIABLES ---------------------------
        /// </summary>
        private string headsetId;
        private string status;
        private string serialId;
        private string firmwareVersion;
        private string dongleSerial;
        private ArrayList sensors;
        private ArrayList motionSensors;
        private JObject settings;
        private string connectedBy;
        private string mode;

        /// <summary>
        /// --------------------------- CONSTRUCTORS ---------------------------
        /// </summary>
        public Headset() {}
        public Headset (JObject jHeadset) {
            HeadsetId = (string)jHeadset["id"];
            Status = (string)jHeadset["status"];
            FirmwareVersion = (string)jHeadset["firmware"];
            DongleSerial = (string)jHeadset["dongle"];
            Sensors = new ArrayList();
            
            foreach (JToken sensor in (JArray)jHeadset["sensors"]) {
                Sensors.Add(sensor.ToString());
            }
            MotionSensors = new ArrayList();
            foreach (JToken sensor in (JArray)jHeadset["motionSensors"]) {
                MotionSensors.Add(sensor.ToString());
            }
            Mode = (string)jHeadset["mode"];
            ConnectedBy = (string)jHeadset["connectedBy"];
            Settings = (JObject)jHeadset["settings"];
        }

        /// <summary>
        /// --------------------------- PROPERTIES ---------------------------
        /// </summary>
        public string HeadsetId {
            get { return this.headsetId; }
            set { this.headsetId = value; }
        }

        public string Status {
            get { return this.status; }
            set { this.status = value; }
        }

        public string SerialId {
            get { return this.serialId; }
            set { this.serialId = value; }
        }

        public string FirmwareVersion {
            get { return this.firmwareVersion; }
            set { this.firmwareVersion = value; }
        }

        public string DongleSerial {
            get { return this.dongleSerial; }
            set { this.dongleSerial = value; }
        }

        public ArrayList Sensors {
            get { return this.sensors; }
            set { this.sensors = value; }
        }

        public ArrayList MotionSensors {
            get { return this.motionSensors; }
            set { this.motionSensors = value; }
        }

        public JObject Settings {
            get { return this.settings; }
            set { this.settings = value; }
        }

        public string ConnectedBy {
            get { return this.connectedBy; }
            set { this.connectedBy = value; }
        }

        public string Mode {
            get { return this.mode; }
            set { this.mode = value; }
        }
    }
}