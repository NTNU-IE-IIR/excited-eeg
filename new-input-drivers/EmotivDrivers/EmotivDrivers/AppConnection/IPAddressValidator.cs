using System.Net;
using System.Net.Sockets;

namespace EmotivDrivers.AppConnection {
    
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
                        if (ipString.Length > 6 && ipString.Contains(".")) {
                            string[] split = ipString.Split('.');
                            if (split.Length == 4 && split[0].Length > 0 && split[1].Length > 0 &&
                                split[2].Length > 0 && split[3].Length > 0) {
                                validAddress = true;
                            }
                        }
                        break;
                    
                    case AddressFamily.InterNetworkV6:
                        if (ipString.Contains(":") && ipString.Length > 15) {
                            validAddress = true;
                        }
                        break;
                }
            }

            return validAddress;
        }
    }
}