package org.processmining.catminer.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Aktivitaet {

	private final StringProperty aktivitaetName;
	
    public Aktivitaet() {
        this(null);
    }

	public Aktivitaet(String aktivitaetName) {
		this.aktivitaetName = new SimpleStringProperty(aktivitaetName);
	}
	
	public String getAktivitaetName() {
		return aktivitaetName.get();
	}

	public void setAktivitaetName(String aktivitaetName) {
		this.aktivitaetName.set(aktivitaetName);
	}
	
	public StringProperty aktivitaetNameProperty() {
		return aktivitaetName;
	}
}