# About #
This is the Jerasure project, by James S. Plank, Scott Simmerman, and
Catherine D. Schuman.  More details can be found at
[http://web.eecs.utk.edu/~plank/plank/papers/CS-08-627.html](http://web.eecs.utk.edu/~plank/plank/papers/CS-08-627.html)

See technical report CS-08-627 for a description of the code.  

The directories are as follows:

* The src directory contains the jerasure code.
* The Examples directory contains the example programs. 
* The jni folder contains the native (C++) part of the JNI coupling (added by jvandertil)
* The java directory contains the Java sources for the JNI coupling (added by jvandertil)
* The win32 directory contains a Visual Studio 2012 solution for building on Windows. (added by jvandertil)

## Hints ##
* It is assumed that GNU make is being used.
* When using the JNI coupling, be sure to include the JAR file in your java.library.path. 
  * You can also set the library path using: java -Djava.library.path={path}
* Make sure the path to the Java headers is correct, the path can be found in jni/Makefile and the properties page of JErasure.JNI in the Visual Studio solution.
