using System;
using System.IO;
using System.Text;

namespace EmotivDrivers.GUI {

    public class ConsoleWriterEventArgs : EventArgs {
        public string Value { get; private set; }

        public ConsoleWriterEventArgs(string value) {
            this.Value = value;
        }
    }
    
    public class ConsoleWriter : TextWriter{
        public override Encoding Encoding { get { return Encoding.UTF8; } }

        public EventHandler<ConsoleWriterEventArgs> WriteEvent;
        public EventHandler<ConsoleWriterEventArgs> WriteLineEvent;
        
        public override void Write(string value) {
            if (WriteEvent != null) {
                WriteEvent(this, new ConsoleWriterEventArgs(value));
            }
            base.Write(value);
        }

        public override void WriteLine(string value) {
            if (WriteLineEvent != null) {
                WriteLineEvent(this, new ConsoleWriterEventArgs(value));
            }
            base.WriteLine(value);
        }
    }
}