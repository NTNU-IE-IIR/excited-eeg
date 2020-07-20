using System;
using System.Drawing;
using System.Windows.Forms;

namespace EmotivDrivers.GUI {
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

        public ApplicaitonGUI() {
            InitComponents();
        }

        private void InitComponents() {
            this.textBox = new TextBox();
            this.label = new Label();
            this.button = new Button();
            this.SuspendLayout();

            this.textBox.AcceptsReturn = true;
            this.textBox.AcceptsTab = true;
            this.textBox.Location = new Point((guiWidth / 2) - (textBoxWidth / 2), (guiHeight / 2) - (textBoxHeight / 2));
            this.textBox.MinimumSize = new Size(textBoxWidth, textBoxHeight);
            this.textBox.Font = new Font("Comic sans MS", 14);
            this.textBox.TextAlign = HorizontalAlignment.Center;
            this.textBox.Multiline = true;
            this.textBox.ScrollBars = ScrollBars.Vertical;

            this.label.Text = "Input IP-address of device running Keyboard App";
            this.label.AutoSize = true;
            this.label.Font = new Font("Comic sans MS", 14);
            this.label.Location = new Point((guiWidth / 2) - (label.Size.Width * 2), (guiHeight / 2) - (label.Size.Height / 2) - 30);

            this.button.Text = "Set IP";
            this.button.AutoSize = true;
            this.button.TextAlign = ContentAlignment.MiddleCenter;
            this.button.BackColor = Color.Red;
            this.button.ForeColor = Color.Blue;
            this.button.Font = new Font("Comic sans MS", 14);
            this.button.Location = new Point((guiWidth / 2) + (button.Size.Width), (guiHeight / 2) + 20);
            
            
            Text = "Emotiv drivers";
            ClientSize = new Size(guiWidth, guiHeight);
            this.Controls.Add(this.textBox);
            this.Controls.Add(this.label);
            this.Controls.Add(this.button);
            this.ResumeLayout(false);
            this.PerformLayout();
            CenterToScreen();
        }

        [STAThread]
        static void Main() {
            Application.EnableVisualStyles();
            Application.Run(new ApplicaitonGUI());
        }
    }
}