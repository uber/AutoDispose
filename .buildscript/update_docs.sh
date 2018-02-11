#!/usr/bin/env bash

echo "Cloning osstrich..."
mkdir tmp
cd tmp
git clone git@github.com:square/osstrich.git
cd osstrich
echo "Packaging..."
mvn package
echo "Running..."
rm -rf tmp/autodispose && java -jar target/osstrich-cli.jar tmp/autodispose git@github.com:uber/autodispose.git com.uber.autodispose
echo "Cleaning up..."
cd ../..
rm -rf tmp
echo "Finished!"
