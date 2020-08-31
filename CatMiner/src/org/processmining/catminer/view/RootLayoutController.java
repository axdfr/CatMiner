package org.processmining.catminer.view;

import java.io.File;

import org.processmining.catminer.MainApp;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;

public class RootLayoutController {

    private MainApp mainApp;

    /**
     * MainApp ruft die MainApp, um eine Referenz auf sich selbst durchzuführen.
     * 
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Cleart alle Listen, damit neue Datei bearbeitet werden kann.
     */
    @FXML
    private void handleNeuMenuOption() {
        MainApp.getKategorieListe().clear();
        MainApp.getAktivitaetListe().clear();
        MainApp.getZuweisungsListe().clear();
 
        mainApp.setXesFilePath(null);
        mainApp.initRootLayout();
    }

    /**
     * Benutzung eines FileChoosers damit eine Datei bearbeitet werden kann.
     */
    @FXML
    private void handleOeffnenMenuOption() {
        FileChooser fileChooser = new FileChooser();

        // Programm erlaubt nur Dateien mit Endung XES.
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "XES files (*.xes)", "*.xes");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());

        if (file != null) {
            mainApp.loadXesDataFromFile(file);
        }
    }

    /**
     * Speichert die Datei, die momentan ausgewählt ist.
     * Falls keine Auswahl besteht wird "Speichern als" verwendet.
     */
    @FXML
    private void handleSpeichernMenuOption() {
        File file = mainApp.getXesFilePath();
        if (file != null) {
            mainApp.saveXesDataToFile(file);
        } else {
        	handleSpeichernAlsMenuOption();
        }
    }

    /**
     * Verwendet FileChooser um Speicherort auszuwaehlen.
     */
    @FXML
    private void handleSpeichernAlsMenuOption() {
        FileChooser fileChooser = new FileChooser();

        // Programm erlaubt nur Dateien mit Endung XES.
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "XES files (*.xes)", "*.xes");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showSaveDialog(mainApp.getPrimaryStage());

        if (file != null) {
            // Test auf korrekte Endung.
            if (!file.getPath().endsWith(".xes")) {
                file = new File(file.getPath() + ".xes");
            }
            mainApp.saveXesDataToFile(file);
        }
    }

    /**
     * Hilfedialog anzeigen.
     */
    @FXML
    private void handleHilfe() {
    	Alert alert = new Alert(AlertType.INFORMATION);
    	alert.setTitle("CatMiner");
    	alert.setHeaderText("Hilfe");
    	alert.setContentText("Autor: Leonard Nake" + "\n" + "Email: leonard.nake@student.uni-halle.de");

    	alert.showAndWait();
    }

    /**
     * Anwendung schliessen.
     */
    @FXML
    private void handleSchliessen() {
    	mainApp.setXesFilePath(null);
    	System.exit(0);
    }
    
    // Wechselt zur KategorisierenView.
    @FXML
    private void handleKategorisierenMenuOption() {
        
        File file = mainApp.getXesFilePath();
        if (file != null) {
        	mainApp.showKategorisierenView();
        } else {
        	Alert alert = new Alert(AlertType.ERROR);
        	alert.setTitle("CatMiner");
        	alert.setHeaderText("Fehler");
        	alert.setContentText("Bitte wählen Sie zunächst die zu bearbeitende Datei aus.");
        	
        	alert.showAndWait();
        }	
    }
    
    // Wechselt zur VisualisierenView.
    @FXML
    private void handleVisualisierenMenuOption() {
        
        File file = mainApp.getXesFilePath();
        if (file != null) {
        	mainApp.showVisualisierenView();
        } else {
        	Alert alert = new Alert(AlertType.ERROR);
        	alert.setTitle("CatMiner");
        	alert.setHeaderText("Fehler");
        	alert.setContentText("Bitte wählen Sie zunächst die zu bearbeitende Datei aus.");
        	
        	alert.showAndWait();
        }	
    }
}