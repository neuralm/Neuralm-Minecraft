# Neuralm-Minecraft

This is a Minecraft mod made with Minecraft Forge. 
Its goal is to train an AI to play minecraft, it uses the [Neuralm Java Client](https://github.com/neuralm/Neuralm-Java-Client) to communicate with the [Neuralm Server](https://github.com/neuralm/Neuralm-Server).
It requests brains, tests them, and sends the results back to the server.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites
You will need the following tools:

* [Jetbrains IntelliJ IDEA](https://www.jetbrains.com/idea/)
* [Java JDK 8](https://www.oracle.com/technetwork/java/javase/downloads/index.html)

### Setup
Follow these steps to get your development environment set up:

  1. Clone the repository.
  2. Import the build.gradle into IntelliJ.  
  3. Run `gradlew genIntellijRuns`

## Deployment

To generate the build mod jar to use in a normal game run `gradlew build`.
The build mod jar will be in build/libs.

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/neuralm/Neuralm-Server/tags). 

## Authors

* **Glovali** - *Initial work* - [Metalglove](https://github.com/metalglove)
* **Suppergerrie2** - *Initial work* - [Suppergerrie2](https://github.com/suppergerrie2)
* **TheMechanist1** - *Initial work* - [MechanistPlays](https://github.com/TheMechanist1)

See also the list of [contributors](https://github.com/neuralm/Neuralm-Minecraft/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
