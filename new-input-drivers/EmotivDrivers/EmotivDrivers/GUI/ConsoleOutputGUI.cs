using System;
using System.Drawing;
using System.IO;
using System.Windows.Forms;

namespace EmotivDrivers.GUI {
    public class ConsoleOutputGUI : GUI {
        
        private TextBox consoleOutputTextBox;
        private int consoleOutputTextBoxWidth = 750;
        private int consoleOutputTextBoxHeight = 400;

        private string consoleOutputtextBoxValue;
        public TextBox ConsoleOutputTextBox {
            get => consoleOutputTextBox;
            set => consoleOutputTextBox = value;
        }
        
        public ConsoleOutputGUI() {
            InitComponents();
        }
        

        private void InitComponents() {
            this.consoleOutputTextBox = new TextBox();

            this.Location = this.Location;
            this.StartPosition = FormStartPosition.Manual;
            this.FormClosing += delegate { this.Show(); };
            this.ClientSize = new Size(guiWidth, guiHeight);

            this.Text = "Emotiv driver output";
            
            InitConsoleOutputComponents();
            
            // Set application icon
            using (var stream = File.OpenRead("Resources/ntnu.ico")) {
                this.Icon = new Icon(stream);
            }

            using (var consoleWriter = new ConsoleWriter()) {
                consoleWriter.WriteEvent += ConsoleWriterWriteEvent;
                consoleWriter.WriteLineEvent += ConsoleWriterWriteLineEvent;
                Console.SetOut(consoleWriter);
            }
            
            
            this.Controls.Add(consoleOutputTextBox);
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
            try {
                GUIUtils.ScrollToBottom(consoleOutputTextBox);
            }
            catch (ObjectDisposedException e) {
                Console.WriteLine(e);
                throw;
            }
        }

        private void ConsoleWriterWriteLineEvent(object sender, ConsoleWriterEventArgs eventArgs) {
            this.consoleOutputTextBox.Text += eventArgs.Value + Environment.NewLine;
            try {
                GUIUtils.ScrollToBottom(consoleOutputTextBox);
            }
            catch (ObjectDisposedException e) {
                Console.WriteLine(e);
                throw;
            }
        }
        
    }
}