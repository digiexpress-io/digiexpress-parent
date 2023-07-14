#!/bin/bash

echo "Input repository url?"
read ssh_repo_url

echo "Input repository branch?"
read ssh_repo_branch

echo "Project nature: 'mvn' or 'ts'?"
read repo_nature



ssh_repo_name=$(basename $ssh_repo_url .git)
repo_location="__migration/cloned_repos/$ssh_repo_name"
echo "Using migration config:"
echo "=============================================="
echo "repository will be cloned: $ssh_repo_url"
echo "repository branch: $ssh_repo_branch"
echo "repository basename: $ssh_repo_name"
echo "repository location: $repo_location"
echo "=============================================="
echo "Type 'yes' for continue"
read confirmation


if [ "$confirmation" != 'yes' ]; then
  echo "Cancelling ..."
  exit
fi                            


cd "cloned_repos"
git clone $ssh_repo_url
git checkout $ssh_repo_branch
cd "../../"

echo "=============================================="
echo "Repo added:"
echo "location: $repo_location",
echo "currently at: $(pwd)"
echo "=============================================="
echo "Type 'yes' for continue"
read confirmation


if [ "$confirmation" != 'yes' ]; then
  echo "Cancelling ..."
  exit
fi                            



git remote add -f $ssh_repo_name $repo_location
git merge "$ssh_repo_name/$ssh_repo_branch"  --no-commit --allow-unrelated-histories



git_move_command="file to be moved: "
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
