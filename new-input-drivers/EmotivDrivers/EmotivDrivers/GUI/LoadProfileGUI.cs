using System.Collections.Generic;
using System.Drawing;
using System.Windows.Forms;

namespace EmotivDrivers.GUI {
    public class LoadProfileGUI : GUI {

        private List<Button> profileButtons;

        private Panel buttonContainer;
        
        public LoadProfileGUI() {
            InitComponents();
        }

        private void InitComponents() {
            SetupProfileButtons();
        }

        private void SetupProfileButtons() {
            buttonContainer = new Panel();

            buttonContainer.BackColor = Color.Aqua;

            Button button1 = new Button();
            Button button2 = new Button();
            Button button3 = new Button();
            Button button4 = new Button();
            Button button5 = new Button();
            
            button1.Size = new Size(200, 100);
            button2.Size = new Size(200, 100);
            button3.Size = new Size(200, 100);
            button4.Size = new Size(200, 100);
            button5.Size = new Size(200, 100);

            button2.Location = new Point(0, 110);
            button3.Location = new Point(0, 220);
            button4.Location = new Point(0, 330);
            button5.Location = new Point(0, 440);
            
            buttonContainer.AutoSize = true;

            buttonContainer.Controls.Add(button1);
            buttonContainer.Controls.Add(button2);
            buttonContainer.Controls.Add(button3);
            buttonContainer.Controls.Add(button4);
            buttonContainer.Controls.Add(button5);
            
            buttonContainer.Location = new Point(guiWidth / 2 - buttonContainer.Size.Width / 2, 10);

            this.AutoScroll = true;

            this.Controls.Add(buttonContainer);
        }
    }
}