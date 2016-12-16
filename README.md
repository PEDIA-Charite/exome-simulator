
[![license](https://img.shields.io/badge/licence-GNU%20GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0.txt)


# pedia-simulator
Simulate exomes using simulated exomes with spike-in pathogenic mutations

## Requirements

pedia-simulator requires java 8 and higher. [SIMdrome](https://github.com/visze/simdrom) has to be cloned and installed via [Maven](https://maven.apache.org/) as well as the development branch of [jannovar](https://github.com/charite/jannovar/tree/develop)

## Installation from Gitlab sources

1. Clone this repository.
2. Compile the java classes using [Maven](https://maven.apache.org/)

### Clone this repository

Use your terminal and go to a folder where you want to checkout pedia-simulator. Then run:

```
git clone https://gitlab.com/charite/pedia-simulator.git
```

### Compile the java classes

Go to your repository and create a jar file of pedia-simulator using Maven.

```
cd pedia-simulator

mvn clean install package
```

Now you should have the  `pedia-simulator-0.1-SNAPSHOT.jar` in the folder `target/`.

## Usage

### Randomly spike-in mutations

This option will use a background VCF (e.g. 1000 Genomes sample) and a mutation (multi) VCF and generates new VCFs containing one random sample of the backgrounds and one sample of the mutation VCF. In addition it filters the variants down to a clinical subset using OMIM.

The number of samples in the mutations VCF are the number of resulting VCF-Files. The output files have the name MUTATION-SAMPLE-NAME_BACKGROUND-SAMPLE-NAME.vcf.gz and both names are included into the header of the VCF.

Please use the seed to generate reproducible results. Otherwise the radom selection of a background sample will be different for each new run of the program!

Important! The background VCF must be annotated with Jannovar (>= v0.18).

Example:

```
java -jar pedia-simulator-0.0.1-SNAPSHOT.jar spike-in \
-m mutations.vcf.gz -v background.vcf.gz \
-o omim_genemap2.txt \
-out outdir
```

Run `java -jar pedia-simulator-0.0.1-SNAPSHOT.jar spike-in -h` to see the help. 
 

### Build reference samples

### Creat simulated JSON files
