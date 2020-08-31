package org.processmining.catminer.view;


import org.processmining.catminer.MainApp;
import org.processmining.catminer.model.Kategorie;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.util.Callback;

public class KategorisierenViewController {
    @FXML
    public TableView<ObservableList<Object>> kategorisierenTabelle;
    
    @FXML
    private TableColumn<ObservableList<Object>, String> aktivitaetsSpalte;
    
    @FXML
    private Label kategorieHinzufuegenLabel;
    
    @FXML
    private Label kategorienSpeichernLabel;
	
    @FXML
    private TextField kategorieAendernTextField;
    
    @FXML
    private Button kategorieHinzufuegenButton;
    
    @FXML
    private Button kategorieLoeschenButton;  
    
    @FXML
    private Button kategorienSpeichernButton;
    
    private MainApp mainApp;
 
    /**
     * Konstruktor, der vor der Initialisierung geladen wird.
     */
    public KategorisierenViewController() {
    }

    /**
     * Initialisierung der Controller-Klasse. Die initialize-Methode wird automatisch aufgerufen,
     * nachdem die Fxml-Datei geladen wurde.
     */
    @FXML
    private void initialize() {
    	
    	
	    aktivitaetsSpalte.setCellValueFactory(new Callback<CellDataFeatures<ObservableList<Object>,String>,ObservableValue<String>>(){                    
	    public ObservableValue<String> call(CellDataFeatures<ObservableList<Object>, String> param) {                                                                                              
	        return new SimpleStringProperty(param.getValue().get(0).toString());                        
	    }                    
	    });
	    aktivitaetsSpalte.setSortable(false);
	    
	    /* Dynamische Konstruktion einer Tabellenspalte für jede Kategorie.
	     * Jede Spalte wird mit Checkboxen aufgefüllt, die den Werten der Zuweisungsliste entsprechen.
	     */
	    for(int i=1 ; i< MainApp.getKategorieListe().size() + 1; i++){
		    final int j = i;                
		    TableColumn <ObservableList<Object>, Boolean> kategorieSpalte = new TableColumn<ObservableList<Object>, Boolean>(MainApp.getKategorieListe().get(i - 1).getKategorieName());
		    kategorieSpalte.setCellValueFactory(new Callback<CellDataFeatures<ObservableList<Object>,Boolean>,ObservableValue<Boolean>>(){                    
		        public ObservableValue<Boolean> call(CellDataFeatures<ObservableList<Object>, Boolean> param) {                                                                                              
		            return  (SimpleBooleanProperty) param.getValue().get(j);                        
		        }                    
		    });	
		    kategorieSpalte.setCellFactory(CheckBoxTableCell.forTableColumn(kategorieSpalte));
		    kategorieSpalte.setEditable(true);
		    kategorieSpalte.setSortable(false);
		    kategorisierenTabelle.getColumns().add(kategorieSpalte);
		    kategorisierenTabelle.setEditable(true);
	    }

    }
    


	/**
     * Wird von der MainApp aufgerufen, um eine Referenz zu sich selbst zu vollziehen.
     * 
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
       
        // Die Zuweisungsliste in die Tabelle eintragen. 
        kategorisierenTabelle.setItems(MainApp.getZuweisungsListe());
        
    }
    
    // fuegt eine Kategorie hinzu, deren Name aus dem Textfeld entnommen wird.
    @FXML
    private void handleHinzufuegen() {

    	if(!kategorieAendernTextField.getText().isEmpty()) {
	        Kategorie temporaerKategorie = new Kategorie(kategorieAendernTextField.getText());
	        boolean okAuswahl = mainApp.showKategorieAendernAlert(temporaerKategorie, "hinzufügen");
	        if (okAuswahl) {
	            MainApp.getKategorieListe().add(temporaerKategorie);
	        }
    	}
    	
    	mainApp.zuweisungsListeHinzufuegen();
    	
    	for(int n = 0; n < MainApp.getAktivitaetListe().size(); n++) {
        	for(int i = 0; i < MainApp.getKategorieListe().size() + 1; i++) {
        		System.out.println(MainApp.getZuweisungsListe().get(n).get(i).toString());
        	}
    	}
    	
    	mainApp.showKategorisierenView();
    }
    
    // loescht die Kategorie, deren Name mit dem String aus dem Textfeld übereinstimmt.
    @FXML
    private void handleLoeschen() {

    	if(!kategorieAendernTextField.getText().isEmpty()) {
    		System.out.println(kategorieAendernTextField.getText());
	        Kategorie temporaerKategorie = new Kategorie(kategorieAendernTextField.getText());
	        
	        boolean vorhanden = false;
	        boolean okAuswahl = mainApp.showKategorieAendernAlert(temporaerKategorie, "löschen");
	        
	        if (okAuswahl) {
	        	for(int n = 0; n < MainApp.getKategorieListe().size(); n++) {
	        		if(MainApp.getKategorieListe().get(n).getKategorieName().equalsIgnoreCase(temporaerKategorie.getKategorieName())) {
	        			vorhanden = true;
	        			MainApp.getKategorieListe().remove(n);
	        			mainApp.zuweisungsListeLoeschen(n);
	        			break;
	        		}
	        	}
	        	mainApp.showKategorisierenView();
	        }	        	        
	        
	        if(!vorhanden) {
	        	Alert alert = new Alert(AlertType.ERROR);
	        	alert.setTitle("CatMiner");
	        	alert.setHeaderText("Fehler");
	        	alert.setContentText("Die Kategorie " + temporaerKategorie.getKategorieName() + " konnte nicht gefunden werden.");
	        	
	        	alert.showAndWait();
	        }
    	}
    }
}