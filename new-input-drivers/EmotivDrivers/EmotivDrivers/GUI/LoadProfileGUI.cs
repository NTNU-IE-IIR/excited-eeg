using System;
using System.Collections;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Windows.Forms;
using EmotivDrivers.CortexClient;
using Newtonsoft.Json.Linq;

namespace EmotivDrivers.GUI {
    public class LoadProfileGUI : GUI {

        private Panel buttonContainer;

        private Label profileLoadingLabel;

        private TextBox consoleOutputTextBox;
        
        private int consoleOutputTextBoxWidth = 750;
        private int consoleOutputTextBoxHeight = 400;

        private Button stopDriverButton;

        private List<string> profileList;

        private CortexClient.CortexClient cortexClient;
        private string cortexToken;
        private string sessionId;
        private string licenseId;
        private bool isActiveSession;
        private string headsetId;

        private Authorizer authorizer;
        private SessionCreator sessionCreator;
        private ProfileHandler profileHandler;
        private HeadsetFinder headsetFinder;
        
        private List<string> streams;
        public List<string> Streams {
            get { return this.streams; }
            set { this.streams = value; }
        }
        
        public event EventHandler<ArrayList> OnComDataReceived; // command word data
        public event EventHandler<ArrayList> OnMotionDataReceived; // motion data
        public event EventHandler<ArrayList> OnEEGDataReceived; // eeg data
        public event EventHandler<ArrayList> OnDevDataReceived; // contact quality data
        public event EventHandler<ArrayList> OnPerfDataReceived; // performance metric
        public event EventHandler<ArrayList> OnBandPowerDataReceived; // band power
        public event EventHandler<Dictionary<string, JArray>> OnSubscribed;
        
        public LoadProfileGUI() {
            authorizer = new Authorizer();
            sessionCreator = new SessionCreator();
            profileHandler = new ProfileHandler();
            headsetFinder = new HeadsetFinder();
            
            
            licenseId = "";
            cortexToken = "";
            sessionId = "";
            isActiveSession = false;

            streams = new List<string>();
            
            cortexClient = CortexClient.CortexClient.Instance;
            SubscribeToEvents();
            this.authorizer.Start(licenseId);
        }

        private void SubscribeToEvents() {
            stopDriverButton.Click += new EventHandler(OnStopDriverButtonClick);

            this.profileHandler.OnProfileQuery += ProfileQueryOk;
            this.authorizer.OnAuthorized += AuthorizedOK;
            this.headsetFinder.OnHeadsetConnected += HeadsetConnectedOK;
            this.profileHandler.OnProfileLoaded += ProfileLoadedOK;
            this.sessionCreator.OnSessionCreated += SessionCreatedOk;

            this.cortexClient.OnSubscribeData += SubscribeDataOK;
            this.cortexClient.OnStreamDataReceived += StreamDataReceived;

            this.OnSubscribed += SubscribedOK;
            this.OnComDataReceived += ComDataReceived;
        }

        private void InitComponents() {
            SetupProfileLoadingLabel();
            SetupProfileButtons();
        }

        private void SetupProfileButtons() {
            this.Text = "Load Emotiv profiles";

            buttonContainer = new Panel();

            Point newLocation = new Point(0, 0);
            for (int i = 0; i < profileList.Count; i++) {
                Button profileButton = new Button();

                profileButton.Size = new Size(200, 100);
                profileButton.BackColor = Color.FromArgb(255, 30, 168, 232);
                profileButton.Font = new Font("Verdana", 14);
                profileButton.Text = profileList.ElementAt(i);
                profileButton.AccessibleName = i.ToString();
                
                profileButton.Click += new EventHandler(OnProfileButtonClick);
                
                profileButton.Location = newLocation;
                newLocation.Offset(0, profileButton.Height + 10);
                buttonContainer.Controls.Add(profileButton);
            }

            buttonContainer.AutoSize = true;

            buttonContainer.Location = new Point(guiWidth / 2 - buttonContainer.Size.Width / 2, 40);

            this.AutoScroll = true;

            this.Controls.Add(buttonContainer);
        }

