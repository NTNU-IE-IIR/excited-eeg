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
    
    public class ApplicaitonGUI : Form {
        
        /// <summary>
        /// --------------------------- VARIABLES ---------------------------
        /// </summary>
        private int guiWidth = 800;
        private int guiHeight = 450;
        
        private TextBox ipInputTextBox;
        private int ipTextBoxWidth = 300;
        private int ipTextBoxHeight = 30;
        
        private string ipTextBoxValue;
        public string TextBoxValue {
            get => ipTextBoxValue;
            set => ipTextBoxValue = value;
        }

        private TextBox consoleOutputTextBox;
        private int consoleOutputTextBoxWidth = 750;
        private int consoleOutputTextBoxHeight = 400;

        private string consoleOutputtextBoxValue;
        public TextBox ConsoleOutputTextBox {
            get => consoleOutputTextBox;
            set => consoleOutputTextBox = value;
        }

        private Label ipLabel;

        private Button setIpButton;
        private Button startDriverButton;
        
        private HeadsetComm.HeadsetComm headsetComm;
        
        /// <summary>
        /// --------------------------- EVENTS ---------------------------
        /// </summary>
        public static event EventHandler<SetIPEventArgs> SetIPEvent;
        
        /// <summary>
        /// --------------------------- CONSTRUCTORS ---------------------------
        /// </summary>
        public ApplicaitonGUI() {
            headsetComm = new HeadsetComm.HeadsetComm();
            
            InitComponents();
            
            SubscribeToEvents();
        }
        
        /// <summary>
        /// --------------------------- METHODS ---------------------------
        /// </summary>
        private void SubscribeToEvents() {
            setIpButton.Click += new EventHandler(OnButtonClick);
            startDriverButton.Click += new EventHandler(OnConnectionButtonClick);
        }
        
        private void InitComponents() {
            this.ipInputTextBox = new TextBox();
            this.ipLabel = new Label();
            this.setIpButton = new Button();
            this.startDriverButton = new Button();
            this.SuspendLayout();

            SetupIpInputTextBox();
            SetupIpLabel();
            SetupSetIpButton();
            SetupStartDriverButton();
            
            // Set application icon
            using (var stream = File.OpenRead("Resources/ntnu.ico")) {
                this.Icon = new Icon(stream);
            }

            Text = "Emotiv drivers";
            ClientSize = new Size(guiWidth, guiHeight);
            this.Controls.Add(this.ipInputTextBox);
            this.Controls.Add(this.ipLabel);
            this.Controls.Add(this.setIpButton);
            this.Controls.Add(this.startDriverButton);
            this.ResumeLayout(false);
            this.PerformLayout();
            CenterToScreen();
            
            // To make the GUI unable to resize
            this.FormBorderStyle = FormBorderStyle.FixedSingle;
            this.MaximizeBox = false;
            this.MinimizeBox = false;
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
        
        private void OnButtonClick(object sender, EventArgs e) {
            var eventArgs = new SetIPEventArgs();
            eventArgs.Ip = ipInputTextBox.Text;
            SetIPEvent?.Invoke(this, eventArgs);
            ipInputTextBox.Text = "";
        }

        private void OnConnectionButtonClick(object sender, EventArgs eventArgs) {
            StartConsoleOutputForm();
            
            //Start in new thread to not freeze up GUI
            Thread thread = new Thread(headsetComm.StartHeadsetCommunications);
            thread.Start();
        }

        private void StartConsoleOutputForm() {
            var form = new Form();
            this.consoleOutputTextBox = new TextBox();

            form.Location = this.Location;
            form.StartPosition = FormStartPosition.Manual;
            form.FormClosing += delegate { this.Show(); };
            form.ClientSize = new Size(guiWidth, guiHeight);

            form.Text = "Emotiv driver output";
            
            InitConsoleOutputComponents();
            
            // Set application icon
            using (var stream = File.OpenRead("Resources/ntnu.ico")) {
                form.Icon = new Icon(stream);
            }

            using (var consoleWriter = new ConsoleWriter()) {
                consoleWriter.WriteEvent += ConsoleWriterWriteEvent;
                consoleWriter.WriteLineEvent += ConsoleWriterWriteLineEvent;
                Console.SetOut(consoleWriter);
            }

            // To make the GUI unable to resize
            form.FormBorderStyle = FormBorderStyle.FixedSingle;
            form.MaximizeBox = false;
            form.MinimizeBox = false;
            
            form.Controls.Add(consoleOutputTextBox);
            
            form.Show();
            this.Hide();
        }

        private void InitConsoleOutputComponents() {
            this.consoleOutputTextBox.ReadOnly = true;
            this.consoleOutputTextBox.Location = new Point((guiWidth / 2) - (consoleOutputTextBoxWidth / 2), (guiHeight / 2) - consoleOutputTextBoxHeight / 2);
            this.consoleOutputTextBox.MinimumSize = new Size(consoleOutputTextBoxWidth, consoleOutputTextBoxHeight);
            this.consoleOutputTextBox.Font = new Font("", 12);
            this.consoleOutputTextBox.Multiline = true;
            this.consoleOutputTextBox.ScrollBars = ScrollBars.Both;
        }

        private void ConsoleWriterWriteEvent(object sender, ConsoleWriterEventArgs eventArgs) {
            this.consoleOutputTextBox.Text += eventArgs.Value;
        }

        private void ConsoleWriterWriteLineEvent(object sender, ConsoleWriterEventArgs eventArgs) {
            this.consoleOutputTextBox.Text += eventArgs.Value + Environment.NewLine;
        }
    }
}