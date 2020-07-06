# System Input Driver

This driver redirects the output from the keybaord into the host operating system's input system. 


## Installation

To connect the keybaord's output to the operating system's input system, simply run this driver and pass in the WebSocket URL 
of the keybord's websocket output interface as a single command line argument to the driver. 

If you compile the driver into a jar, you run it like this:
```
java -jar systeminput.jar ws://example.com/output
```

If you run it from the IDE, just add the WebSocket URL to the project run configuration.
