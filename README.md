
[![Build Status](https://travis-ci.org/visze/pedia-simulator.svg?branch=master)](https://travis-ci.org/charite/pedia-simulator)
[![API Docs](https://img.shields.io/badge/api-v0.1-SNAPSHOT-blue.svg?style=flat)](http://charite.github.io/pedia-simulator/api/0.1-SNAPSHOT/)
[![license](https://img.shields.io/badge/licence-GNU%20GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0.txt)


# pedia-simulator
Simulate exomes using simulated exomes with spike-in pathogenic mutations

## Requirements

pedia-simulator requires java 8 and higher. [SIMdrome](https://github.com/visze/simdrom) has to be cloned and installed via [Maven](https://maven.apache.org/) as well as the development branch of [jannovar](https://github.com/charite/jannovar/tree/develop)

## Installation from Github sources

1. Clone this repository.
2. Compile the java classes 
3. Load the plugin into your Weka Package Manager

### Clone this repository

Use your terminal and go to a folder where you want to checkout pedia-simulator. Then run:

```
git clone https://github.com/charite/pedia-simulator.git
```

### Compile the java classes and create the Weka plugin

Go to your repository and create a jar file of pedia-simulator using Maven.

```
cd pedia-simulator

mvn clean install package
```

Now you should have the  `pedia-simulator-0.1-SNAPSHOT.jar` in the folder `target/`.

## Usage

### Build reference samples

### Creat simulated JSON files
