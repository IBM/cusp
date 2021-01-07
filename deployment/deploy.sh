#!/usr/bin/env bash

if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
  # decrypt GPG signing private key
  openssl aes-256-cbc -K $encrypted_25a884814f46_key -iv $encrypted_25a884814f46_iv -in deployment/signingkey.asc.enc -out deployment/signingkey.asc -d

  # import secret without adding it to the trust database
  gpg --fast-import deployment/signingkey.asc

  # create pubring
  gpg --keyring=$TRAVIS_BUILD_DIR/pubring.gpg --no-default-keyring --import deployment/signingkey.asc

  # create secring
  gpg --allow-secret-key-import --keyring=$TRAVIS_BUILD_DIR/secring.gpg --no-default-keyring --import deployment/signingkey.asc

  # upload artifacts
  ./gradlew -Psigning.password=$PASSPHRASE uploadArchives
fi