# Java Named Arguments
This project implements optional keyword arguments, also known as named parameters, in Java. This feature allows a function call to specify the name of the parameter to which a given
argument applies. It also simplifies the use of optional arguments, as it makes it easy to pass only a subset of the needed arguments.

## Compilation
To compile the project and generate a jar, simply execute: <br>

 ``` $ ant ```

You can also specify a phase to be executed (e.g., clean, compile or jar):

 ``` $ ant <optional_phase>```

## Execution
To execute the project with a test file such as test.TestA, execute:

```$ java -jar keyConstructors.jar test.TestA ```

## Organization

The main package only implements this feature for constructors. The extension for standard methods is in the subpackage <i>extended</i>.
To execute the extended version, switch the main class in the <i>build.xml</i> (simply uncomment the commented line and comment the above).
