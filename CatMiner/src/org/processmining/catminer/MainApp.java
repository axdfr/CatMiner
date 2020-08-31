package org.processmining.catminer;

import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.prefs.Preferences;
import java.io.OutputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.XExtensionManager;
import org.deckfour.xes.extension.XExtensionParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.impl.XAttributeListImpl;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.catminer.model.Aktivitaet;
import org.processmining.catminer.model.Kategorie;
import org.processmining.catminer.view.KategorisierenViewController;
import org.processmining.catminer.view.RootLayoutController;
import org.processmining.catminer.view.VisualisierenViewController;
import org.xml.sax.SAXException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

public class MainApp extends Application {
	
    private Stage primaryStage;
    private BorderPane rootLayout;
    private XLog log;
    XExtensionManager xextensionManager;
    XExtension categorizationExtension;
    Set<XExtension> extensionSet;
    private boolean kategorisierungVorgenommen = false;

    public XLog getLog() {
		return log;
	}

	/**
     * Speichern der vorhandenen Daten als statische observableListen, die für die TableView verwendet werden.
     */
    static private ObservableList<Aktivitaet> aktivitaetListe = FXCollections.observableArrayList();
    static private ObservableList<Kategorie> kategorieListe = FXCollections.observableArrayList();
    static private ObservableList<ObservableList<Object>> zuweisungsListe = FXCollections.observableArrayList();

    /**
     * Konstruktor der MainApp
     */
    public MainApp() {
    }

    /**
     * Funktionen mit der die oft gebrauchten Listen returnt werden können.
     * 
     */
    public static ObservableList<Aktivitaet> getAktivitaetListe() {
        return aktivitaetListe;
    }
    
    public static ObservableList<Kategorie> getKategorieListe() {
        return kategorieListe;
    }
    
