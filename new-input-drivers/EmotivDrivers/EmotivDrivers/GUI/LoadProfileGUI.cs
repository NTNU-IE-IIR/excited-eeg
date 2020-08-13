using System;
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

        public LoadProfileGUI() {
            authorizer = new Authorizer();
            sessionCreator = new SessionCreator();
            profileHandler = new ProfileHandler();
            headsetFinder = new HeadsetFinder();
            
            licenseId = "";
            cortexToken = "";
            sessionId = "";
            isActiveSession = false;

            cortexClient = CortexClient.CortexClient.Instance;
            SubscribeToEvents();
            this.authorizer.Start(licenseId);
        }
        
        private void SubscribeToEvents() {
            this.profileHandler.OnProfileQuery += ProfileQueryOk;
            this.authorizer.OnAuthorized += AuthorizedOK;
            this.headsetFinder.OnHeadsetConnected += HeadsetConnectedOK;
            this.profileHandler.OnProfileLoaded += ProfileLoadedOK;
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

        private void OnProfileButtonClick(object sender, EventArgs eventArgs) {
            Button cb = (Button) sender;
            string profileName = cb.AccessibleName;

            switch (profileName) {
                case "0" :
                    profileHandler.LoadProfile(profileList.ElementAt(0), cortexToken, headsetId);
                    StartConsoleOutputGUI();
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

        private void StartConsoleOutputGUI() {
            ConsoleOutputGUI consoleOutputGui = new ConsoleOutputGUI();
            consoleOutputGui.Owner = this;
            consoleOutputGui.Show();
            this.Hide();
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
        
        protected override void OnFormClosing(FormClosingEventArgs e) {
            base.OnFormClosing(e);
            Application.Exit();
        }
    }
}