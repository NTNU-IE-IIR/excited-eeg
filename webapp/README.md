# Accessible Virtual Keyboard Web Version

Accessible Virtual Keyboard is a tool to help people with disabilities to communicate. This application should make the users be able to type word and sentences using only simple boolean inputs.

This is a simple web version of the application that has been developed for testing purposes. Input drivers for a few different hardware sensors can be found in the [input drivers repository](https://github.com/accessible-virtual-keyboard/inputdrivers).

## Installation

Before you can build and run the application, you have to install the [keyboard backend project](https://github.com/accessible-virtual-keyboard/backend).


## Running

After staring the application you can access the keyboard on the following URI:

```
http://localhost:8080/
````

## Connecting the input and output drivers

The input and ouput drivers can connect to the web application with the following URIs respectively:
```
ws://localhost:8080/input
ws://localhost:8080/output
```


