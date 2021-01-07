#!/usr/bin/env bash

if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
  # upload artifacts
  ./gradlew -Psigning.password=$PASSPHRASE uploadArchives
fi