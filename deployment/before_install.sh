#!/usr/bin/env bash

echo Decrypting GPG signing private key
openssl aes-256-cbc -K $encrypted_25a884814f46_key -iv $encrypted_25a884814f46_iv -in deployment/signingkey.asc.enc -out deployment/signingkey.asc -d

echo Creating pubring
gpg --keyring=$TRAVIS_BUILD_DIR/deployment/pubring.gpg --no-default-keyring --import deployment/signingkey.asc

echo Creating secring
gpg --allow-secret-key-import --keyring=$TRAVIS_BUILD_DIR/deployment/secring.gpg --no-default-keyring --import deployment/signingkey.asc
