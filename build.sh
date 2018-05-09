echo "Building..."
if [ -d "bin" ]; then
  rm -rf bin/*
  rmdir bin
fi

rm cabolabs-loadehr.jar
mkdir bin

cd src
find $PWD | grep groovy > ../tmpsources.txt
cd ..
groovyc -cp "./lib/*" -d bin @tmpsources.txt
rm tmpsources.txt

echo "Done!"
