# setting up UI for wrench only deployment
UI source code can be found under ```digiexpress-parent/ts_setup/eveli-ide```.  
This source code is released to npmjs as: ```@dxs-ts/eveli-ide```.  
It must be embedded as a library to user project(```pnpm add @dxs-ts/eveli-ide```).  
Sample integration can be found under ```digiexpress-parent/ts_setup/eveli-ide/demo-app```.


# setting up git based DB for wrench 


### 1 pick a location on cloud or local hdd where the root of virtual db is located
this is the location where private/public known_hosts git clone will work from
example when using locally:
* create folder named: /Users/olevmutso/Development/_wrench_only_persistence


### 2 ssh configuration
libarary uses [apache mina](https://github.com/apache/mina-sshd) for ssh connections, this means you need to have 3 things:
* private key
* public key
* known_hosts

Generate private and public key using following command(there will be no password): 
```bash
ssh-keygen -t rsa -b 4096 -m pem -C "wrench_only@resys.io" -f
```

Generate known_hosts using following command:
```bash
ssh-keyscan -H <my_host> >> known_hosts
```

You can test your private key with following command
```bash
ssh -o "IdentitiesOnly=yes" -i <private_key> -T git@github.com
```


### app configuration
```yaml
wrench.assets.git.enabled: true
wrench.assets.git.branch-specifier: main

# git repository clone url
wrench.assets.git.repository-url: "git@github.com:digiexpress-io/wrench-git-db.git"
# folder created in step 1
wrench.assets.git.repository-path: /Users/olevmutso/Development/_wrench_only_persistence
# files created in step 2, you must copy under 1 folder all ssh folders
wrench.assets.git.path: src/main/resources/git_auth
# private key name from classpath
wrench.assets.git.private-key: classpath:git_auth/id_rsa
```