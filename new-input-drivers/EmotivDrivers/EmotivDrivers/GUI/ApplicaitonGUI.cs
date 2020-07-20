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

        public ApplicaitonGUI() {
            InitComponents();
        }

        private void InitComponents() {
            this.textBox = new TextBox();
            this.SuspendLayout();

            this.textBox.AcceptsReturn = true;
            this.textBox.AcceptsTab = true;
            this.textBox.Location = new Point((guiWidth / 2) - (textBoxWidth / 2), (guiHeight / 2) - (textBoxHeight / 2));
            this.textBox.MinimumSize = new Size(textBoxWidth, textBoxHeight);
            this.textBox.Font = new Font("Comic sans MS", 14);
            this.textBox.TextAlign = HorizontalAlignment.Center;
            this.textBox.Multiline = true;
            this.textBox.ScrollBars = ScrollBars.Vertical;
            
            Text = "Emotiv drivers";
            ClientSize = new Size(guiWidth, guiHeight);
            this.Controls.Add(this.textBox);
            this.ResumeLayout(false);
            this.PerformLayout();
            CenterToScreen();
            
            Console.WriteLine(textBox.Size);
        }

        [STAThread]
        static void Main() {
            Application.EnableVisualStyles();
            Application.Run(new ApplicaitonGUI());
        }
    }
}