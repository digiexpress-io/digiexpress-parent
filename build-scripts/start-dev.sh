#!/usr/bin/env bash

PUBLIC_FILE=./public
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
  
  read -p "Press [Enter] to continue" 

  cp ./.demo/index.tsx $INDEX_FILE
  cp ./.demo/react-app-env.d.ts $TS_FILE
  rm -r $PUBLIC_FILE
  mkdir $PUBLIC_FILE
  cp -R ./.demo/public/* $PUBLIC_FILE
fi
