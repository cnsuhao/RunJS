## auto build script
#export RUNJS_HOME=/home/runjs/runjs
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home

java -cp "packages/ant-launcher.jar:packages/ant.jar:$JAVA_HOME/lib/tools.jar" org.apache.tools.ant.launch.Launcher -lib $RUNJS_HOME/packages $*