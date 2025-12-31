#!/bin/sh

if [ $1 = "help" ]
then
    echo usage: "./pseudo_build.sh <argument>";
    echo;
    echo compile - compile source into .java;
    echo package - package into .jar;
    echo test-compile - compile tests into .java;
    echo test - run tests;
    echo test-debug - debug tests
fi

if [ $1 = "compile" ]
then
    echo compiling ...;
    javac -Xlint:all -g -d .class --target 21 -classpath main/ main/podio/*.java main/module-info.java
fi

if [ $1 = "package" ]
then
    echo packaging ...;
    jar --create --file ".out/podio-api-client.jar" --module-version 1.0 -C .class/ module-info.class .class/podio/*.class
fi

if [ $1 = "test-compile" ]
then
    echo compiling tests ...;
    javac -Xlint:all -g -d .class --target 21 -classpath .class test/*.java
fi

#Inspired by Apache Maven
if [ $1 = "test" ]
then
    echo "running tests ...";
    java -ea -classpath .class TestSuite;
    echo tests exited with $?
fi

if [ $1 = "test-debug" ]
then
    echo "debugging tests ...";
    jdb -classpath .class -sourcepath test;main/podio/ TestSuite
fi
