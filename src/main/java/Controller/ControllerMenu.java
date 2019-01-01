package Controller;

import Indexer.WrieFile;
import Model.ModelMenu;
import Searcher.QuerySol;
import View.CityItem;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import View.IR_MenuView;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.trim;

/**
 * Controller
 */
public class ControllerMenu implements Observer {

    private FXMLLoader fxmlLoader;
    private Stage stage;
    private Parent root;

    private IR_MenuView ir_menuView;
    private ModelMenu ir_modelMenu;
    private String[] propertyKeys = {"data.set.path", "save.files.path"};

    private DoubleProperty progress;

    private long start;
    private long end;

    private boolean savePath = false;
    private boolean dataPath = false;
    private boolean searchQueriesLoaded = false;


    private ArrayList<String> selectedCities = new ArrayList<>();
    private ArrayList<String> citiesList = null;


    /**
     * ctor
     */
    public ControllerMenu() {
        stage = new Stage();
        fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("menu/ir_menu_partB.fxml"));
        try {
            root = fxmlLoader.load();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("OMFG!");
            alert.setHeaderText("Couldn't load FXML!");
            alert.setContentText("try to run the app again");
            alert.showAndWait();
        }
        Scene scene = new Scene(root);
        scene.getStylesheets().add("menu/style_menu.css");
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/menu/images/logo.png")));
        stage.setTitle("The Search Engine that Hits the Spot");
        ir_menuView = fxmlLoader.getController();
        ir_modelMenu = new ModelMenu();
        ir_modelMenu.addObserver(this);
        ir_menuView.addObserver(this);
        progress = new SimpleDoubleProperty(0);
        treeViewForResultHandle();
        setPartBState(true);
    }

    /**
     * changing the Disable state for all initial GUI widgets as the argument the function receive
     *
     * @param state - the state the Disable state to change to
     */
    private void setPartBState(boolean state) {
        ir_menuView.semantic_checkBox.setDisable(state);
        ir_menuView.freeSearch_textField.setDisable(state);
        ir_menuView.browse_queries_btn.setDisable(state);
        ir_menuView.cities_btn.setDisable(state);
        ir_menuView.totalRickAll_toggle.setDisable(state);
        ir_menuView.search_btn.setDisable(state);
    }

    /**
     * Adding a Listener for the results treeView.
     * connecting the listener to the Objects from the model:
     * docsEntitites and docsResult for retrieving the needed info
     */
    private void treeViewForResultHandle() {
        ir_menuView.result_treeView.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<TreeItem<String>>() {

                    @Override
                    public void changed(
                            ObservableValue<? extends TreeItem<String>> observable,
                            TreeItem<String> old_val, TreeItem<String> new_val) {
                        TreeItem<String> selectedItem = new_val;
                        ir_menuView.entities_textArea.setEditable(true);
                        ir_menuView.entities_textArea.clear();
                        String[] entities = ir_modelMenu.getDocsEntities().get(selectedItem.getValue());
                        if (entities != null) {
                            StringBuilder text = new StringBuilder();
                            int i = 1;
                            for (String s : entities) {
                                text.append(i++).append(". ").append(s).append("\n");
                            }
                            ir_menuView.entities_textArea.setText(text.toString());
                            ir_menuView.entities_textArea.setEditable(false);
                        }
                    }
                });
    }


    /**
     * @param selectedDirectoryPath the path from the FileChooser
     */
    private void loadCorpusPath(String selectedDirectoryPath) {
        PropertiesFile.putProperty(propertyKeys[0], selectedDirectoryPath);
        boolean corpusCheck = new File(selectedDirectoryPath, "corpus").exists();
        boolean stopWords = new File(selectedDirectoryPath, "stop_words.txt").exists();
        if ((!corpusCheck && !stopWords)) {
            ir_menuView.data_textField.setText("stop_words.txt and corpus folder are not existing in this path");
            ir_menuView.start_btn.setDisable(true);
        } else if (!corpusCheck) {
            ir_menuView.data_textField.setText("corpus folder not existing in this path");
            ir_menuView.start_btn.setDisable(true);
        } else if (!stopWords) {
            ir_menuView.data_textField.setText("stop_words.txt not existing in this path");
            ir_menuView.start_btn.setDisable(true);
        } else {
            if (selectedDirectoryPath.endsWith("\\")) {
                PropertiesFile.putProperty(propertyKeys[0], selectedDirectoryPath);
            } else {
                PropertiesFile.putProperty(propertyKeys[0], selectedDirectoryPath + "\\");
            }
            ir_menuView.data_textField.setText(PropertiesFile.getProperty(propertyKeys[0]));
            dataPath = true;
            checkIfCanStart();
        }
    }


    /**
     * @param selectedDirectoryPath the path from the FileChooser
     */
    private void loadTargetPath(String selectedDirectoryPath) {
        if (selectedDirectoryPath.endsWith("\\"))
            PropertiesFile.putProperty(propertyKeys[1], selectedDirectoryPath);
        else
            PropertiesFile.putProperty(propertyKeys[1], selectedDirectoryPath + "\\");
        ir_menuView.save_textField.setText(PropertiesFile.getProperty(propertyKeys[1]));
        ir_menuView.dict_btn.setDisable(false);
        ir_menuView.read_dict_btn.setDisable(false);
        ir_menuView.reset_btn.setDisable(false);
        savePath = true;
        checkIfCanStart();
    }

    /**
     * Opens the directory chooser
     *
     * @param operation : 1 -> Save; 0 Load
     */
    private void loadPathFromDirectoryChooser(int operation) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            if (operation == 0) loadCorpusPath(selectedDirectory.getAbsolutePath());
            else {
                loadTargetPath(selectedDirectory.getAbsolutePath());
                ir_menuView.read_dict_btn.setDisable(false);
            }
        }
    }

    /**
     * opens an fileChooser for the queries file.
     */
    public void loadQueriesFile() {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            String path = selectedFile.getAbsolutePath();
            PropertiesFile.putProperty("queries.file.path", path);
            searchQueriesLoaded = true;
        } else {
            searchQueriesLoaded = false;
        }
    }

    /**
     * checks if dir contains stop_words and corpus
     */
    private void checkIfCanStart() {
        if (dataPath && savePath) {
            ir_menuView.start_btn.setDisable(false);
        }
    }

    /**
     * Shows an Alert window
     *
     * @param title       - window's title
     * @param information - info
     * @param content     - Alert content
     */
    public void showAlert(String title, String information, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(information);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o.equals(ir_menuView)) {
            if (arg.equals("resetLabel")) {
                clearSummary();
            } else if (arg.equals("stem")) {
                if (ir_menuView.stemmer_checkBox.isSelected()) {
                    PropertiesFile.putProperty("stem.mode", "1");
                } else PropertiesFile.putProperty("stem.mode", "0");
            } else if (arg.equals("semantic")) {
                if (ir_menuView.semantic_checkBox.isSelected()) {
                    PropertiesFile.putProperty("semantic.mode", "1");
                } else PropertiesFile.putProperty("semantic.mode", "0");
            } else if (arg.equals("totalRickall")) {
                if (ir_menuView.totalRickAll_toggle.isSelected()) {
                    PropertiesFile.putProperty("total.rickall", "1");
                    ir_menuView.summary_lbl.setVisible(true);
                    ir_menuView.summary_lbl.setText("\n\tPlease pay attention that TotalRickall\n\toption works only with query files.");
                } else {
                    PropertiesFile.putProperty("total.rickall", "0");
                    ir_menuView.summary_lbl.setText("");
                }
            } else if (arg.equals("cities")) {
                // TODO: 31/12/2018 deal with cities so it will work!
                dealWithCities();
            } else if (arg.equals("start")) {
                this.update(o, "stem");
                setSceneBeforeStart();
                Thread thread = new Thread(() -> ir_modelMenu.start());
                start = System.currentTimeMillis();
                thread.setDaemon(true);
                thread.start();
            } else if (arg.equals("browse")) {
                loadPathFromDirectoryChooser(0);
            } else if (arg.equals("target")) {
                loadPathFromDirectoryChooser(1);
            } else if (arg.equals("reset")) {
                ir_modelMenu.reset();
            } else if (arg.equals("show")) {
                showDictionary();
                ir_menuView.summary_lbl.setVisible(false);
            } else if (arg.equals("read")) {
                ir_menuView.summary_lbl.setText("Please wait while the dictionaries read to memory");
                readDictionary();
                ir_menuView.summary_lbl.setText("");
                ir_menuView.summary_lbl.setVisible(false);
            } else if (arg.equals("search_single")) {
                String query = setQueryForSearch();
                if (!query.isEmpty()) {
                    setSummaryForSearch(true);
                    Thread thread = new Thread(() -> ir_modelMenu.search(query, selectedCities));
                    thread.setDaemon(true);
                    thread.start();
                } else {
                    setSummaryForSearch(false);
                }
            } else if (arg.equals("queryFile")) {
                setSceneForResults(false);
                loadQueriesFile();
                if (searchQueriesLoaded) {
                    setSummaryForSearch(true);
                    Thread thread = new Thread(() -> ir_modelMenu.multiSearch(selectedCities));
                    thread.setDaemon(true);
                    thread.start();
                }
            } else if (arg.equals("save_results")) {
                saveResults();
            }
        } else if (o.equals(ir_modelMenu)) {
            if (arg.equals("index_done")) {
                ir_menuView.summary_lbl.setVisible(true);
                ir_menuView.stemmer_checkBox.setDisable(false);
                end = System.currentTimeMillis();
                Platform.runLater(this::addSummaryToLabel);
            } else if (arg.equals("search_done")) {
                Platform.runLater(this::clearSummary);
                Platform.runLater(this::showResultView);
            }
        }
    }

    /**
     * updating the languages list: docs_language, by requesting the collection from the model
     */
    private void addLanguages() {
        ir_menuView.docs_language.getItems().clear();
        ir_menuView.docs_language.setDisable(false);
        TreeSet<String> languages = ir_modelMenu.getLanguages();
        while (!languages.isEmpty()){
            ir_menuView.docs_language.getItems().addAll(languages.pollFirst());
        }
        if (!ir_menuView.docs_language.getItems().isEmpty())
            ir_menuView.docs_language.setPromptText("Please Choose Language");
    }

    /**
     * setting the entered single query for search
     *
     * @return
     */
    private String setQueryForSearch() {
        String query = ir_menuView.freeSearch_textField.getText();
        query = trim(query);
        return query;
    }

    /**
     * @param start
     */
    private void setSummaryForSearch(boolean start) {
        ir_menuView.summary_lbl.setVisible(true);
        if (start)
            ir_menuView.summary_lbl.setText("\n\n\t\t\tPlease wait while Searching");
        else
            ir_menuView.summary_lbl.setText("\n\n\t\t\tPlease type something to search...");
    }

    private void clearSummary() {
        ir_menuView.summary_lbl.setText("");
    }

    /**
     * making all the Objects that connected to showing the search result
     * visible or not visible.
     *
     * @param status the state of visibility
     */
    private void setSceneForResults(boolean status) {
        ir_menuView.entities_textArea.setEditable(false);
        ir_menuView.result_lbl.setVisible(status);
        ir_menuView.save_lbl.setVisible(status);
        ir_menuView.result_treeView.setVisible(status);
        ir_menuView.save_results_img.setVisible(status);
        ir_menuView.entities_textArea.setVisible(status);
        ir_menuView.entities_textArea.setPromptText("Please select a document to see the most frequent entities");
    }

    /**
     * generating the treeView as need with query number and the docs result.
     * only if user selected the entities then the textArea will be visible with the Entities info.
     */
    private void showResultView() {
        setSceneForResults(true);
        // init the result list
        TreeItem<String> rootItem = new TreeItem<String>("Results");
        rootItem.setExpanded(true);
        int i = 1;
        ArrayList<ArrayList<String>> docsResult = ir_modelMenu.getDocsResultsAsArray();
        for (ArrayList<String> result : docsResult) {
            Node queryIcon = new ImageView(new Image(getClass().getResourceAsStream("/menu/images/searchQuery.png"), 15, 15, false, false));
            TreeItem<String> queryItem = new TreeItem<String>("Query " + i++, queryIcon);
            queryItem.setExpanded(true);
            rootItem.getChildren().add(queryItem);
            for (String s : result) {
                Node docIcon = new ImageView(new Image(getClass().getResourceAsStream("/menu/images/doc.png"), 15, 15, false, false));
                TreeItem<String> docItem = new TreeItem<String>(s, docIcon);
                queryItem.getChildren().add(docItem);
            }
        }
        ir_menuView.result_treeView.setRoot(rootItem);
    }


    /**
     * opens new stage with a list containing all the cities that received from master.
     * multi selection enabled and after confirm button pressed:
     * selectedCities updated and containing all the selectedCities in the list.
     */
    private void dealWithCities() {
        selectedCities.clear();
        ListView<CityItem> listView = new ListView<>();
        for (String city : this.citiesList) {
            CityItem item = new CityItem(city, false);

            item.onProperty().addListener((obs, wasOn, isNowOn) -> {
                if (isNowOn){
                    selectedCities.add(item.getName());
                } else {
                    selectedCities.remove(item.getName());
                }
            });
            listView.getItems().add(item);
        }
        listView.setCellFactory(CheckBoxListCell.forListView(new Callback<CityItem, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(CityItem item) {
                return item.onProperty();
            }
        }));
        Stage citiesStage = new Stage();
        Button confirm = new Button("Confirm");
        confirm.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                citiesStage.close();
            }
        });
        BorderPane root = new BorderPane(listView, null, null, confirm, null);
        Scene scene = new Scene(root, 250, 400);

        citiesStage.setScene(scene);
        citiesStage.showAndWait();
    }


    /**
     * Read Dictionary to RAM
     */
    private void readDictionary() { // TODO: 01/01/2019 check that really all the dectionaries are read as needed!!!
        String dicPath = ir_modelMenu.getDicsPath();
        try {
            File file = new File(dicPath);
            if (file.isDirectory()) {
                if (ir_modelMenu.readDictionaries(dicPath)) {
                    this.citiesList = ir_modelMenu.getCitiesList();
                    addLanguages();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Read Dictionaries");
                    alert.setHeaderText("Dictionaries are read and now available to use");
                    alert.showAndWait();
                    setPartBState(false);
                } else throw new Exception();
            } else throw new Exception();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("OMG!");
            alert.setHeaderText("Couldn't read Dictionaries. try showing them");
            alert.showAndWait();
        }
    }

    /**
     * save the search results to the Disk in required format for treceval use
     */
    public void saveResults() {
        try {
            ArrayList<QuerySol> results = ir_modelMenu.getDocsResult();
            FileChooser fileChooser = new FileChooser();
            File file = null;
            try {
                file = new File(PropertiesFile.getProperty("queries.file.path"));
            } catch (Exception e) {
                file = new File(PropertiesFile.getProperty("save.files.path"));
            }
            if (!file.isDirectory()) {
                fileChooser.setInitialDirectory(new File(file.getParent()));
            } else {
                fileChooser.setInitialDirectory(new File(file.getAbsolutePath()));
            }
            fileChooser.setInitialFileName("results.txt");
//            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("text (*.txt)", ".txt"));
            file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    file.delete();
                    WrieFile.writeQueryResults(results, file.getParent(), file.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the Dictionary.
     * if program is ran by jar - dictionary won't be shown as notepad++
     */
    private void showDictionary() {
        StringBuilder stringBuilder = new StringBuilder(ir_modelMenu.getDicsPath());
        String dicPath = stringBuilder.append("\\1. Term Dictionary with").append(PropertiesFile.getProperty("stem.mode").equals("0") ? "out " : " ").append("stemming").toString();
        stringBuilder.setLength(0);
        File file = new File(dicPath);
        if (file.isFile()) {
            try {
                file.setWritable(false);
                Runtime runtime = Runtime.getRuntime();
                Process process = runtime.exec("./Notepad++\\Notepad++.exe " + dicPath);
                new SimpleBooleanProperty(false).addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        if (newValue.booleanValue())
                            file.setWritable(true);
                    }
                });
            } catch (IOException ioe) {
                try {
                    Stage dicShow = new Stage();
                    dicShow.setTitle("Term Dictionary");
                    ListView<String> termsDic = new ListView<>();
                    termsDic.setPrefSize(300, 400);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(dicPath), StandardCharsets.UTF_8));
                    String s = bufferedReader.readLine();
                    while (s != null) {
                        termsDic.getItems().add(s);
                        s = bufferedReader.readLine();
                    }
                    Pane root = new Pane();
                    root.getChildren().add(termsDic);
                    dicShow.setScene(new Scene(root, 300, 400));
                    dicShow.show();
                    bufferedReader.close();
                } catch (Exception e) {
                    showAlert(
                            "Wrong Path",
                            "Couldn't Find The Requested Dictionary",
                            "try to change path to be the directory that contains:" +
                                    " \"Dictionaries with/out stemming\"" +
                                    "\nand click on the \'show\' button again");
                }
            }
        } else {
            showAlert(
                    "Wrong Path",
                    "Couldn't Find The Requested Dictionary",
                    "try to change path to be the directory that contains:" +
                            " \"Dictionaries with/out stemming\"" +
                            "\nand click on the \'show\' button again");
        }
    }


    /**
     * Sets the stage to be "blocked" from use before starting indexing
     */
    private void setSceneBeforeStart() {
        ir_menuView.summary_lbl.setAlignment(Pos.CENTER);
        ir_menuView.summary_lbl.setText("IN PROCESS!!");
        ir_menuView.summary_lbl.setVisible(true);
        ir_menuView.start_btn.setDisable(true);
        ir_menuView.dict_btn.setDisable(true);
        ir_menuView.read_dict_btn.setDisable(true);
        ir_menuView.browse_btn.setDisable(true);
        ir_menuView.save_btn.setDisable(true);
        ir_menuView.reset_btn.setDisable(true);
        ir_menuView.stemmer_checkBox.setDisable(true);
        ir_menuView.progressbar.setVisible(true);
        ir_menuView.progress_lbl.setVisible(true);
        ir_menuView.docs_language.setDisable(true);
        ir_menuView.docs_language.getItems().clear();
        progress.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                double val = newValue.doubleValue();
                if (val > 1) {
                    val -= 1;
                    double finalVal = val;
                    Platform.runLater(() -> ir_menuView.progress_lbl.setText("Merging temporary files\t%" + (int) (finalVal * 100)));
                } else {
                    double finalVal = val;
                    Platform.runLater(() -> ir_menuView.progress_lbl.setText("Creating temporary files\t%" + (int) (finalVal * 100)));
                }
                ir_menuView.progressbar.progressProperty().set(val);
            }
        });
        progress.bind(ir_modelMenu.getProgress());
    }

    /**
     * edits the stage to be set after done indexing
     */
    private void addSummaryToLabel() {
        ir_menuView.dict_btn.setDisable(false);
        ir_menuView.read_dict_btn.setDisable(false);
        ir_menuView.browse_btn.setDisable(false);
        ir_menuView.save_btn.setDisable(false);
        ir_menuView.reset_btn.setDisable(false);
        ir_menuView.summary_lbl.setAlignment(Pos.TOP_LEFT);
        String time = "Time for the whole operation: " + (end - start) / 1000 + " seconds";
        String numOfterms = "Total number of term: " + ir_modelMenu.getNumOfTerms();
        String numOfDocs = "Total number of Documents: " + ir_modelMenu.getDocCount();
        String summary = "Summary:\n" +
                "\t" + time + "\n" +
                "\t" + numOfterms + "\n" +
                "\t" + numOfDocs;
        ir_menuView.summary_lbl.setText(summary);
        ir_menuView.progressbar.setVisible(false);
        ir_menuView.progress_lbl.setVisible(false);
        addLanguages();
        loadCorpusPath(PropertiesFile.getProperty("data.set.path"));
        loadTargetPath(PropertiesFile.getProperty("save.files.path"));
    }

    /**
     * Shows the application window
     */
    public void showStage() {
        stage.show();
    }
}
