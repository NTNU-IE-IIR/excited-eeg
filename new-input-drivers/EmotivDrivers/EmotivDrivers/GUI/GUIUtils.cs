using System;
using System.Runtime.InteropServices;
using System.Windows.Forms;

namespace EmotivDrivers.GUI {
    public class GUIUtils {
        
        /// <summary>
        /// --------------------------- VARIABLES ---------------------------
        /// </summary>
        [DllImport("user32.dll", CharSet = CharSet.Auto)]
        private static extern int SendMessage(IntPtr hWnd, int wMsg, IntPtr wParam, IntPtr lParam);

        private const int VM_SCROLL = 0x115;
        private const int SB_BOTTOM = 7;
        
        /// <summary>
        /// --------------------------- METHODS ---------------------------
        /// </summary>
        public static void ScrollToBottom(TextBox textBox) {
            SendMessage(textBox.Handle, VM_SCROLL, (IntPtr) SB_BOTTOM, IntPtr.Zero);
        }
    }
}