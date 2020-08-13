using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Windows.Forms;
using EmotivDrivers.CortexClient;

namespace EmotivDrivers.GUI {
    public class LoadProfileGUI : GUI {

        private Panel buttonContainer;

        private List<string> profileList;

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
            for (int i = 0; i < profileList.Count; i++) {
                Button profileButton = new Button();

                profileButton.Size = new Size(200, 100);
                profileButton.BackColor = Color.FromArgb(255, 30, 168, 232);
                profileButton.Font = new Font("Verdana", 14);
                profileButton.Text = profileList.ElementAt(i);

                profileButton.Location = newLocation;
                newLocation.Offset(0, profileButton.Height + 10);
                buttonContainer.Controls.Add(profileButton);
            }

            buttonContainer.AutoSize = true;

            buttonContainer.Location = new Point(guiWidth / 2 - buttonContainer.Size.Width / 2, 10);

            this.AutoScroll = true;

            this.Controls.Add(buttonContainer);
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
            this.cortexClient.QueryProfile(cortexToken);
        }
    }
}