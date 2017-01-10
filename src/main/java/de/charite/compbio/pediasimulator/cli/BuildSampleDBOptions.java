package de.charite.compbio.pediasimulator.cli;

import java.io.File;
import java.util.function.BiFunction;

import de.charite.compbio.pediasimulator.cmd.BuildSampleDBCommand;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

public class BuildSampleDBOptions {

	private File vcfInputFile;
//	private File jannovarDB;
//	private File caddSNV;
//	private File caddIndel;
//	private File genomeFastaFile;
	private File serializedFile;
	private File omimFile;

	public static void setupParser(Subparsers subparsers) {
		BiFunction<String[], Namespace, BuildSampleDBCommand> handler = (argv, args) -> {
			try {
				return new BuildSampleDBCommand(argv, args);
			} catch (CommandLineParsingException e) {
				throw new RuntimeException("Could not parse command line", e);
			}
		};
		Subparser parser = subparsers.addParser("build-sample-db").aliases("build").setDefault("cmd", handler);

		parser.addArgument("-v", "--variants").type(File.class).nargs(1).required(true)
				.help("VCF file of input variants");
//		parser.addArgument("-jdb", "--jannovardb").type(File.class).nargs(1).required(true)
//				.help("Jannovar serialized databases (refseq)");
//		parser.addArgument("-cs", "--cadd").type(File.class).nargs(1).required(true)
//				.help("CaddScore file for variants");
//		parser.addArgument("-ci", "--caddindel").type(File.class).nargs(1).required(true)
//				.help("CaddScore file for indels");
//		parser.addArgument("-f", "--fasta").type(File.class).nargs(1).required(true)
//		.help("Fasta file of the used reference genome");
		parser.addArgument("-s", "--serialize").type(File.class).nargs(1).required(true)
		.help("Serialize all samples to this file!");
		parser.addArgument("-o", "--omim").type(File.class).nargs(1).required(true)
		.help("OMIM genemap2 file!");

		parser.defaultHelp(true);
	}

	public void setFromArgs(Namespace res) {
		this.vcfInputFile = (File) res.getList("variants").get(0);
//		this.jannovarDB = (File) res.getList("jannovardb").get(0);
//		this.caddSNV = (File) res.getList("cadd").get(0);
//		this.caddIndel = (File) res.getList("caddindel").get(0);
//		this.genomeFastaFile = (File) res.getList("fasta").get(0);
		this.serializedFile = (File) res.getList("serialize").get(0);
		this.omimFile = (File) res.getList("omim").get(0);
	}

	public File getVcfInputFile() {
		return vcfInputFile;
	}

	public File getSerializedFile() {
		return serializedFile;
	}
	
	public File getOMIMFile() {
		return omimFile;
	}

}
