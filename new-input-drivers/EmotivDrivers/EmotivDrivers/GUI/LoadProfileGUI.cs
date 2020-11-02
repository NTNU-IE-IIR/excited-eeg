using System;
using System.Collections;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.Linq;
using System.Windows.Forms;
using EmotivDrivers.AppConnection;
using EmotivDrivers.CortexSystem;
using Newtonsoft.Json.Linq;

namespace EmotivDrivers.GUI {
    public class LoadProfileGUI : GUI {
        
        /// <summary>
        /// --------------------------- VARIABLES ---------------------------
        /// </summary>
        private Panel buttonContainer;
        private Label profileLoadingLabel;
        private TextBox consoleOutputTextBox;
        
        private int consoleOutputTextBoxWidth = 750;
        private int consoleOutputTextBoxHeight = 400;
        
        private List<string> profileList;

        private readonly CortexClient cortexClient;
        private string cortexToken;
        private string sessionId;
        private string licenseId;
        private readonly bool isActiveSession;
        private string headsetId;

        private readonly Authorizer authorizer;
        private readonly SessionCreator sessionCreator;
        private readonly ProfileHandler profileHandler;
        private readonly HeadsetFinder headsetFinder;
        
        private List<string> streams;
        public List<string> Streams {
            get { return this.streams; }
            set { this.streams = value; }
        }
        
        private static float currentTimeStamp = 0;
        private static float[] previousTriggerTime = new float [14];
        private static int neutral = 0;
        private static int left = 1;
        private static int right = 2;
        private static int push = 3;
        private static int pull = 4;
        private static int lift = 5;
        private static int drop = 6;
        private static int rotateRight = 7;
        private static int rotateLeft = 8;
        private static int rotateClockwise = 9;
        private static int rotateCounterClockwise = 10;
        private static int rotateReverse = 11;
        private static int rotateForwards = 12;
        private static int disappear = 13;
        
        private static float commandInterval = 1.5f;
        private static float triggerThreshold = 0.30f;

        private static Stopwatch stopwatch = new Stopwatch();

        private ApplicationConnection applicationConnection;

        /// <summary>
        /// --------------------------- EVENTS ---------------------------
        /// </summary>
        public event EventHandler<ArrayList> OnComDataReceived; // command word data
        public event EventHandler<Dictionary<string, JArray>> OnSubscribed;
        
        /// <summary>
        /// --------------------------- CONSTRUCTORS ---------------------------
        /// </summary>
        public LoadProfileGUI() {
            this.authorizer = new Authorizer();
            this.sessionCreator = new SessionCreator();
            this.profileHandler = new ProfileHandler();
            this.headsetFinder = new HeadsetFinder();
            
            
            this.licenseId = "";
            this.cortexToken = "";
            this.sessionId = "";
            this.isActiveSession = false;
            Array.Clear(previousTriggerTime, 0, previousTriggerTime.Length);

            this.streams = new List<string>();
            
            this.applicationConnection = ApplicationConnection.Instance;
            
            stopwatch.Start();
            
            this.cortexClient = CortexClient.Instance;
            SubscribeToEvents();
            this.authorizer.Start(licenseId);
        }
        
        /// <summary>
        /// --------------------------- METHODS ---------------------------
        /// </summary>
        private void SubscribeToEvents() {
            this.profileHandler.OnProfileQuery += ProfileQueryOk;
            this.profileHandler.OnProfileLoaded += ProfileLoadedOK;
            
            this.authorizer.OnAuthorized += AuthorizedOK;
            this.headsetFinder.OnHeadsetConnected += HeadsetConnectedOK;
            this.sessionCreator.OnSessionCreated += SessionCreatedOk;

            this.cortexClient.OnSubscribeData += SubscribeDataOK;
            this.cortexClient.OnStreamDataReceived += StreamDataReceived;

            this.OnSubscribed += SubscribedOK;
            this.OnComDataReceived += ComDataReceived;
        }

        private void InitComponents() {
            this.buttonContainer = new Panel();
            this.profileLoadingLabel = new Label();
            this.consoleOutputTextBox = new TextBox();
            
            SetupProfileLoadingLabel();
            SetupProfileButtons();
            
            this.Controls.Add(buttonContainer);
            this.Controls.Add(profileLoadingLabel);
        }

        private void SetupProfileButtons() {
            this.Text = "Load Emotiv profiles";
            
            Point newLocation = new Point(0, 0);
            for (int i = 0; i < profileList.Count; i++) {
                Button profileButton = new Button();

                profileButton.Size = new Size(200, 100);
                profileButton.BackColor = Color.FromArgb(255, 30, 168, 232);
                profileButton.Font = new Font("Verdana", 14);
                profileButton.Text = profileList.ElementAt(i);
                profileButton.AccessibleName = i.ToString();
                
                profileButton.Click += OnProfileButtonClick;
                
                profileButton.Location = newLocation;
                newLocation.Offset(0, profileButton.Height + 10);
                this.buttonContainer.Controls.Add(profileButton);
            }

            this.buttonContainer.AutoSize = true;

            this.buttonContainer.Location = new Point(guiWidth / 2 - buttonContainer.Size.Width / 2, 40);

            this.AutoScroll = true;
        }