        private void SetupProfileLoadingLabel() {
            profileLoadingLabel = new Label();

            this.profileLoadingLabel.Text = "Select profile to load";
            this.profileLoadingLabel.Font = new Font("Verdana", 14);
            this.profileLoadingLabel.AutoSize = true;
            this.profileLoadingLabel.Location = new Point((guiWidth / 2) - this.profileLoadingLabel.Width - 4, 10);
            
            this.Controls.Add(profileLoadingLabel);
        }

        private void SetupConsoleOutput() {
            this.consoleOutputTextBox = new TextBox();
            this.stopDriverButton = new Button();
            
            this.Location = this.Location;
            this.StartPosition = FormStartPosition.Manual;
            this.FormClosing += delegate { this.Show(); };
            this.ClientSize = new Size(guiWidth, guiHeight);
            
            using (var consoleWriter = new ConsoleWriter()) {
                consoleWriter.WriteEvent += ConsoleWriterWriteEvent;
                consoleWriter.WriteLineEvent += ConsoleWriterWriteLineEvent;
                Console.SetOut(consoleWriter);
            }
            
            this.Controls.Add(consoleOutputTextBox);
            this.Controls.Add(stopDriverButton);
            
            this.consoleOutputTextBox.ReadOnly = true;
            this.consoleOutputTextBox.Location = new Point((guiWidth / 2) - (consoleOutputTextBoxWidth / 2), (guiHeight / 2) - consoleOutputTextBoxHeight / 2 - 20);
            this.consoleOutputTextBox.MinimumSize = new Size(consoleOutputTextBoxWidth, consoleOutputTextBoxHeight);
            this.consoleOutputTextBox.Font = new Font("", 12);
            this.consoleOutputTextBox.Multiline = true;
            this.consoleOutputTextBox.ScrollBars = ScrollBars.Both;
        }
        
        private void OnProfileButtonClick(object sender, EventArgs eventArgs) {
            Button cb = (Button) sender;
            string profileName = cb.AccessibleName;

            switch (profileName) {
                case "0" :
                    AddStreams("com");
                    profileHandler.LoadProfile(profileList.ElementAt(0), cortexToken, headsetId);
                    this.buttonContainer.Dispose();
                    this.profileLoadingLabel.Dispose();
                    SetupConsoleOutput();
                    break;
                
                case "1":
                    MessageBox.Show(cb.AccessibleName);
                    break;
                
                case "2":
                    break;
                
                case "3":
                    break;
                
                case "4":
                    break;
                
                case "5":
                    break;
                
                case "6":
                    break;
                
                case "7":
                    break;
                
                case "8":
                    break;
                
                case "9":
                    break;
            }
        }
        
        private void ProfileQueryOk(object sender, List<string> profileList) {
            this.profileList = profileList;
            Console.WriteLine(this.profileList.Count);
            
            // Threading so that the GUI wil load after the profile list has been filled
            if (this.InvokeRequired) {
                this.BeginInvoke((MethodInvoker) delegate { InitComponents(); });
            }
        }
        
        private void AuthorizedOK(object sender, string cortexToken) {
            if (!String.IsNullOrEmpty(cortexToken)) {
                this.cortexToken = cortexToken;
                
            }
            this.headsetFinder.FindHeadset();
            this.cortexClient.QueryProfile(cortexToken);
            
        }

        private void HeadsetConnectedOK(object sender, string headsetId) {
            this.headsetId = headsetId;
            Console.WriteLine("Headset with id: " + headsetId + " connected");
        }
        
        private void ProfileLoadedOK(object sender, string loadedProfile) {
            this.sessionCreator.Create(this.cortexToken, headsetId, this.isActiveSession);
        }
        
