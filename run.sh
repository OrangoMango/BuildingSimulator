rm -r bin/*
cd src
echo "compiling..."
javac -d ../bin -cp .:../lib/* --module-path $1 --add-modules javafx.controls com/orangomango/building/MainApplication.java
cd ../bin
cp -r ../res/* .
echo "executing..."
java -cp .:../lib/* --module-path $1 --add-modules javafx.controls com.orangomango.building.MainApplication
