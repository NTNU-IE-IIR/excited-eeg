using System;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Windows.Forms;

namespace EmotivDrivers.GUI {
    public class SetIPEventArgs : EventArgs {
        public string Ip { get; set; }
    }
    
    public class ApplicaitonGUI : Form {

        private int guiWidth = 800;
        private int guiHeight = 450;
        
        private TextBox textBox;
        private int textBoxWidth = 300;
        private int textBoxHeight = 30;

        private string textBoxValue;
        public string TextBoxValue {
            get => textBoxValue;
            set => textBoxValue = value;
        }

        private Label label;

        private Button button;
        private Button connectButton;

        public static bool autoStart;

        public static event EventHandler<SetIPEventArgs> SetIPEvent;

        public ApplicaitonGUI() {
            InitComponents();
            
            button.Click += new EventHandler(OnButtonClick);
            connectButton.Click += new EventHandler(OnConnectionButtonClick);
        }
        
        private void InitComponents() {
            this.textBox = new TextBox();
            this.label = new Label();
            this.button = new Button();
            this.connectButton = new Button();
            this.SuspendLayout();

            this.textBox.AcceptsReturn = true;
            this.textBox.AcceptsTab = true;
            this.textBox.Location = new Point((guiWidth / 2) - (textBoxWidth / 2), (guiHeight / 2) - (textBoxHeight / 2));
            this.textBox.MinimumSize = new Size(textBoxWidth, textBoxHeight);
            this.textBox.Font = new Font("Verdana", 14);
            this.textBox.TextAlign = HorizontalAlignment.Center;
            this.textBox.Multiline = true;
            this.textBox.ScrollBars = ScrollBars.Vertical;

            this.label.Text = "Input IP-address of device running Keyboard App";
            this.label.AutoSize = true;
            this.label.Font = new Font("Verdana", 14);
            this.label.Location = new Point((guiWidth / 2) - (label.Size.Width + 135), (guiHeight / 2) - (label.Size.Height / 2) - 30);

            this.button.Text = "Set IP";
            this.button.AutoSize = true;
            this.button.TextAlign = ContentAlignment.MiddleCenter;
            this.button.BackColor = Color.DodgerBlue;
            this.button.Font = new Font("Verdana", 14);
            this.button.Location = new Point((guiWidth / 2) + (button.Size.Width), (guiHeight / 2) + 20);

            this.connectButton.Text = "Start Emotiv drivers";
            this.connectButton.AutoSize = true;
            this.connectButton.TextAlign = ContentAlignment.MiddleCenter;
            this.connectButton.BackColor = Color.DodgerBlue;
            this.connectButton.Font = new Font("Verdana", 14);
            this.connectButton.Location = new Point((guiWidth / 2) + (button.Size.Width), (guiHeight / 2) + 60);

            using (var stream = File.OpenRead("Resources/ntnu.ico")) {
                this.Icon = new Icon(stream);
            }

            Text = "Emotiv drivers";
            ClientSize = new Size(guiWidth, guiHeight);
            this.Controls.Add(this.textBox);
            this.Controls.Add(this.label);
            this.Controls.Add(this.button);
            this.Controls.Add(this.connectButton);
            this.ResumeLayout(false);
            this.PerformLayout();
            CenterToScreen();
            
            // To make the GUI unable to resize
            this.FormBorderStyle = FormBorderStyle.FixedSingle;
            this.MaximizeBox = false;
            this.MinimizeBox = false;
        }
        
        private void OnButtonClick(object sender, EventArgs e) {
            var eventArgs = new SetIPEventArgs();
            eventArgs.Ip = textBox.Text;
            SetIPEvent?.Invoke(this, eventArgs);
            textBox.Text = "";
        }

        private void OnConnectionButtonClick(object sender, EventArgs eventArgs) {
            
        }
    }
}