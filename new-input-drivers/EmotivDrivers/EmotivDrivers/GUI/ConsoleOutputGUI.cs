using System;
using System.Drawing;
using System.IO;
using System.Windows.Forms;

namespace EmotivDrivers.GUI {
    public class ConsoleOutputGUI : GUI {
        
        private Form form = new Form();
        
        private TextBox consoleOutputTextBox;
        private int consoleOutputTextBoxWidth = 750;
        private int consoleOutputTextBoxHeight = 400;

        private string consoleOutputtextBoxValue;
        public TextBox ConsoleOutputTextBox {
            get => consoleOutputTextBox;
            set => consoleOutputTextBox = value;
        }
        
        static ConsoleOutputGUI() {}
        
        public ConsoleOutputGUI() {
            InitComponents();
        }
        
        public static ConsoleOutputGUI Instance { get; } = new ConsoleOutputGUI();

        private void InitComponents() {
            
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
            
            
            form.Controls.Add(consoleOutputTextBox);
        }
        
        private void InitConsoleOutputComponents() {
            this.consoleOutputTextBox.ReadOnly = true;
            this.consoleOutputTextBox.Location = new Point((guiWidth / 2) - (consoleOutputTextBoxWidth / 2), (guiHeight / 2) - consoleOutputTextBoxHeight / 2);
            this.consoleOutputTextBox.MinimumSize = new Size(consoleOutputTextBoxWidth, consoleOutputTextBoxHeight);
            this.consoleOutputTextBox.Font = new Font("", 12);
            this.consoleOutputTextBox.Multiline = true;
            this.consoleOutputTextBox.ScrollBars = ScrollBars.Both;
        }

        public void ShowConsoleOutputForm() {
            form.Show();
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