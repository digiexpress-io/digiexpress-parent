#!/bin/bash

echo "Input repository url?"
read ssh_repo_url

echo "Input repository branch?"
read ssh_repo_branch

echo "Project nature: 'mvn' or 'ts'?"
read repo_nature

if [ "$repo_nature" != 'mvn' ] && [ "$repo_nature" != 'ts' ]; then
  echo "unsupported option ... $repo_nature"
  exit
fi


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

new_file_location=""
if [ "$repo_nature" == 'mvn' ]; then
  new_file_location="mvn_setup/$ssh_repo_name"
fi
if [ "$repo_nature" == 'ts' ]; then
  new_file_location="ts_setup/$ssh_repo_name"
fi

mkdir -p "$new_file_location"

echo "=============================================="
echo "Repo added:"
echo "location: $repo_location"
echo "currently at: $(pwd)"
echo "migrating to: $new_file_location"
echo "=============================================="
echo "Type 'yes' for continue"
read confirmation


if [ "$confirmation" != 'yes' ]; then
  echo "Cancelling ..."
  exit
fi                            



git remote add $ssh_repo_name $ssh_repo_url
git fetch $ssh_repo_name
git merge "$ssh_repo_name/$ssh_repo_branch" --allow-unrelated-histories
# --no-commit 


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
  new_loc="$new_file_location/$file_name"
  
  echo "file to be moved: $file_name - $new_loc"
  git mv $file_name $new_loc 
done




git add $new_file_location
git commit -am "repo: '$ssh_repo_url', branch: '$ssh_repo_branch' migrated to: '$new_file_location'"



