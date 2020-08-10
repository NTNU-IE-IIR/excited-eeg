using System.Collections.Generic;
using System.Drawing;
using System.Windows.Forms;

namespace EmotivDrivers.GUI {
    public class LoadProfileGUI : GUI {

        private List<Button> profileButtons;
        
        public LoadProfileGUI() {
            InitComponents();
        }

        private void InitComponents() {
            SetupProfileButtons();
        }

        private void SetupProfileButtons() {
            Point newLocation = new Point((guiWidth / 2), guiHeight / 2);

            for (int i = 0; i < 4; i++) {
                Button button = new Button();
                button.Size = new Size(50, 20);
                button.Location = newLocation;
                newLocation.Offset(0, button.Height + 5);
                Controls.Add(button);
            }
        }
    }
}