        private void SetupProfileLoadingLabel() {
            this.profileLoadingLabel.Text = "Select profile to load";
            this.profileLoadingLabel.Font = new Font("Verdana", 14);
            this.profileLoadingLabel.AutoSize = true;
            this.profileLoadingLabel.Location = new Point((guiWidth / 2) - this.profileLoadingLabel.Width - 4, 10);
        }

        private void SetupConsoleOutput() {
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

            this.consoleOutputTextBox.ReadOnly = true;
            this.consoleOutputTextBox.Location = new Point((guiWidth / 2) - (consoleOutputTextBoxWidth / 2), (guiHeight / 2) - consoleOutputTextBoxHeight / 2 - 20);
            this.consoleOutputTextBox.MinimumSize = new Size(consoleOutputTextBoxWidth, consoleOutputTextBoxHeight);
            this.consoleOutputTextBox.Font = new Font("", 12);
            this.consoleOutputTextBox.Multiline = true;
            this.consoleOutputTextBox.ScrollBars = ScrollBars.Both;
        }
        
        private void OnProfileButtonClick(object sender, EventArgs eventArgs) {
            Button senderButton = (Button) sender;
            string profileName = senderButton.AccessibleName;

            switch (profileName) {
                case "0" :
                    AddStream("com");
                    this.profileHandler.LoadProfile(profileList.ElementAt(0), cortexToken, headsetId);
                    this.buttonContainer.Dispose();
                    this.profileLoadingLabel.Dispose();
                    SetupConsoleOutput();
                    break;
                
                case "1":
                    AddStream("com");
                    this.profileHandler.LoadProfile(profileList.ElementAt(1), cortexToken, headsetId);
                    this.buttonContainer.Dispose();
                    this.profileLoadingLabel.Dispose();
                    SetupConsoleOutput();
                    break;
                
                case "2":
                    AddStream("com");
                    this.profileHandler.LoadProfile(profileList.ElementAt(2), cortexToken, headsetId);
                    this.buttonContainer.Dispose();
                    this.profileLoadingLabel.Dispose();
                    SetupConsoleOutput();
                    break;
                
                case "3":
                    AddStream("com");
                    this.profileHandler.LoadProfile(profileList.ElementAt(3), cortexToken, headsetId);
                    this.buttonContainer.Dispose();
                    this.profileLoadingLabel.Dispose();
                    SetupConsoleOutput();
                    break;
                
                case "4":
                    AddStream("com");
                    this.profileHandler.LoadProfile(profileList.ElementAt(4), cortexToken, headsetId);
                    this.buttonContainer.Dispose();
                    this.profileLoadingLabel.Dispose();
                    SetupConsoleOutput();
                    break;
                
                case "5":
                    AddStream("com");
                    this.profileHandler.LoadProfile(profileList.ElementAt(5), cortexToken, headsetId);
                    this.buttonContainer.Dispose();
                    this.profileLoadingLabel.Dispose();
                    SetupConsoleOutput();
                    break;
                
                case "6":
                    AddStream("com");
                    this.profileHandler.LoadProfile(profileList.ElementAt(6), cortexToken, headsetId);
                    this.buttonContainer.Dispose();
                    this.profileLoadingLabel.Dispose();
                    SetupConsoleOutput();
                    break;
                
                case "7":
                    AddStream("com");
                    this.profileHandler.LoadProfile(profileList.ElementAt(7), cortexToken, headsetId);
                    this.buttonContainer.Dispose();
                    this.profileLoadingLabel.Dispose();
                    SetupConsoleOutput();
                    break;
                
                case "8":
                    AddStream("com");
                    this.profileHandler.LoadProfile(profileList.ElementAt(8), cortexToken, headsetId);
                    this.buttonContainer.Dispose();
                    this.profileLoadingLabel.Dispose();
                    SetupConsoleOutput();
                    break;
                
                case "9":
                    AddStream("com");
                    this.profileHandler.LoadProfile(profileList.ElementAt(9), cortexToken, headsetId);
                    this.buttonContainer.Dispose();
                    this.profileLoadingLabel.Dispose();
                    SetupConsoleOutput();
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
            ArrayList data = e.Data.ToObject<ArrayList>();
            // insert timestamp to datastream
            data.Insert(0, e.Time);
            if (e.StreamName == "com") {
                OnComDataReceived(this, data);
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
        
        private void ComDataReceived(object sender, ArrayList comData) {
            string command = comData[1].ToString();
            float power = float.Parse(comData[2].ToString());
            
            if (power >= triggerThreshold) {

                currentTimeStamp = (float) stopwatch.Elapsed.TotalSeconds;

                switch (command) {
                    case "neutral":
                        if (currentTimeStamp - previousTriggerTime[neutral] >= commandInterval) {
                            previousTriggerTime[neutral] = currentTimeStamp;
                            applicationConnection.SendMessageToKeyboardServer("0");
                            Console.WriteLine("Sending command: Neutral.");
                        }
                        break;

                    case "left":
                        if (currentTimeStamp - previousTriggerTime[left] >= commandInterval) {
                            previousTriggerTime[left] = currentTimeStamp;
                            applicationConnection.SendMessageToKeyboardServer("1");
                            Console.WriteLine("Sending command: Left.");
                            
                        }
                        break;

                    case "right":
                        if (currentTimeStamp - previousTriggerTime[right] >= commandInterval) {
                            previousTriggerTime[right] = currentTimeStamp;
                            applicationConnection.SendMessageToKeyboardServer("2");
                            Console.WriteLine("Sending command: Right.");
                        }
                        break;

                    case "push":
                        if (currentTimeStamp - previousTriggerTime[push] >= commandInterval) {
                            previousTriggerTime[push] = currentTimeStamp;
                            applicationConnection.SendMessageToKeyboardServer("3");
                            Console.WriteLine("Sending command: Push.");
                        }
                        break;

                    case "pull":
                        if (currentTimeStamp - previousTriggerTime[pull] >= commandInterval) {
                            previousTriggerTime[pull] = currentTimeStamp;
                            applicationConnection.SendMessageToKeyboardServer("4");
                            Console.WriteLine("Sending command: Pull.");
                        }
                        break;
                    
                    case "lift":
                        if (currentTimeStamp - previousTriggerTime[lift] >= commandInterval) {
                            previousTriggerTime[lift] = currentTimeStamp;
                            applicationConnection.SendMessageToKeyboardServer("5");
                            Console.WriteLine("Sending command: Lift.");
                        }
                        break;
                        
                    case "drop":
                        if (currentTimeStamp - previousTriggerTime[drop] >= commandInterval) {
                            previousTriggerTime[drop] = currentTimeStamp;
                            applicationConnection.SendMessageToKeyboardServer("6");
                            Console.WriteLine("Sending command: Drop.");
                        }
                        break;
                    
                    case "rotateRight":
                        if (currentTimeStamp - previousTriggerTime[rotateRight] >= commandInterval) {
                            previousTriggerTime[rotateRight] = currentTimeStamp;
                            applicationConnection.SendMessageToKeyboardServer("7");
                            Console.WriteLine("Sending command: Rotate right.");
                        }
                        break;
                    
                    case "rotateLeft":
                        if (currentTimeStamp - previousTriggerTime[rotateLeft] >= commandInterval) {
                            previousTriggerTime[rotateLeft] = currentTimeStamp;
                            applicationConnection.SendMessageToKeyboardServer("8");
                            Console.WriteLine("Sending command: Rotate left.");
                        }
                        break;
                    
                    case "rotateClockwise":
                        if (currentTimeStamp - previousTriggerTime[rotateClockwise] >= commandInterval) {
                            previousTriggerTime[rotateClockwise] = currentTimeStamp;
                            applicationConnection.SendMessageToKeyboardServer("9");
                            Console.WriteLine("Sending command: Rotate clockwise.");
                        }
                        break;
                    
                    case "rotateCounterClockwise":
                        if (currentTimeStamp - previousTriggerTime[rotateCounterClockwise] >= commandInterval) {
                            previousTriggerTime[rotateCounterClockwise] = currentTimeStamp;
                            applicationConnection.SendMessageToKeyboardServer("10");
                            Console.WriteLine("Sending command: Rotate counterclockwise.");
                        }
                        break;
                    
                    case "rotateReverse":
                        if (currentTimeStamp - previousTriggerTime[rotateReverse] >= commandInterval) {
                            previousTriggerTime[rotateReverse] = currentTimeStamp;
                            applicationConnection.SendMessageToKeyboardServer("11");
                            Console.WriteLine("Sending command: Rotate reverse.");
                        }
                        break;
                    
                    case "rotateForwards":
                        if (currentTimeStamp - previousTriggerTime[rotateForwards] >= commandInterval) {
                            previousTriggerTime[rotateForwards] = currentTimeStamp;
                            applicationConnection.SendMessageToKeyboardServer("12");
                            Console.WriteLine("Sending command: Rotate forwards.");
                        }
                        break;
                    
                    case "disappear":
                        if (currentTimeStamp - previousTriggerTime[disappear] >= commandInterval) {
                            previousTriggerTime[disappear] = currentTimeStamp;
                            applicationConnection.SendMessageToKeyboardServer("13");
                            Console.WriteLine("Sending command: Disappear.");
                        }
                        break;
                }
            }
        }
        
        private void AddStream(string stream) {
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
        
        protected override void OnFormClosing(FormClosingEventArgs e) {
            base.OnFormClosing(e);
            Application.Exit();
        }
    }
}