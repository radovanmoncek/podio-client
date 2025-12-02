echo "compiling ..."
start /b /wait compile.bat
echo "packaging ..."
jar.exe --create --file "podio-api-client-22.jar" --module-path . --module-version 1.0 -C main\ module-info.class main\podio\*.class
