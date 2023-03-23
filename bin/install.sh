#!/bin/bash
fqnfile=`ls -tr -1 $PWD/target/latency-tools*`
echo "fqnfile: $fqnfile"
file=$(basename $fqnfile)
echo "file: $file"
cp -Rp ./target/$file /usr/local/lib/$file

if [ -e /usr/local/lib/latency-tools.jar ] ; then
  echo "cleaning up old jar"
  rm /usr/local/lib/latency-tools.jar
fi
ln -s /usr/local/lib/$file /usr/local/lib/latency-tools.jar

if [ ! -e /usr/local/bin/latreader.sh ] ; then
  cp ./bin/latreader.sh /usr/local/bin/latreader.sh
fi


