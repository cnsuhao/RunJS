## auto build script
export RUNJS_HOME=/home/runjs/runjs
export JAVA_HOME=/home/runjs/jdk1.7.0_10
cd $RUNJS_HOME
$JAVA_HOME/bin/java -cp "$RUNJS_HOME/packages/ant-launcher.jar:$RUNJS_HOME/packages/ant.jar:$JAVA_HOME/lib/tools.jar" org.apache.tools.ant.launch.Launcher -lib $RUNJS_HOME/packages $*
