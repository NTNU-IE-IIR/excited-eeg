using System;
using System.Collections.Generic;
using System.Drawing;
using System.Runtime.CompilerServices;
using System.Windows.Forms;
using EmotivDrivers.CortexClient;

namespace EmotivDrivers.GUI {
    public class LoadProfileGUI : GUI {

        private Panel buttonContainer;

        private List<string> profiles;

        private CortexClient.CortexClient cortexClient;
        private string cortexToken;
        private string sessionId;
        private string licenseId;
        private bool isActiveSession;

        private Authorizer authorizer;
        private SessionCreator sessionCreator;
        private ProfileHandler profileHandler;


        public LoadProfileGUI() {
            authorizer = new Authorizer();
            sessionCreator = new SessionCreator();
            profileHandler = new ProfileHandler();
            licenseId = "";
            cortexToken = "";
            sessionId = "";
            isActiveSession = false;

            cortexClient = CortexClient.CortexClient.Instance;
            SubscribeToEvents();
            this.authorizer.Start(licenseId);
            //LoadProfilesList();

            //InitComponents();
        }
        


        private void SubscribeToEvents() {
            this.profileHandler.OnProfileQuery += ProfileQueryOk;
            this.authorizer.OnAuthorized += AuthorizedOK;
        }

        private void InitComponents() {
            SetupProfileButtons();
        }

        private void SetupProfileButtons() {
            this.Text = "Load Emotiv profiles";

            buttonContainer = new Panel();

            Point newLocation = new Point(0, 0);
            for (int i = 0; i < 5; i++) {
                Button profileButton = new Button();

                profileButton.Size = new Size(200, 100);
                profileButton.BackColor = Color.FromArgb(255, 30, 168, 232);
                profileButton.Font = new Font("Verdana", 14);
                profileButton.Text = "I am a button " + i;

                profileButton.Location = newLocation;
                newLocation.Offset(0, profileButton.Height + 10);
                buttonContainer.Controls.Add(profileButton);
            }

            buttonContainer.AutoSize = true;

            buttonContainer.Location = new Point(guiWidth / 2 - buttonContainer.Size.Width / 2, 10);

            this.AutoScroll = true;

            this.Controls.Add(buttonContainer);
        }

        private void LoadProfilesList() {
            cortexClient.Open();
            cortexClient.Authorize("", 5);
            cortexClient.QueryProfile(cortexToken);
            profiles = profileHandler.ProfileList;
            Console.WriteLine(profiles.Count);
        }

        private void ProfileQueryOk(object sender, List<string> profileList) {
            this.profiles = profileList;
            Console.WriteLine(profiles.Count);
            InitComponents();
        }
        
        private void AuthorizedOK(object sender, string cortexToken) {
            if (!String.IsNullOrEmpty(cortexToken)) {
                this.cortexToken = cortexToken;
                
            }
            this.cortexClient.QueryProfile(cortexToken);
        }
    }
}