#!/bin/bash
fqnfile=`ls -tr -1 $PWD/target/latency-tools*`
DST=/usr/local/
if [ "$1" != "" ] ; then
  if [ -e $1 ] ; then
    DST=$1
    echo "local install to: $DST"
  else
    mkdir $1
    DST=$1
    echo "local install to: $DST"
  fi
fi


echo "fqnfile: $fqnfile"
file=$(basename $fqnfile)
echo "file: $file"

if [ ! -e $DST/lib ] ; then
  echo "creating dir: $DST/lib"
  mkdir $DST/lib
fi
cp -Rp ./target/$file $DST/lib/$file

if [ -e /usr/local/lib/latency-tools.jar ] ; then
  echo "cleaning up old jar"
  rm $DST/lib/latency-tools.jar
fi
ln -s $DST/lib/$file $DST/lib/latency-tools.jar

if [ ! -e $DST/bin ] ; then
  echo "creating dir $DST/bin"
  mkdir $DST/bin
fi

if [ ! -e $DST/bin/latreader.sh ] ; then
  cp ./bin/latreader.sh $DST/bin/latreader.sh
fi


if [ ! -e $DST/etc ] ; then
  echo "creating $DST/etc"
  mkdir $DST/etc
fi
cp ./etc/lattools.xml $DST/etc
