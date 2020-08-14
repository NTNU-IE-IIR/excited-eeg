using System;
using System.Drawing;
using System.Windows.Forms;
using EmotivDrivers.AppConnection;

namespace EmotivDrivers.GUI {
    public class SetIPEventArgs : EventArgs {
        public string Ip { get; set; }
    }
    
    public class SetIPGUI : GUI {
        
        /// <summary>
        /// --------------------------- VARIABLES ---------------------------
        /// </summary>
        private TextBox ipInputTextBox;

        private const int IP_TEXT_BOX_WIDTH = 300;
        private const int IP_TEXT_BOX_HEIGHT = 30;

        private Label ipLabel;
        private Label ipValidationLabel;
        private Button setIpButton;
        private Button loadProfileButton;
        
        private readonly IPAddressValidator ipAddressValidator;

        /// <summary>
        /// --------------------------- EVENTS ---------------------------
        /// </summary>
        public static event EventHandler<SetIPEventArgs> SetIPEvent;
        
        /// <summary>
        /// --------------------------- CONSTRUCTORS ---------------------------
        /// </summary>
        public SetIPGUI() {
            ipAddressValidator = new IPAddressValidator();
            
            InitComponents();
            
            SubscribeToEvents();
        }
        
        /// <summary>
        /// --------------------------- METHODS ---------------------------
        /// </summary>
        private void SubscribeToEvents() {
            setIpButton.Click += new EventHandler(OnSetIPButtonClick);
            loadProfileButton.Click += new EventHandler(OnLoadProfilesButtonClick);
        }
        
        private void InitComponents() {
            this.ipInputTextBox = new TextBox();
            this.ipLabel = new Label();
            this.setIpButton = new Button();
            this.ipValidationLabel = new Label();
            this.loadProfileButton = new Button();
            
            SetupIpInputTextBox();
            SetupIpLabel();
            SetupSetIpButton();
            SetupLoadProfileButton();
            
            Text = "Emotiv drivers";
            
            this.Controls.Add(this.ipInputTextBox);
            this.Controls.Add(this.ipLabel);
            this.Controls.Add(this.setIpButton);
            this.Controls.Add(this.ipValidationLabel);
            this.Controls.Add(this.loadProfileButton);
        }

        private void SetupIpInputTextBox() {
            this.ipInputTextBox.AcceptsReturn = true;
            this.ipInputTextBox.AcceptsTab = true;
            this.ipInputTextBox.Location = new Point((guiWidth / 2) - (IP_TEXT_BOX_WIDTH / 2), (guiHeight / 2) - (IP_TEXT_BOX_HEIGHT / 2));
            this.ipInputTextBox.MinimumSize = new Size(IP_TEXT_BOX_WIDTH, IP_TEXT_BOX_HEIGHT);
            this.ipInputTextBox.Font = new Font("Verdana", 14);
            this.ipInputTextBox.TextAlign = HorizontalAlignment.Center;
            this.ipInputTextBox.Multiline = true;
            this.ipInputTextBox.ScrollBars = ScrollBars.Vertical;
        }

        private void SetupIpLabel() {
            this.ipLabel.Text = "Input IP-address of device running Keyboard App";
            this.ipLabel.Font = new Font("Verdana", 14);
            this.ipLabel.AutoSize = true;
            this.ipLabel.Left = (guiWidth / 2) - 235;
            this.ipLabel.Top = (guiHeight / 2) - (ipLabel.Top / 2) - 40;
        }

        private void SetupSetIpButton() {
            this.setIpButton.Text = "Set IP";
            this.setIpButton.AutoSize = true;
            this.setIpButton.TextAlign = ContentAlignment.MiddleCenter;
            this.setIpButton.BackColor = Color.FromArgb(255, 30, 168, 232);
            this.setIpButton.Font = new Font("Verdana", 14);
            this.setIpButton.Location = new Point((guiWidth / 2) - (this.setIpButton.Width / 2), (guiHeight / 2) + 60);
        }

        private void SetupLoadProfileButton() {
            this.loadProfileButton.Text = "Load Emotiv profiles";
            this.loadProfileButton.AutoSize = true;
            this.loadProfileButton.TextAlign = ContentAlignment.MiddleCenter;
            this.loadProfileButton.BackColor = Color.FromArgb(255, 30, 168, 232);
            this.loadProfileButton.Font = new Font("Verdana", 14);
            this.loadProfileButton.Location = new Point((guiWidth / 2) - this.loadProfileButton.Width - 26, (guiHeight / 2) + 100);
        }
        
        private void OnSetIPButtonClick(object sender, EventArgs e) {
            var eventArgs = new SetIPEventArgs();

            if (ipAddressValidator.ValidateIPAddress(ipInputTextBox.Text)) {
                eventArgs.Ip = ipInputTextBox.Text;
                SetIPEvent?.Invoke(this, eventArgs);
                ipInputTextBox.Text = "";
                SetupIpValidationLabel();
            }
            else {
                SetupIpValidationLabelError();
            }
        }
        
        private void OnLoadProfilesButtonClick(object sender, EventArgs eventArgs) {
            LoadProfileGUI loadProfileGui = new LoadProfileGUI();
            loadProfileGui.Show();
            this.Hide();
        }

        private void SetupIpValidationLabel() {
            this.ipValidationLabel.Text = "IP-Address was successfully set";
            this.ipValidationLabel.Font = new Font("Verdana", 14);
            this.ipValidationLabel.ForeColor = Color.FromArgb(255, 40, 156, 71);
            this.ipValidationLabel.AutoSize = true;
            this.ipValidationLabel.Left = (guiWidth / 2) - (ipValidationLabel.Width / 2);
            this.ipValidationLabel.Top = (guiHeight / 2) - (ipValidationLabel.Height / 2) + 40;
        }

        private void SetupIpValidationLabelError() {
            this.ipValidationLabel.Text = "IP-Address is not a valid IP-Address";
            this.ipValidationLabel.Font = new Font("Verdana", 14);
            this.ipValidationLabel.ForeColor = Color.FromArgb(255, 235, 26, 26);
            this.ipValidationLabel.AutoSize = true;
            this.ipValidationLabel.Left = (guiWidth / 2) - (ipValidationLabel.Width / 2);
            this.ipValidationLabel.Top = (guiHeight / 2) - (ipValidationLabel.Height / 2) + 40;
        }
    }
}