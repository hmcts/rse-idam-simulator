# Rse Idam Simulator using spring-boot-template

[![Build Status](https://travis-ci.org/hmcts/spring-boot-template.svg?branch=master)](https://travis-ci.org/hmcts/spring-boot-template)

## Notes

Since Spring Boot 2.1 bean overriding is disabled. If you want to enable it you will need to set `spring.main.allow-bean-definition-overriding` to `true`.

JUnit 5 is now enabled by default in the project. Please refrain from using JUnit4 and use the next generation

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running/Debugging the application with IntelliJ Idea
Right click on Application.java and choose Run 'Application.main()' or Debug 'Application.main()'

### Running the application with Docker

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Create docker image:

```bash
  docker-compose build
```

Run the distribution (created in `build/install/rse-idam-simulator` directory)
by executing the following command:

```bash
  docker-compose up
```

This will start the API container exposing the application's port
(set to `5556` in this template app).

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:5556/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Alternative script to run application

To skip all the setting up and building, just execute the following command:

```bash
./bin/run-in-docker.sh
```

For more information:

```bash
./bin/run-in-docker.sh -h
```

Script includes bare minimum environment variables necessary to start api instance. Whenever any variable is changed or any other script regarding docker image/container build, the suggested way to ensure all is cleaned up properly is by this command:

```bash
docker-compose rm
```

It clears stopped containers correctly. Might consider removing clutter of images too, especially the ones fiddled with:

```bash
docker images

docker image rm <image-id>
```

There is no need to remove postgres and java or similar core images.


## How to use the simulator with Post Man
Check IdamSimulatorController to see how works the endpoints. These endpoints are all the endpoints required to have the idam java client working correctly,
and one endpoint to add a user in the local memory map of the simulator. Keep in mind that username and email are the same in Idam system.

Here a quick start and having a request a token using postman and the open id route.

Add an user by doing this request:
```
POST  http://localhost:5556/testing-support/accounts
Content-type: application/json
{

"email": "myemail-test@hmcts.net",
"forename": "John",
"surename": "Smith",
"roles": ["role1", "role2"],
"password": "onePassword"

}
```

Have an openId Token using this call. Notice this is not some Jason content but x-www-form-urlencoded content.
```
POST  http://localhost:5556/o/token
Content-type: application/x-www-form-urlencoded

client_id: sometestservice
client_secret: sometestservice
grant_type: password
redirect_uri: https://davidtestservice.com
username: myemail-test@hmcts.net
password: somePassword
scope: openid profile roles
```

## How to add an user and request a token using swagger?
Do same operation than above but using these swaggers endpoints from any browser:
- http://localhost:5556/swagger-ui.html#/idam-simulator-controller/addNewUserUsingPOST
- http://localhost:5556/swagger-ui.html#/idam-simulator-controller/getOpenIdTokenUsingPOST

## License

