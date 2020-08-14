using System.Drawing;
using System.IO;
using System.Windows.Forms;

namespace EmotivDrivers.GUI {
    public class GUI : Form {
        
        /// <summary>
        /// --------------------------- VARIABLES ---------------------------
        /// </summary>
        protected int guiWidth = 800;
        protected int guiHeight = 450;
        
        /// <summary>
        /// --------------------------- CONSTRUCTORS ---------------------------
        /// </summary>
        protected GUI() {
            InitComponents();
        }
        
        /// <summary>
        /// --------------------------- METHODS ---------------------------
        /// </summary>
        private void InitComponents() {
            this.SuspendLayout();

            // Set application icon
            using (var stream = File.OpenRead("Resources/ntnu.ico")) {
                this.Icon = new Icon(stream);
            }
            
            ClientSize = new Size(guiWidth, guiHeight);
            
            this.ResumeLayout(false);
            this.PerformLayout();
            CenterToScreen();
            
            // To make the GUI unable to resize
            this.FormBorderStyle = FormBorderStyle.FixedSingle;
            this.MaximizeBox = false;
            this.MinimizeBox = false;
        }
    }
}