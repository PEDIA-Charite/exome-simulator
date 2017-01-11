package de.charite.compbio.pediasimulator.model;

public class Gene {
	
	private String name;
	private int entrezGeneID;
	
	
	public Gene(String name, int entrezGeneID) {
		this.name = name;
		this.entrezGeneID = entrezGeneID;
	}
	
	public int getEntrezGeneID() {
		return entrezGeneID;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + entrezGeneID;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Gene other = (Gene) obj;
		if (entrezGeneID != other.entrezGeneID)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Gene [name=" + name + ", entrezGeneID=" + entrezGeneID + "]";
	}
	
	

}
