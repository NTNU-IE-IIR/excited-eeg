using System;
using System.Drawing;
using System.IO;
using System.Threading;
using System.Windows.Forms;
using EmotivDrivers.HeadsetComm;

namespace EmotivDrivers.GUI {
    public class SetIPEventArgs : EventArgs {
        public string Ip { get; set; }
    }
    
    public class SetIPGUI : GUI {
        
        /// <summary>
        /// --------------------------- VARIABLES ---------------------------
        /// </summary>

        private TextBox ipInputTextBox;
        private int ipTextBoxWidth = 300;
        private int ipTextBoxHeight = 30;
        
        private string ipTextBoxValue;
        public string TextBoxValue {
            get => ipTextBoxValue;
            set => ipTextBoxValue = value;
        }
        
        private Label ipLabel;

        private Button setIpButton;
        private Button startDriverButton;
        
        private HeadsetComm.HeadsetComm headsetComm;

        private Thread headsetCommThread;
        
        /// <summary>
        /// --------------------------- EVENTS ---------------------------
        /// </summary>
        public static event EventHandler<SetIPEventArgs> SetIPEvent;
        
        /// <summary>
        /// --------------------------- CONSTRUCTORS ---------------------------
        /// </summary>
        public SetIPGUI() {
            headsetComm = new HeadsetComm.HeadsetComm();
            
            InitComponents();
            
            SubscribeToEvents();
        }
        
        /// <summary>
        /// --------------------------- METHODS ---------------------------
        /// </summary>
        private void SubscribeToEvents() {
            setIpButton.Click += new EventHandler(OnSetIPButtonClick);
            startDriverButton.Click += new EventHandler(OnConnectionButtonClick);
        }
        
        private void InitComponents() {
            this.ipInputTextBox = new TextBox();
            this.ipLabel = new Label();
            this.setIpButton = new Button();
            this.startDriverButton = new Button();
            
            SetupIpInputTextBox();
            SetupIpLabel();
            SetupSetIpButton();
            SetupStartDriverButton();
            
            Text = "Emotiv drivers";
            
            this.Controls.Add(this.ipInputTextBox);
            this.Controls.Add(this.ipLabel);
            this.Controls.Add(this.setIpButton);
            this.Controls.Add(this.startDriverButton);
        }

        private void SetupIpInputTextBox() {
            this.ipInputTextBox.AcceptsReturn = true;
            this.ipInputTextBox.AcceptsTab = true;
            this.ipInputTextBox.Location = new Point((guiWidth / 2) - (ipTextBoxWidth / 2), (guiHeight / 2) - (ipTextBoxHeight / 2));
            this.ipInputTextBox.MinimumSize = new Size(ipTextBoxWidth, ipTextBoxHeight);
            this.ipInputTextBox.Font = new Font("Verdana", 14);
            this.ipInputTextBox.TextAlign = HorizontalAlignment.Center;
            this.ipInputTextBox.Multiline = true;
            this.ipInputTextBox.ScrollBars = ScrollBars.Vertical;
        }

        private void SetupIpLabel() {
            this.ipLabel.Text = "Input IP-address of device running Keyboard App";
            this.ipLabel.AutoSize = true;
            this.ipLabel.Font = new Font("Verdana", 14);
            this.ipLabel.Location = new Point((guiWidth / 2) - (ipLabel.Size.Width + 135), (guiHeight / 2) - (ipLabel.Size.Height / 2) - 30);
        }

        private void SetupSetIpButton() {
            this.setIpButton.Text = "Set IP";
            this.setIpButton.AutoSize = true;
            this.setIpButton.TextAlign = ContentAlignment.MiddleCenter;
            this.setIpButton.BackColor = Color.DodgerBlue;
            this.setIpButton.Font = new Font("Verdana", 14);
            this.setIpButton.Location = new Point((guiWidth / 2) + (setIpButton.Size.Width), (guiHeight / 2) + 20);
        }

        private void SetupStartDriverButton() {
            this.startDriverButton.Text = "Start Emotiv drivers";
            this.startDriverButton.AutoSize = true;
            this.startDriverButton.TextAlign = ContentAlignment.MiddleCenter;
            this.startDriverButton.BackColor = Color.DodgerBlue;
            this.startDriverButton.Font = new Font("Verdana", 14);
            this.startDriverButton.Location = new Point((guiWidth / 2) + (setIpButton.Size.Width), (guiHeight / 2) + 60);
        }
        
        private void OnSetIPButtonClick(object sender, EventArgs e) {
            var eventArgs = new SetIPEventArgs();
            eventArgs.Ip = ipInputTextBox.Text;
            SetIPEvent?.Invoke(this, eventArgs);
            ipInputTextBox.Text = "";
        }

        private void OnConnectionButtonClick(object sender, EventArgs eventArgs) {
            ConsoleOutputGUI consoleOutputGui = new ConsoleOutputGUI();
            consoleOutputGui.Owner = this;
            consoleOutputGui.Disposed += ChildFormClosed;
            consoleOutputGui.Show();
            this.Hide();
            
            //Start headset communication in new thread to not freeze up GUI
            headsetCommThread = new Thread(headsetComm.StartHeadsetCommunications);
            headsetCommThread.Start();
        }

        private void ChildFormClosed(object sender, EventArgs eventArgs) {
            this.Close();
        }
    }
}