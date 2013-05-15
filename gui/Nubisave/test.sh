#!/bin/sh

# FIXME: This doesn't work
# => java.lang.NoClassDefFoundError: net/contentobjects/jnotify/JNotifyListener
# => java.lang.ClassNotFoundException: net.contentobjects.jnotify.JNotifyListener
# java -Djava.library.path=lib -jar dist/NubiSave_Configuration_GUI.jar /tmp

cp dist/NubiSave_Configuration_GUI.jar ../../bin/_GUI-Test.jar
cd ../../bin
java -Djava.library.path=lib -jar _GUI-Test.jar /tmp
