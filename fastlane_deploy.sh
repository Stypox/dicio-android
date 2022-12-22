#!/bin/sh
set -e
file="fastlane_config.txt"

while echo "Reading config from $file"; do
    read -r signing_store_file
    read -r signing_key_alias
    read -r play_store_json_key
    break
done < $file

echo "Using signing_store_file:$signing_store_file"
echo "Using signing_key_alias:$signing_key_alias"
echo "Using play_store_json_key:$play_store_json_key"

echo "Enter signing_store_password: "
read -s signing_store_password
echo "Enter signing_key_password: "
read -s signing_key_password

fastlane deploy signing_store_file:$signing_store_file signing_store_password:$signing_store_password signing_key_alias:$signing_key_alias signing_key_password:$signing_key_password play_store_json_key:$play_store_json_key
