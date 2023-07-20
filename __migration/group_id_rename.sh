#!/bin/bash

echo "Input current group id?"
read current_group_id

new_group_id="io.digiexpress"

for file_name in $(find ./ -name "pom.xml");
do 	    
 
  echo $file_name
  expression="s/<groupId>$current_group_id<\/groupId>/<groupId>$new_group_id<\/groupId>/g"
  sed -i $expression $file_name
 
done




