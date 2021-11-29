#!/usr/bin/env bash

PUBLIC_FILE=./public
TEST_DIR=./src/test_app
INDEX_FILE=./src/index.tsx
TS_FILE=./src/react-app-env.d.ts

if [[ -f "$INDEX_FILE" ]]; then
  echo "Demo start up exists, delete: '$INDEX_FILE' if you want them to be recreated"
  read -p "Press [Enter] to continue"
else
  echo "Preparing demo-envir sources"
  echo "Coping start files from './.demo' folder"
  echo " - '$INDEX_FILE'"
  echo " - '$PUBLIC_FILE'"
  echo " - '$TS_FILE'"
  echo " - '$TEST_DIR'"
  
  read -p "Press [Enter] to continue" 

  cp ./.build-scripts/demo/index.tsx $INDEX_FILE
  cp ./.build-scripts/demo/index.tsx $INDEX_FILE
  rm -r $PUBLIC_FILE
  mkdir $PUBLIC_FILE
  mkdir $TEST_DIR
  cp -R ./.build-scripts/demo/test_app/* $TEST_DIR
  cp -R ./.build-scripts/demo/public/* $PUBLIC_FILE
fi
