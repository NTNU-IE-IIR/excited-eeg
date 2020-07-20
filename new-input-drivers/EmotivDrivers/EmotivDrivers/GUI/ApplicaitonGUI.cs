using System;
using System.Drawing;
using System.Windows.Forms;

namespace EmotivDrivers.GUI {
    public class ApplicaitonGUI : Form {

        public ApplicaitonGUI() {
            InitComponents();
        }

        private void InitComponents() {
            Text = "Emotiv drivers";
            ClientSize = new Size(800, 450);
            CenterToScreen();
        }

        [STAThread]
        static void Main() {
            Application.EnableVisualStyles();
            Application.Run(new ApplicaitonGUI());
        }
    }
}