        private void SessionCreatedOk(object sender, string sessionId) {
            // subscribe
            this.sessionId = sessionId;
            this.cortexClient.Subscribe(this.cortexToken, this.sessionId, Streams);
        }
        
        private void SubscribeDataOK(object sender, MultipleResultEventArgs e) {
            foreach (JObject ele in e.FailList) {
                string streamName = (string)ele["streamName"];
                int code = (int)ele["code"];
                string errorMessage = (string)ele["message"];
                Console.WriteLine("Subscribe stream " + streamName + " unsuccessfully." + " code: " + code + " message: " + errorMessage);
                if (this.streams.Contains(streamName)) {
                    this.streams.Remove(streamName);
                }
            }
            Dictionary<string, JArray> header = new Dictionary<string, JArray>();
            foreach (JObject ele in e.SuccessList) {
                string streamName = (string)ele["streamName"];
                JArray cols = (JArray)ele["cols"];
                header.Add(streamName, cols);
            }
            if (header.Count > 0) {
                OnSubscribed(this, header);
            }
            else {
                Console.WriteLine("No Subscribe Stream Available");
            }
        }
        
        private void StreamDataReceived(object sender, StreamDataEventArgs e) {
            Console.WriteLine(e.StreamName + " data received.");
            ArrayList data = e.Data.ToObject<ArrayList>();
            // insert timestamp to datastream
            data.Insert(0, e.Time);
            if (e.StreamName == "com") {
                OnComDataReceived(this, data);
            }
            else if (e.StreamName == "eeg") {
                OnEEGDataReceived(this, data); 
            }
            else if (e.StreamName == "mot") {
                OnMotionDataReceived(this, data);
            }
            else if (e.StreamName == "met") {
                OnPerfDataReceived(this, data);
            }
            else if (e.StreamName == "pow") {
                OnBandPowerDataReceived(this, data);
            }
        }
        
        private static void SubscribedOK(object sender, Dictionary<string, JArray> e) {
            foreach (string key in e.Keys) {
                if (key == "com") {
                    // print header
                    ArrayList header = e[key].ToObject<ArrayList>();
                    //add timeStamp to header
                    header.Insert(0, "Timestamp");
                }
            }
        }
        
        private static void ComDataReceived(object sender, ArrayList comData) {
            string command = comData[1].ToString();
            string power = comData[2].ToString();

            switch (command) {
                case "neutral":
                    Console.WriteLine("Neutral");
                    break;
                
                case "left":
                    Console.WriteLine("Left");
                    break;
                
                case "right":
                    Console.WriteLine("Right");
                    break;
                
                case "push":
                    Console.WriteLine("Push");
                    break;
                
                case "pull":
                    Console.WriteLine("Pull");
                    break;
            }
        }
        
        public void AddStreams(string stream) {
            if (!this.streams.Contains(stream)) {
                this.streams.Add(stream);
            }
        }
        
        private void ConsoleWriterWriteEvent(object sender, ConsoleWriterEventArgs eventArgs) {
            this.consoleOutputTextBox.Text += eventArgs.Value;
            try {
                GUIUtils.ScrollToBottom(consoleOutputTextBox);
            }
            catch (ObjectDisposedException e) {
                Console.WriteLine(e);
            }
        }
        
        private void ConsoleWriterWriteLineEvent(object sender, ConsoleWriterEventArgs eventArgs) {
            this.consoleOutputTextBox.Text += eventArgs.Value + Environment.NewLine;
            try {
                GUIUtils.ScrollToBottom(consoleOutputTextBox);
            }
            catch (ObjectDisposedException e) {
                Console.WriteLine(e);
                throw;
            }
        }
        
        private void OnStopDriverButtonClick(object sender, EventArgs eventArgs) {
            Application.Exit();
        }
        
        protected override void OnFormClosing(FormClosingEventArgs e) {
            base.OnFormClosing(e);
            Application.Exit();
        }
    }
}