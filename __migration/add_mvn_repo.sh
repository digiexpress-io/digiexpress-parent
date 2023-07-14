#!/bin/bash

echo "Input repository url?"
read ssh_repo_url

echo "Input repository branch?"
read ssh_repo_branch

echo "Input repository name?"
read ssh_repo_name



echo "Project nature: 'java' or 'typescript'?"
read repo_nature


echo "Using migration config:"
echo "repository will be cloned: $ssh_repo_url"
echo "repository branch: $ssh_repo_branch"
echo "files will be moved here: $repo_nature -- $new_location"

echo "Type 'yes' for continue"
read confirmation


if [ "$confirmation" != 'yes' ]; then
  echo "Cancelling..."
  exit
fi                            

cd cloned_repos
git clone $ssh_repo_url
git checkout $ssh_repo_branch

cd ../../
pwd


repo_location = "__migration/cloned_repos/$ssh_repo_name"
git remote add -f $ssh_repo_name $repo_location
git merge "$ssh_repo_name/$ssh_repo_branch"  --no-commit --allow-unrelated-histories



git_move_command="filename: "
for file_name in $(ls -1a);
do 	    
  if [ "$file_name" == '.git' ] || 
     [ "$file_name" == '__migration' ] || 
  	 [ "$file_name" == 'mvn_setup' ] ||
  	 [ "$file_name" == 'ts_setup' ] ||
  	 [ "$file_name" == 'bazel_setup' ] || 
  	 [ "$file_name" == '.' ] ||
  	 [ "$file_name" == '..' ]; then
    continue                    
  fi                            
  printf "$git_move_command$file_name\n";
done
