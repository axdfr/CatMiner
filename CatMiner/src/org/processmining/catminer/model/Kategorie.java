package org.processmining.catminer.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Kategorie{

	private final StringProperty kategorieName;

	public Kategorie(String kategorieName) {
		this.kategorieName = new SimpleStringProperty(kategorieName);
	}
	
	public String getKategorieName() {
		return kategorieName.get();
	}

	public void setKategorieName(String kategorieName) {
		this.kategorieName.set(kategorieName);
	}
	
	public StringProperty kategorieNameProperty() {
		return kategorieName;
	}
}