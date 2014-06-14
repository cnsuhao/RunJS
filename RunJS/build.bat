@echo off
java -cp "packages/ant-launcher.jar;packages/ant.jar;%JAVA_HOME%/lib/tools.jar" org.apache.tools.ant.launch.Launcher %1 %2 %3 %4 %5