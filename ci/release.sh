#!/bin/bash

RELEASECMD='./gradlew leveldb:bintrayUpload'

if [[ ! -z "$TRAVIS_TAG" ]]
then
  echo "Performing release since this build is for a git tag ($TRAVIS_TAG)."
  echo
  
  echo "$RELEASECMD"
  eval "$RELEASECMD"
else
  echo "Not performing release since this build is not for a git tag."
fi
