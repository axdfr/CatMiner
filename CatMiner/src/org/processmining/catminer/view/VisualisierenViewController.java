package org.processmining.catminer.view;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.jgraph.graph.GraphConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.processmining.catminer.MainApp;
import org.processmining.catminer.model.Aktivitaet;
import org.processmining.catminer.model.Kategorie;
import org.processmining.catminer.util.ZoomableScrollPane;
import org.processmining.hybridminer.algorithms.cg2hpn.CGToHybridPN;
import org.processmining.hybridminer.algorithms.preprocessing.*;
import org.processmining.hybridminer.models.causalgraph.HybridCausalGraph;
//import org.processmining.hybridminer.models.causalgraph.PIPPanel;
//import org.processmining.hybridminer.models.causalgraph.ZoomPanel;
import org.processmining.hybridminer.models.causalgraph.gui.HybridPetrinetVisualization;
import org.processmining.hybridminer.models.causalgraph.gui.HybridPetrinetVisualizer;
import org.processmining.hybridminer.models.hybridpetrinet.HybridPetrinet;
import org.processmining.hybridminer.plugins.HybridCGMiner;
import org.processmining.hybridminer.plugins.HybridCGMinerSettings;
import org.processmining.hybridminer.plugins.HybridPNMinerSettings;
import org.processmining.models.graphbased.AttributeMapOwner;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.shapes.Ellipse;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.settings.HeuristicsMinerSettings;
import com.fluxicon.slickerbox.factory.SlickerDecorator;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class VisualisierenViewController {
	
    @FXML
    public TableView<ObservableList<Object>> visualisierenTabelle;
    
    @FXML
    private TableColumn<ObservableList<Object>, String> kategorieSpalte;
	
    @FXML
    private TableColumn<ObservableList<Object>, Boolean> einzeichnenSpalte;
	
    @FXML
    public StackPane leinwand;
    
    @FXML
    public SplitPane split;
    
    @FXML
    public VBox left;

    @FXML
    public VBox leftDown;
    
    @FXML
    public Slider sicherSlider;
    
    @FXML
    public Slider unsicherSlider;
      
    @FXML
    private Button visualisierenButton;
    
    private MainApp mainApp;
    private XLog visLog;
    private ObservableList<ObservableList<Object>> visualisierenListe = FXCollections.observableArrayList();
    
    private static double SURETHRESHOLD = 0.8;
    private static double QUESTIONMARKTHRESHOLD = 0.4;
    private static double PARALLELISMTHRESHOLD = 0.1;
    private static double PLACEEVALTHRESHOLD = 1.1;
    private static double PREPLACEEVALUATIONTHRESHOLD = 0.1;
    private static boolean MAXCLUSTERSIZEENABLED = false;
    
	protected JScrollPane scroll;
 
    /**
     * Konstruktor, der vor der Initialisierung geladen wird.
     */
    public VisualisierenViewController() {
    }

    /**
     * Initialisierung der Controller-Klasse. Die initialize-Methode wird automatisch aufgerufen,
     * nachdem die Fxml-Datei geladen wurde.
     */
    @FXML
    private void initialize() {
    	
    	kategorieSpalte.setCellValueFactory(new Callback<CellDataFeatures<ObservableList<Object>,String>,ObservableValue<String>>(){                    
	    public ObservableValue<String> call(CellDataFeatures<ObservableList<Object>, String> param) {                                                                                              
	        return new SimpleStringProperty(param.getValue().get(0).toString());                        
	    	}                    
	    });
    	kategorieSpalte.setSortable(false);
    	
    	einzeichnenSpalte.setCellValueFactory(new Callback<CellDataFeatures<ObservableList<Object>,Boolean>,ObservableValue<Boolean>>(){                    
	        public ObservableValue<Boolean> call(CellDataFeatures<ObservableList<Object>, Boolean> param) {                                                                                              
	            return  (SimpleBooleanProperty) param.getValue().get(1);                        
	        }  
    	});
    	
    	einzeichnenSpalte.setCellFactory(CheckBoxTableCell.forTableColumn(einzeichnenSpalte));
    	einzeichnenSpalte.setEditable(true);
    	einzeichnenSpalte.setSortable(false);

    	visualisierenTabelle.setEditable(true);
    	
    	visualisierenListeInitiieren();
    }
    	
	

	/**
     * Wird von der MainApp aufgerufen, um eine Referenz zu sich selbst zu vollziehen.
     * 
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
       
        // Vorhandene Kategorien in die Tabelle eintragen.
        visualisierenTabelle.setItems(visualisierenListe);
        visLog = (XLog) mainApp.getLog().clone();
    }
    
    // Fuegt alle vorhandenen Kategorien in eine Liste, 
    // sodass sie in der Einzeichnungstabelle verwendet werden kann.
	private void visualisierenListeInitiieren() {
		
		for(Kategorie k : MainApp.getKategorieListe()) {
			ObservableList<Object> reihe = FXCollections.observableArrayList();
			reihe.add(k.getKategorieName());
			reihe.add(new SimpleBooleanProperty(false));
			visualisierenListe.add(reihe);
		}		
	}
    
	// Funktion des Visualsieren-Buttons
	public void handleVisualisieren() {
		
		visLog.clear();
		visLog = (XLog) mainApp.getLog().clone();
		
		SURETHRESHOLD = sicherSlider.getValue();
		QUESTIONMARKTHRESHOLD = unsicherSlider.getValue();
		
		//falls keine Kategorien ausgewählt wurden, werden alle Aktivitäten angezeigt.
		boolean auswahlGetroffen = false;
		for(int i= 0; i < MainApp.getKategorieListe().size(); i++) {
			SimpleBooleanProperty tempAuswahlGetroffen = (SimpleBooleanProperty) visualisierenListe.get(i).get(1);
			if(tempAuswahlGetroffen.get()) {
				auswahlGetroffen = true;
				break;
			}
		}
		
		if(auswahlGetroffen) nichtAusgewaehlteAktivitaetenAggregieren();
		
		if(SURETHRESHOLD != 0) nachFuzzyModellMinen();
		else {
        	Alert alert = new Alert(AlertType.ERROR);
        	alert.setTitle("CatMiner");
        	alert.setHeaderText("Fehler");
        	alert.setContentText("Bitte geben Sie an, ab wann eine Kante als sicher gilt.");
        	
        	alert.showAndWait();
		}
	}
	
	//Aggregation von allen Aktivitaeten mit Kategoriesignifikanz 0
    private void nichtAusgewaehlteAktivitaetenAggregieren() {
    	
    	List<String> gewollteAktivitaeten = new ArrayList<>();
    	
    	for(int i= 0; i < MainApp.getKategorieListe().size(); i++) {
    		SimpleBooleanProperty tempBool = (SimpleBooleanProperty) visualisierenListe.get(i).get(1);
			if(tempBool.get()) {
				for(int n = 0; n < MainApp.getAktivitaetListe().size(); n++) {
					SimpleBooleanProperty tempBool2 = (SimpleBooleanProperty) MainApp.getZuweisungsListe().get(n).get(i + 1);
					if(tempBool2.get()){
						gewollteAktivitaeten.add((String) MainApp.getZuweisungsListe().get(n).get(0));
					}
				}
			}
		}
    	
    	List<String> nichtGewollteAktivitaeten = new ArrayList<>();
    	List<String> tempAktiString = new ArrayList<>();
    	
		for(Aktivitaet a: MainApp.getAktivitaetListe()) {
			tempAktiString.add(a.getAktivitaetName());
		}
    	
		for(String s: tempAktiString) {
			if(!gewollteAktivitaeten.contains(s)) nichtGewollteAktivitaeten.add(s);
		}
    	
		// Zur Aggregation werden alle Aktivitäten mit Kategoriesignifikanz 0 
		// als dieselbe Aktivität betrachtet.
		for(XTrace trace: visLog) {
			for(XEvent event: trace) {
				XAttributeMap map;
				XAttributeLiteralImpl clusterAk = new XAttributeLiteralImpl("Activity","Cluster: Nicht ausgewaehlt");
				XAttributeLiteralImpl clusterConcept = new XAttributeLiteralImpl("concept:name","Cluster: Nicht ausgewaehlt");
				
				map = event.getAttributes();
				String logAktivitaet = map.get("Activity").toString();
				if(nichtGewollteAktivitaeten.contains(logAktivitaet)){
					map.put("Activity", clusterAk);
					map.put("concept:name", clusterConcept);
					event.setAttributes(map);
				}
			}
		}
	}
    
    // Eigentliche Extraktion eines Prozessmodells unter Verwendung des FuzzyMiners.
	public void nachFuzzyModellMinen(){
    	XLog vorverarbeiteterLog = LogPreprocessor.preprocessLog(visLog);
    	
        XEventClassifier nameCl = new XEventNameClassifier();
        
        XLogInfo logInfo = XLogInfoFactory.createLogInfo(vorverarbeiteterLog, nameCl);
        HeuristicsMinerSettings hMS = new HeuristicsMinerSettings();
        hMS.setClassifier(nameCl);

        HybridCGMinerSettings cGSettings = new HybridCGMinerSettings(hMS, SURETHRESHOLD, QUESTIONMARKTHRESHOLD, PARALLELISMTHRESHOLD);
        XLog gefilterterLog = LogFilterer.filterLogByActivityFrequency(vorverarbeiteterLog, logInfo, cGSettings);
          
        HybridPNMinerSettings pNSettings = new HybridPNMinerSettings(PREPLACEEVALUATIONTHRESHOLD, PLACEEVALTHRESHOLD, MAXCLUSTERSIZEENABLED);
        HybridCGMiner miner = new HybridCGMiner(gefilterterLog, gefilterterLog.getInfo(cGSettings.getHmSettings().getClassifier()), cGSettings);
        System.out.println("*********** Start mining the FuzzyCausalGraph ***********");
        HybridCausalGraph fCG = miner.mineFCG(cGSettings);
        //System.out.println(fCG);
        
        HybridPetrinet fPN = CGToHybridPN.fuzzyCGToFuzzyPN(fCG, gefilterterLog, pNSettings);       
        ViewSpecificAttributeMap map = new ViewSpecificAttributeMap();

        darstellungModellieren(map, fPN, fCG.getActivityFrequencyMap());
        
        SwingNode swingScroll = new SwingNode();
         
        try {
        	
        	ProMJGraph graph = HybridPetrinetVisualizer.createJGraph(fPN, map, null);

        	HybridPetrinetVisualization petriNetzViusalisierung = new HybridPetrinetVisualization(graph);
        
        	scroll = petriNetzViusalisierung.getScroll();

        	SlickerDecorator decorator = SlickerDecorator.instance();
        	decorator.decorate(scroll, Color.white, Color.gray, Color.lightGray);
       
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        SwingUtilities.invokeLater(() -> {
        	
            swingScroll.setContent(scroll);

            Platform.runLater(() -> {
            	
            	ZoomableScrollPane zoomPane = new ZoomableScrollPane(swingScroll);
            	
            	AnchorPane.setTopAnchor(zoomPane, 0.0);
            	AnchorPane.setLeftAnchor(zoomPane, 0.0);
            	AnchorPane.setRightAnchor(zoomPane, 0.0);
            	AnchorPane.setBottomAnchor(zoomPane, 0.0);
            	
            	zoomPane.setPrefHeight(598);
            	zoomPane.setPrefWidth(592);
            	
            	zoomPane.setMinHeight(598);
            	zoomPane.setMinWidth(592);
            	
            	leinwand.getChildren().addAll(zoomPane);          
            });
        });
    }
	
	// Veränderung der Darstellung des FuzzyModells.
	private void darstellungModellieren(ViewSpecificAttributeMap map, 
			HybridPetrinet fPN, Map<String, Integer> activityFrequencyMap) {
		
        final String SIZE = "size";
        final String PORTOFFSET = "portoffset";
        final String FILLCOLOR = "ProM_Vis_attr_fillcolor";
        final String GRADIENTCOLOR = "ProM_Vis_attr_gradientcolor";
        final String AUTOSIZE = "ProM_Vis_attr_autosize";
        final String LINEWIDTH = "ProM_Vis_attr_lineWidth";
        final String EDGECOLOR = "ProM_Vis_attr_edgeColor";
        final String SHAPE = "ProM_Vis_attr_shape";     
        final String TOOLTIP = "ProM_Vis_attr_tooltip";    
        final String LABEL = "ProM_Vis_attr_label";
        final String LABELVERTICALALIGNMENT = "ProM_Vis_attr_labelVerticalAlignment";          
        
        //Linienbreite der Kanten.
        final float weiteSicher = 2;
		final float weiteUnsicher = 1;
        
        Random random = new Random();
        
        final String PREF_ORIENTATION = "ProM_Vis_attr_orientation";
        
        List<Color> farbPalette = new ArrayList<Color>();
        farbPalette.add(new Color(80,182,207,135));
        farbPalette.add(Color.yellow);
        farbPalette.add(Color.green);
        farbPalette.add(Color.red.brighter());
        farbPalette.add(Color.orange);
        farbPalette.add(Color.magenta);
        farbPalette.add(Color.blue);
        farbPalette.add(Color.pink);
        
        //Zufällige Farben erstellen, falls mehr als 8 Kategorien erstellt wurden.
        if(farbPalette.size() < MainApp.getKategorieListe().size()) {
        	for(int i = farbPalette.size(); i <= MainApp.getKategorieListe().size(); i++) {
        		float farbton = random.nextFloat();
        		//Farbsaettigung zwischen 0,1 und 0,3
        		float saettigung = (random.nextInt(2000) + 1000) / 10000f;
        		float helligkeit = 0.9f;
        		Color zufallFarbe = Color.getHSBColor(farbton, saettigung, helligkeit);
        		farbPalette.add(zufallFarbe);	 
        	} 	
        }
        
		for (AttributeMapOwner node : fPN.getGraph().getNodes()) {
			map.putViewSpecific(node, SIZE, new Dimension(50, 50));
			map.putViewSpecific(node, PORTOFFSET,
					new Point2D.Double(GraphConstants.PERMILLE / 2, GraphConstants.PERMILLE / 2));
			map.putViewSpecific(node, FILLCOLOR, new Color(227,227,227,214));
			map.putViewSpecific(node, LABELVERTICALALIGNMENT, 1);
			
			
			map.putViewSpecific(node, AUTOSIZE, true);
			map.putViewSpecific(node, PREF_ORIENTATION, SwingConstants.SOUTH);
			
			String knotenName = node.toString();
			if(knotenName.contains("start") || knotenName.contains("end")) {
				map.putViewSpecific(node, SHAPE,new Ellipse());
			}
		}
        
		//Knotenfarben nach Kategorien bestimmen.
		for (AttributeMapOwner node : fPN.getGraph().getNodes()) {
			int zuweisungsErgebnis = farbeZuweisen(node);
			if(zuweisungsErgebnis != 404) {
				if(zuweisungsErgebnis != 202) {
					map.putViewSpecific(node, GRADIENTCOLOR, farbPalette.get(zuweisungsErgebnis));
				}
				else {
					map.putViewSpecific(node, GRADIENTCOLOR, Color.lightGray.brighter());
				}
			} else {
				map.putViewSpecific(node, GRADIENTCOLOR, Color.gray);
			}
		}
				
		//Kanten modellieren.
		for (AttributeMapOwner edge : fPN.getGraph().getEdges()) {	
			if(edge.toString().contains("?")) {
				map.putViewSpecific(edge, LINEWIDTH, weiteUnsicher);
				map.putViewSpecific(edge, EDGECOLOR, Color.gray);
				map.putViewSpecific(edge, LABEL, ""); 
				map.putViewSpecific(edge, TOOLTIP, "Unsichere Kante");
			} else {
				map.putViewSpecific(edge, LINEWIDTH, weiteSicher);
				map.putViewSpecific(edge, TOOLTIP, "Sichere Kante");
			}
		}
	}
	
	
	//Falls eine Kategorie zugewiesen wurde, wird dieser eine Farbe aus Farbpalette zugewiesen.
	//202 steht fuer Aktivitaeten denen keine Kategorie zugewiesen wurde.
	//404 steht fuer keine Aktivitaeten. (start, ende, etc.)
	private int farbeZuweisen(AttributeMapOwner node) {
		if(node.toString().equals("Cluster: Nicht ausgewaehlt")) return 202;
		for(int i = 0; i < MainApp.getAktivitaetListe().size(); i++) {
			if(node.toString().equals(MainApp.getAktivitaetListe().get(i).getAktivitaetName())) {
				for(int n = 0; n < MainApp.getKategorieListe().size(); n++) {
					SimpleBooleanProperty kategorieZugewiesen = (SimpleBooleanProperty) MainApp.getZuweisungsListe().get(i).get(n + 1);
					if(kategorieZugewiesen.get()) return n;
				}
				return 202;
			}
		}	
		return 404;	
	}
}
