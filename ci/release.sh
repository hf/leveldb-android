#!/bin/bash

RELEASECMD='./gradlew leveldb:bintrayUpload'

if [ "$TRAVIS_BRANCH" = "master" -a "$TRAVIS_PULL_REQUEST" = "false" ]
then
  echo "Performing release since on master and not a pull request."
  echo
  
  echo "$RELEASECMD"
  eval "$RELEASECMD"
else
  echo "Not performing release since on branch '$TRAVIS_BRANCH' or a pull request ($TRAVIS_PULL_REQUEST)."
fi
