using System.Net;
using System.Net.Sockets;

namespace EmotivDrivers.ApplicationConnection {
    
    /// <summary>
    /// Used as a quick and simple way to validate ip-addresses
    /// </summary>
    public class IPAddressValidator {
        
        /// <summary>
        /// Checks if a given string is either a valid IP-address
        /// The IP-address can be either IPv4 or IPv6
        /// </summary>
        /// <param name="ipString"></param>
        /// <returns></returns>
        public bool ValidateIPAddress(string ipString) {
            IPAddress address;
            bool validAddress = false;
            
            if (IPAddress.TryParse(ipString, out address)) {
                switch (address.AddressFamily) {
                    case AddressFamily.InterNetwork:
                        validAddress = true;
                        break;
                    
                    case AddressFamily.InterNetworkV6:
                        validAddress = true;
                        break;
                    
                    default:
                        validAddress = false;
                        break;
                }
            }

            return validAddress;
        }
    }
}