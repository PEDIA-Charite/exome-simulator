
[![license](https://img.shields.io/badge/licence-GNU%20GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0.txt)


# pedia-simulator
Simulate exomes using simulated exomes with spike-in pathogenic mutations

## Requirements

pedia-simulator requires java 8 and higher. The development branch of [SIMdrome](https://github.com/visze/simdrom/tree/develop) has to be cloned and installed via [Maven](https://maven.apache.org/).

### SIMdrom

```
git clone https://github.com/visze/simdrom.git
cd simdrom
git checkout development
mvn install
```

## Installation from Gitlab sources

1. Clone this repository.
2. Checkout development
3. Compile the java classes using [Maven](https://maven.apache.org/)

### Clone this repository

Use your terminal and go to a folder where you want to checkout pedia-simulator. Then run:

```
git clone https://gitlab.com/charite/pedia-simulator.git
```

### Compile the java classes

Go to your repository and create a jar file of pedia-simulator using Maven.

```
cd pedia-simulator
git checkout development
mvn clean install package
```

Now you should have the  `pedia-simulator-0.1-SNAPSHOT.jar` in the folder `target/`.

## Annotate a VCF file

This section describes how you need to annotate a VCF file before you can use it with the pedia simulator. We need to annotate it using a modified jannovar version with CADD, and several population databases (like UK10K, ExAC, dbSNP and 1000 Genomes).

So first we need to clone my repository and go to the tabixAnnotation branch:

```
git clone https://github.com/visze/jannovar.git
cd jannovar
git checkout feature/tabixAnnotation
```

Then we need to compile it via maven to generate the jar file

```
mvn package
```

Now we should have jar file located here: `jannovar-cli/target/jannovar-cli-0.21-SNAPSHOT.jar`

Then we have to create a transcript database (refseq) for jannovar:

```
java -jar jannovar-cli/target/jannovar-cli-0.21-SNAPSHOT.jar download \
-d hg19/refseq
```

RefSeq will be serialized and stored under `data/hg19_refseq.ser`.

The next step is to download all the needed databases:

* CADD SNV: http://krishna.gs.washington.edu/download/CADD/v1.3/
* CADD INDEL: http://krishna.gs.washington.edu/download/CADD/v1.3/
* ExAC: ftp://ftp.broadinstitute.org/pub/ExAC_release/release0.3.1/
* UK10K: Not available for public
* reference fasta: Download it from your favorite source. But be aware that it must be one indexed multifasta file and the chromosomes must be named with 1,2,3,... and not chr1,chr2,chr3,...

Now we can annotate any multiVCF (e.g. 1kG population or mutation file):

```
java -jar jannovar-cli-0.21-SNAPSHOT.jar annotate-vcf \
	-d data/hg19_refseq.ser \
	--exac-vcf ExAC.r0.3.sites.vep.vcf.gz --uk10k-vcf UK10K_COHORT.20160215.sites.vcf.gz \
	--tabix whole_genome_SNVs.tsv.gz InDels.tsv.gz --tabix-prefix CADD_SNV_ CADD_INDEL_ \
	--ref-fasta human_g1k_v37.fasta \
	-i sample.vcf.gz -o sample_annotated.vcf.gz
		```

The new annotated file `sample_annotated.vcf.gz` than can be used for the spike-in with an additional annotated 1kG background VCF (see "Build reference samples" how to generate that).

## The pedia simulator

### Randomly spike-in mutations

This option will use a background VCF (e.g. 1000 Genomes sample) and a mutation (multi) VCF and generates new VCFs containing one random sample of the backgrounds and one sample of the mutation VCF. In addition it filters the variants down to a clinical subset using OMIM (Only genes of OMIM are used that have a phenotype and the phenotype should not start with `[`, `{`, or `?`).

The number of samples in the mutations VCF are the number of resulting VCF-Files. The output files have the name MUTATION-SAMPLE-NAME_BACKGROUND-SAMPLE-NAME.vcf.gz and both names are included into the header of the VCF.

Please use the seed to generate reproducible results. Otherwise the random selection of a background sample will be different for each new run of the program!

Important! The background VCF must be annotated with Jannovar v0.20 (see build reference samples).

Example:

```
java -jar pedia-simulator-0.0.1-SNAPSHOT.jar spike-in \
-m mutations.vcf.gz -v background.vcf.gz \
-o omim_genemap2.txt \
-out outdir
```

If you want to generate files only for a subset of samples you can use the `--sample` option to define one ore more samples of the mutation VCF. 


Run `java -jar pedia-simulator-0.0.1-SNAPSHOT.jar spike-in -h` to see the help. 

 

#### Build reference samples

This part is how you generate a multiVCF file only on the refseq coding regions with the 1KG data downloaded from ftp://ftp-trace.ncbi.nih.gov/1000genomes/ftp/release/20130502/

First we have to get the RefSeq database and generate a bed-file for filtering (+ indexing).

```
mkdir refseq
curl ftp://ftp.ncbi.nlm.nih.gov/refseq/H_sapiens/H_sapiens/ARCHIVE/ANNOTATION_RELEASE.105/g'FF/ref_GRCh37.p13_top_level.gff3.gz \
	> refseq/ref_GRCh37.p13_top_level.gff3.gz

zcat refseq/ref_GRCh37.p13_top_level.gff3.gz | \
	grep '^NC' | grep '\sexon\s' | awk -F \"\\t\" -v OFS=\"\\t\" '{{print $1,$4-1,$5,$3,$6,$7}}' | \
	sed 's/NC_000001.10/1/g' | \
	sed 's/NC_000002.11/2/g' | \
	sed 's/NC_000003.11/3/g' | \
	sed 's/NC_000004.11/4/g' | \
	sed 's/NC_000005.9/5/g' | \
	sed 's/NC_000006.11/6/g' | \
	sed 's/NC_000007.13/7/g' | \
	sed 's/NC_000008.10/8/g' | \
	sed 's/NC_000011.9/9/g' | \
	sed 's/NC_000010.10/10/g' | \
	sed 's/NC_000009.11/11/g' | \
	sed 's/NC_000012.11/12/g' | \
	sed 's/NC_000013.10/13/g' | \
	sed 's/NC_000014.8/14/g' | \
	sed 's/NC_000015.9/15/g' | \
	sed 's/NC_000016.9/16/g' | \
	sed 's/NC_000017.10/17/g' | \
	sed 's/NC_000018.9/18/g' | \
	sed 's/NC_000019.9/19/g' | \
	sed 's/NC_000020.10/20/g' | \
	sed 's/NC_000021.8/21/g' | \
	sed 's/NC_000022.10/22/g' | \
	sed 's/NC_000023.10/X/g' | \
	sed 's/NC_000024.9/Y/g' | \
	sed 's/NC_012920.1/MT/g' | \
	sort -k 1,1 -k 2,2n | bedtools merge -i - | bgzip -c \
	> refseq/ref_GRCh37.p13_top_level.bed.gz

tabix refseq/ref_GRCh37.p13_top_level.bed.gz
```
Then we need to filter the vcfs of 1KG still separated by their chromosomes.
This will be different for autosomes and gonosomes. For gonosomes you need an extra ruby script that is present in the scripts-folder of the pedia-simulator

```
mkdir 1KG
refseq=refseq/ref_GRCh37.p13_top_level.bed.gz
# Autosomes:
bedtools intersect -header -a {input.vcf} -b $refseq 2>/dev/null | \
	bgzip -c > 1KG/1KG_{chr}.refSeq105.vcf.gz
# Gonosomes:
bedtools intersect -header -a ALL.chrX.phase3_shapeit2_mvncall_integrated_v1b.20130502.genotypes.vcf.gz -b $refseq 2>/dev/null | \
	bgzip -c > 1KG/1KG_chrX.refSeq105.vcf.gz
bedtools intersect -header -a ALL.chrY.phase3_integrated_v2a.20130502.genotypes.vcf.gz -b $refseq 2>/dev/null | \
	ruby scripts/modify_Y+MT/addZeroIfSampleNotPresent.rb scripts/modify_Y+MT/samples.txt | \
	bgzip -c > 1KG/1KG_chrY.refSeq105.vcf.gz
bedtools intersect -header -a ALL.chrMT.phase3_callmom.20130502.genotypes.vcf.gz -b $refseq 2>/dev/null | \
		ruby scripts/modify_Y+MT/addZeroIfSampleNotPresent.rb scripts/modify_Y+MT/samples.txt | \
		bgzip -c > 1KG/1KG_chrMT.refSeq105.vcf.gz
``` 

Then we have to index every vcf new VCF file `tabix 1KG/1KG_chr*.refSeq105.vcf.gz` and finally concat them together:

```
bcftools concat 1KG/1KG_chr*.refSeq105.vcf.gz | \
	bgzip -c > 1KG.refSeq105.vcf.gz
tabix 1KG.refSeq105.vcf.gz
```

### Append JSON with simulated variants

This task uses an annotated VCF file (maybe generated by the spike-in command), a jsonfile with scores genes by gestalt, feature, etc and it creates an new json file with the old content and adds CADD phred and raw scores to genes (maybe create new genes if gene is not present). Important is, that the json file contains the entrez gene id and the field is called TODO.

It also uses the genemap2 file of omim to use only the genes that are listed in OMIM. Only genes of OMIM are used that have a phenotype and the phenotype string should not start with `[`, `{`, or `?`.

Run `java -jar pedia-simulator-0.0.1-SNAPSHOT.jar extend-json -h` to see the help. 

Command for a testsample:

```
java -jar pedia-simulator-0.0.1-SNAPSHOT.jar extend-json \
-j testsample.json -v testsample.vcf.gz -o genemap2.txt \ 
-out testsample_with_cadd.json 
```





