using System;
using System.Drawing;
using System.IO;
using System.Windows.Forms;

namespace EmotivDrivers.GUI {
    public class ConsoleOutputGUI : GUI {
        
        private TextBox consoleOutputTextBox;
        public TextBox ConsoleOutputTextBox {
            get => consoleOutputTextBox;
            set => consoleOutputTextBox = value;
        }
        
        private int consoleOutputTextBoxWidth = 750;
        private int consoleOutputTextBoxHeight = 400;

        private Button stopDriverButton;
        
        public ConsoleOutputGUI() {
            InitComponents();
            
            SubscribeToEvents();
        }

        private void SubscribeToEvents() {
            stopDriverButton.Click += new EventHandler(OnStopDriverButtonClick);
        }
        
        private void InitComponents() {
            this.consoleOutputTextBox = new TextBox();
            this.stopDriverButton = new Button();

            this.Location = this.Location;
            this.StartPosition = FormStartPosition.Manual;
            this.FormClosing += delegate { this.Show(); };
            this.ClientSize = new Size(guiWidth, guiHeight);

            this.Text = "Emotiv driver output";
            
            SetupConsoleOutputComponents();
            SetupStopDriverButton();
            
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
            this.Controls.Add(stopDriverButton);
        }
        
        private void SetupConsoleOutputComponents() {
            this.consoleOutputTextBox.ReadOnly = true;
            this.consoleOutputTextBox.Location = new Point((guiWidth / 2) - (consoleOutputTextBoxWidth / 2), (guiHeight / 2) - consoleOutputTextBoxHeight / 2 - 20);
            this.consoleOutputTextBox.MinimumSize = new Size(consoleOutputTextBoxWidth, consoleOutputTextBoxHeight);
            this.consoleOutputTextBox.Font = new Font("", 12);
            this.consoleOutputTextBox.Multiline = true;
            this.consoleOutputTextBox.ScrollBars = ScrollBars.Both;
        }

        private void SetupStopDriverButton() {
            this.stopDriverButton.Text = "Stop Drivers";
            this.stopDriverButton.ForeColor = Color.White;
            this.stopDriverButton.AutoSize = true;
            this.stopDriverButton.TextAlign = ContentAlignment.MiddleCenter;
            this.stopDriverButton.BackColor = Color.FromArgb(255, 235, 26, 26);
            this.stopDriverButton.Font = new Font("Verdana", 14);
            this.stopDriverButton.Location = new Point((guiWidth / 2) - stopDriverButton.Size.Width, (guiHeight / 2) + 185);
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

        private void OnStopDriverButtonClick(object sender, EventArgs eventArgs) {
            Application.Exit();
        }

        protected override void OnFormClosing(FormClosingEventArgs e) {
            base.OnFormClosing(e);
            Application.Exit();
        }
    }
}