    public static ObservableList<ObservableList<Object>> getZuweisungsListe() {
        return zuweisungsListe;
    }    
    
    
    /**
     * Wird automatisch zur Beginn der Anwendung aufgerufen.
     * Laden von Extension und Icon.
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("CatMiner");
        
		try {
	    	XExtensionParser extentionParser = XExtensionParser.instance();
	    	File categorizationExtensionFile = new File("./resources/xesextensions/CategoriesExtension.xesext");
	    	categorizationExtension = extentionParser.parse(categorizationExtensionFile);
	    	
		} catch (IOException | ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		}
		
    	XExtensionManager manager = XExtensionManager.instance();
    	manager.register(categorizationExtension);
		
        // Das Icon der Anwendung einfuegen (liegt im Ordner resources)
        this.primaryStage.getIcons().add(new Image("file:resources/images/CatMinerIcon.png"));
        
        initRootLayout();
    }
    
	/**
     * Initialisierung des RootLayouts. Das RootLayout umfasst das Menu. 
     * In das Layout werden die anderen Ansichten hineingeladen.
     */
    public void initRootLayout() {
        try {
            // Lade das Root-Layout (fxml-Datei).
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();
            
            // Zeige das RootLayout
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            
            // Dem Controller Zugriff auf die MainApp geben.
            RootLayoutController controller = loader.getController();
            controller.setMainApp(this);
            
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Zeigt die Tabellenansicht mit der Aktivitäten kategorisiert werden können.
     */
    public void showKategorisierenView() {
        try {
            // Lade Kategorisierenansicht (fxml-Datei).
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/KategorisierenView.fxml"));
            AnchorPane kategorisierenView = (AnchorPane) loader.load();
            
            //Kategorisierung wird durchgeführt, weshalb gespeichert 
            //werden muss bevor Visualisierung aufgerufen wird.
            kategorisierungVorgenommen = true;
            
            // Die Kategorisierenansicht in die Mitte der RootLayout-Borderpane setzen.
            rootLayout.setCenter(kategorisierenView);
            
            // Dem Controller Zugriff auf die MainApp geben.
            KategorisierenViewController controller = loader.getController();
            controller.setMainApp(this);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Zeigt die Visualisierungsansicht.
     */
    public void showVisualisierenView() {
        try {
        	//Speichern bevor Visualisierung durchgeführt wird
        	if(kategorisierungVorgenommen) {
        		saveXesDataToFile(getXesFilePath());
        	}
        	kategorisierungVorgenommen = false;
        	
            // Lade Visualisierenansicht (fxml-Datei).
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/VisualisierenView.fxml"));
            AnchorPane visualisierenView = (AnchorPane) loader.load();
            
            // Die Kategorisierenansicht in die Mitte der RootLayout-Borderpane setzen.
            rootLayout.setCenter(visualisierenView);
            
            // Dem Controller Zugriff auf die MainApp geben.
            VisualisierenViewController controller = loader.getController();
            controller.setMainApp(this);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Nachfrage, ob Kategorie tatsächlich gelöscht/hinzugefügt werden soll.
     */
    public boolean showKategorieAendernAlert(Kategorie kategorie, String funktion) {
        	Alert aendernAlert = new Alert(AlertType.CONFIRMATION);
        	aendernAlert.setTitle("CatMiner");
        	aendernAlert.setHeaderText("Kategorie " + funktion);
        	aendernAlert.setContentText("Sind Sie sicher, dass Sie " + kategorie.getKategorieName() + " " +funktion +" möchten?");      
        	
        	Optional<ButtonType> result = aendernAlert.showAndWait();
        	if (result.get() == ButtonType.OK){
        	    return true;
        	} else {
        	    return false;
        	}
    }
    
    /**
     * Returnt den Dateipfad der letzten geöffnete Datei. Ermöglicht die Speicherfunktion
     * 
     * @return
     */
    public File getXesFilePath() {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        String filePath = prefs.get("filePath", null);
        if (filePath != null) {
            return new File(filePath);
        } else {
            return null;
        }
    }

    /**
     * Erstellt den Dateipfad der aktuell geöffneten Datei.
     * 
     * @param file
     */
    public void setXesFilePath(File file) {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        if (file != null) {
            prefs.put("filePath", file.getPath());

            // Titel aktualisieren.
            primaryStage.setTitle("CatMiner - " + file.getName());
        } else {
            prefs.remove("filePath");

            // Titel aktualisieren.
            primaryStage.setTitle("CatMiner");
        }
    }
    
    /**
     * Laden von Aktivitäten sowie Kategorien mit den jeweiligen Zuweisungen.
     * 
     * @param file
     */
    public void loadXesDataFromFile(File file) {
        try {
        	
            // Dateipfad angeben.
        	setXesFilePath(file);
        	       	
        	XesXmlParser parser = new XesXmlParser();
        	List<XLog> logListe = parser.parse(file);
        	
        	log = logListe.get(0);
        	
        	extensionSet = log.get(0).get(0).getExtensions();
        	
        	//falls im Trace Extensions existieren, die nicht in den Events stehen, 
        	//dann werden sie dem verwendeten Set zugeordnet.
        	Set<XExtension> extensionFromTrace = log.get(0).get(0).getExtensions();     	
        	for(XExtension ext : extensionFromTrace) {
        		if(!extensionSet.contains(ext)) extensionSet.add(ext);
        	}
        	
        	aktivitaetListe.clear();
        	kategorieListe.clear();
        	zuweisungsListe.clear();
        	initRootLayout();

        	List<String> logAktivitaeten = new ArrayList<String>();
        	List<String> logKategorien = new ArrayList<String>();
        	Multimap<String, String> zugehoerigMap = ArrayListMultimap.create();
        	
        	//erste Schleife fuer trace, zweite fuer event
        	for(int i = 0; i < log.size(); i++) {
        		for(int z = 0; z < log.get(i).size(); z++) {			
  
        			String logAktivitaet = log.get(i).get(z).getAttributes().get("Activity").toString();
        			
        			if(!logAktivitaeten.contains(logAktivitaet)) {
        				logAktivitaeten.add(logAktivitaet);
        				aktivitaetListe.add(new Aktivitaet((logAktivitaet)));
        			}
        			
        			try {
        				List<XAttribute> logKategorienListe = new ArrayList<XAttribute>();
        				
        				if(log.get(i).get(z).getAttributes().get("cat:Categories").hasAttributes()) {
        					
        					XAttributeListImpl attribute = (XAttributeListImpl) log.get(i).get(z).getAttributes()
        							.get("cat:Categories");
        					logKategorienListe.addAll(attribute.getCollection());
        				}
        				
        				for (XAttribute attribut : logKategorienListe) {
	        				String logKategorie = attribut.toString();
	        				System.out.println(logKategorie);
	        				
	            			if(!logKategorie.isEmpty()) {
	                			if(!logKategorien.contains(logKategorie)) {
	                				logKategorien.add(logKategorie);
	                				kategorieListe.add(new Kategorie((logKategorie)));
	                			}
	                		zugehoerigMap.put(logAktivitaet, logKategorie);
	            			}
        				}
        				
        			} catch(Exception e) {
        				//keine Handlung
        			}
        		}
        	}
        	
        	for (Map.Entry<String, String> entry : zugehoerigMap.entries()) {
        		System.out.println(entry.getKey() + " = " + entry.getValue());
        	}
        	
        	
        	zuweisungsListeInitiieren(zugehoerigMap);
        
        	Alert alert = new Alert(AlertType.INFORMATION);
        	alert.setTitle("CatMiner");
        	alert.setHeaderText("Erfolg");
        	alert.setContentText("Die Daten der Datei:\n" + file.getPath() + " wurden erfolgreich geladen. "
        			+ "Bitte wählen Sie nun im Menü einen Bearbeitungsmodus aus.");
        	
        	alert.showAndWait();
        	
        } catch (Exception e) {
        	Alert alert = new Alert(AlertType.ERROR);
        	alert.setTitle("CatMiner");
        	alert.setHeaderText("Fehler");
        	alert.setContentText("Die Daten der Datei:\n" + file.getPath() + " konnten nicht geladen werden.");
        	
        	alert.showAndWait();
        }
    }

    /**
     * Speichert die zugewiesenen Kategorien in der Datei.
     * 
     * @param file
     */
    public void saveXesDataToFile(File file) {
    	try {
        	XAttributeMapImpl kategorieZuweisungsMap = new XAttributeMapImpl();

        	for(int i = 0; i < aktivitaetListe.size(); i++) {
        		XAttributeListImpl kategorieWertListe = new XAttributeListImpl("cat:Categories");
            	for(int n = 1; n < kategorieListe.size() + 1; n++) {
            		if(zuweisungsListe.get(i).get(n).toString().contains("true")) {
            			XAttributeLiteralImpl kategorieWert = new XAttributeLiteralImpl
            					("cat:Category", MainApp.getKategorieListe().get(n - 1).getKategorieName(), categorizationExtension);
                    	kategorieWertListe.addToCollection(kategorieWert);
            		}	
            	}
    			kategorieZuweisungsMap.put(aktivitaetListe.get(i).getAktivitaetName(), kategorieWertListe);
    			System.out.println(kategorieZuweisungsMap);
        	}
        	
        	for(Map.Entry<String, XAttribute> entry : kategorieZuweisungsMap.entrySet()) {
        		XAttributeMapImpl tempMap = new XAttributeMapImpl();
        		tempMap.put("cat:Categories", entry.getValue());

        		for(int i = 0; i < log.size(); i++) {
            		for(int z = 0; z < log.get(i).size(); z++) {
            			
            			if(log.get(i).get(z).getAttributes().get("Activity").toString().equals(entry.getKey())) {
            				
            				log.get(i).get(z).getAttributes().remove("cat:Categories");
            				tempMap.putAll(log.get(i).get(z).getAttributes());
            				
            				XEventImpl eve = new XEventImpl(tempMap);
            				log.get(i).set(z, eve);

            			}
            		}
            	}
        	}
        	
        	extensionSet.add(categorizationExtension);
        	
        	for(XExtension ext : extensionSet) {
        		log.getExtensions().add(ext);
        	}
        	
        	boolean globalesEventAttributVorhanden = false;
        	for(XAttribute globalesEvent :log.getGlobalEventAttributes()) {
        		if(globalesEvent.getKey().contains("cat:Categories")) {
        			globalesEventAttributVorhanden = true;
        		}
        	}
        	
        	if(!globalesEventAttributVorhanden) {
            	XAttributeListImpl globalesListenAttribut = new XAttributeListImpl("cat:Categories");
            	log.getGlobalEventAttributes().add(globalesListenAttribut);
        	}

        	//Die Logdaten in die ausgewählte Datei schreiben
        	OutputStream output = new FileOutputStream(file);
        	XesXmlSerializer serializer = new XesXmlSerializer();
        	serializer.serialize(log, output);

            // Den File-Path speichern
            setXesFilePath(file);

        } catch (Exception e) { 
        	Alert alert = new Alert(AlertType.ERROR);
        	alert.setTitle("Error");
        	alert.setHeaderText("Could not save data");
        	alert.setContentText("Could not save data to file:\n" + file.getPath());
        	
        	alert.showAndWait();
        }
    }
    
	/**
	 * Returnt die Main-Stage
	 * @return
	 */
	public Stage getPrimaryStage() {
		return primaryStage;
	}
	
	
	/**
	 * Funktion, die der Zuweisungliste Boolean-Properties zuweist, wenn eine neue Kategorie hinzugefügt wurde
	 */
	public void zuweisungsListeHinzufuegen(){
		for(int i = 0; i < aktivitaetListe.size(); i++) {
			zuweisungsListe.get(i).add(new SimpleBooleanProperty(false));
		}	
	}
	
	/**
	 * Funktion, die die Werte der gelöschten Kategorie aus der Zuweisungsliste entfernt.
	 * Verwendet den Index der gefundenen Kategorie aus der Kategorieliste.
	 */
	public void zuweisungsListeLoeschen(int listenIndex){
		for(int i = 0; i < aktivitaetListe.size(); i++) {
			zuweisungsListe.get(i).remove(listenIndex + 1);
		}
	}
	
	
	public void zuweisungsListeInitiieren(Multimap<String, String> map) {
		
		/* 
		 * Zunaechst wird die Zuweisungsliste nach den Anzahlen der Aktivitäten und Kategorien initiiert.
		 * Im ersten Schritt werden alle SimpleBooleanProperties als false initiiert.
		 */
    	for(int i = 0; i < aktivitaetListe.size(); i++) {
    		ObservableList<Object> reihe = FXCollections.observableArrayList();
    		for(int n = 0; n < kategorieListe.size() + 1; n++) {
        		if(n == 0) reihe.add(aktivitaetListe.get(i).getAktivitaetName());
        		else reihe.add(new SimpleBooleanProperty(false));
        	}
    		zuweisungsListe.add(reihe);
    	}
    	
		/* 
		 * Im nächsten Schritt werden dann alle Einträge der erstellten Map ausgelesen.
		 * Aus den Listen werden die Koordinaten der jeweiligen Beziehung innerhalb der Zuweisungsliste entnommen.
		 * Dies geschieht nur, wenn es Einträge in der Map gibt.
		 */
		if(!map.isEmpty()) {
	    	for (Map.Entry<String, String> entry : map.entries()) {
	    		int x = 404;
	    		int y = 404;
	    		for(int i = 0; i < aktivitaetListe.size(); i++) {
	    			if(aktivitaetListe.get(i).getAktivitaetName().equals(entry.getKey())) {
	    				x = i;
	    				break;
	    			}
	    		}
	    		for(int n = 0; n < kategorieListe.size(); n++) {
	    			if(kategorieListe.get(n).getKategorieName().equals(entry.getValue())) {
	    				y = n + 1;
	    				break;
	    			}
	    		}
	    	    zuweisungsListe.get(x).set(y, new SimpleBooleanProperty(true));
	    		System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
	    	}
	    	
	    	for(int n = 0; n < aktivitaetListe.size(); n++) {
	        	for(int i = 0; i < kategorieListe.size() + 1; i++) {
	        		System.out.println(zuweisungsListe.get(n).get(i).toString());
	        	}
	    	}
		}
	}
	
	//Startet die Anwendung.
    public static void main(String[] args) {
        launch(args);
    }
}