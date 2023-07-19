# Clone the repo
```
git clone git@github.com:dialob/dialob-parent.git
```
or
```
git clone https://github.com/dialob/dialob-parent.git
```

# install and run locally
```
cd dialob-parent
git checkout digiexpress
mvn clean install -DskipTests=true
dialob-dev/spring-app
mvn spring-boot:run
```

# run frontend
```
cd dialob-parent/dialob-composer/dialob-composer-ui
yarn install
yarn start

```
view on http://localhost:3000/portal
