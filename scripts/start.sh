#!/usr/bin/env bash

if [ "$1" = "add-customer" ] || [ "$1" = "add-worker" ]
then
    read -p "Enter host address of Asgardeo (Required) [https://dev.api.asgardeo.io] : " host
    [ -z "$host" ] && { echo "Error: Host address can't be empty!"; exit 1; }
    read -p "Enter the organization name (Required)  : " tenant
    [ -z "$tenant" ] && { echo "Error: Organization can't be empty!"; exit 1; }
    read -p "Enter username (Required) : " username
    [ -z "$username" ] && { echo "Error: username can't be empty!"; exit 1; }
    stty -echo
    read -p "Enter password (Required) : " password
    [ -z "$password" ] && { password=none; }
    stty echo
    echo
    if [ "$1" = "add-customer" ]
    then
        userStore="CUSTOMER-DEFAULT"
        java -jar $(find . -name "*Scim-Bulk-Import*") "$username" $password "$host" "$tenant" "$userStore"
    else
        userStore="WORK-DEFAULT"
        java -jar $(find . -name "*Scim-Bulk-Import*") "$username" $password "$host" "$tenant" "$userStore"
    fi
else
    echo "Command not found"
fi

