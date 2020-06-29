
# Keyboard Backend

This is the backend project that implementes the typing logic for the keyboard. The is created as a reusable library and is intended to be included
in other projects that has use of its functionality. The backend does not implement any user interface, it only implements the logic needed for typing.

## Installation

If you want to use this library in your application, you have to install it to you local maven repository by using the following command:

For MacOS and Linux:
```
mvn install
```
For Windows:
```
mvnw install
```

If the command complains about failing test, you can add the "-DskipTests" switch to skip the tests, like this:
```
mvn install -DskipTests
```

After installing the project, you can include it in your project by adding the following in your maven pom.xml file:

```
<dependency>
    <groupId>no.ntnu.stud.avikeyb</groupId>
    <artifactId>backend</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```


## Documentation

A brief introduction about how to use the project can be found in the [wiki](https://github.com/accessible-virtual-keyboard/backend/wiki) 








