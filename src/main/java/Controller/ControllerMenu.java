package Controller;

import Indexer.WrieFile;
import Model.ModelMenu;
import Searcher.QuerySol;
import Tests.Treceval_cmd;
import TextContainers.LanguagesInfo;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import View.IR_MenuView;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.lang3.StringUtils.substring;

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
        ir_menuView = fxmlLoader.getController();
        ir_menuView.summary_lbl.setText("Summary:");
        ir_modelMenu = new ModelMenu();
        ir_modelMenu.addObserver(this);
        ir_menuView.addObserver(this);
        progress = new SimpleDoubleProperty(0);
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
            else loadTargetPath(selectedDirectory.getAbsolutePath());
        }
    }

    public void loadQueriesFile() {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            String path = selectedFile.getAbsolutePath();
            PropertiesFile.putProperty("queries.file.path", path);
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
            if (arg.equals("stem")) {
                if (ir_menuView.stemmer_checkBox.isSelected()) {
                    PropertiesFile.putProperty("stem.mode", "1");
                } else PropertiesFile.putProperty("stem.mode", "0");
            } else if (arg.equals("entities")) {
                if (ir_menuView.stemmer_checkBox.isSelected()) {
                    PropertiesFile.putProperty("entities.mode", "1");
                } else PropertiesFile.putProperty("entities.mode", "0");
            } else if (arg.equals("semantic")) {
                if (ir_menuView.stemmer_checkBox.isSelected()) {
                    PropertiesFile.putProperty("semantic.mode", "1");
                } else PropertiesFile.putProperty("semantic.mode", "0");
            } else if (arg.equals("cities")) {
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
            } else if (arg.equals("save")) {

            } else if (arg.equals("reset")) {
                ir_modelMenu.reset();
            } else if (arg.equals("show")) {
                //TODO - give the content to save
//                saveResults(null);
                showDictionary();
                ir_menuView.summary_lbl.setVisible(false);
            } else if (arg.equals("read")) {
                loadTargetPath("C:\\Users\\User\\Documents\\SearchEngineTests");
                loadCorpusPath("C:\\Users\\User\\Documents\\SearchEngineTests");
                readDictionary();
                crazyBionicAstroFantasticWeightsTest("C:\\Users\\User\\Documents\\SearchEngineTests");
//                searchQueries();
//                testBM25();
                ir_menuView.summary_lbl.setVisible(false);
            } else if (arg.equals("freeLangSearch")) {
                ArrayList<String> languages = new ArrayList<>();
                // todo - implement
                ir_modelMenu.search("query", languages);
            } else if (arg.equals("queryFile")) {
                update(o, "read");
                searchQueries();
            }
        } else if (o.equals(ir_modelMenu)) {
            if (arg.equals("done")) {
                ir_menuView.summary_lbl.setVisible(true);
                ir_menuView.stemmer_checkBox.setDisable(false);
                end = System.currentTimeMillis();
                Platform.runLater(this::addSummaryToLabel);
            } else if (arg.equals("search_done")) {
                // todo - implement
            }
        }
    }

    private void dealWithCities() {
        selectedCities.clear();
        ListView<String> listView = new ListView<>();
        for (String city : this.citiesList) {
            listView.getItems().add(city);
        }
        listView.setCellFactory(CheckBoxListCell.forListView(new Callback<String, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(String item) {
                BooleanProperty observable = new SimpleBooleanProperty();
                observable.addListener((obs, wasSelected, isNowSelected) -> {
                    if (isNowSelected)
                        selectedCities.add(item);
                    else
                        selectedCities.remove(item);
                });
                return observable;
            }
        }));
        Stage citiesStage = new Stage();
        Button confirm = new Button("Confirm");
        BorderPane root = new BorderPane(listView, null, null, confirm, null);
        Scene scene = new Scene(root, 250, 400);

        confirm.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                citiesStage.close();
            }
        });
        citiesStage.setScene(scene);
        citiesStage.showAndWait();
    }

    private void searchQueries() {
        ArrayList<String> cities = new ArrayList<>();
//        loadQueriesFile();
        PropertiesFile.putProperty("queries.file.path", "C:\\Users\\User\\Documents\\SearchEngineTests\\queries.txt");
        ArrayList<QuerySol> querySols = ir_modelMenu.multiSearch(cities);
//        for (QuerySol querySol : querySols) {
//            System.out.print("\n" + querySol.getqNum() + ": " + querySol.getTitle() + ": ");
//            for (String s : querySol.getSols()) {
//                System.out.print(s + ", ");
//            }
//        }
    }


    /**
     * Read Dictionary to RAM
     */
    private void readDictionary() {
        String dicPath = ir_modelMenu.getDicsPath();
        try {
            File file = new File(dicPath);
            if (file.isDirectory()) {
                if (ir_modelMenu.readDictionaries(dicPath)) {
                    this.citiesList = ir_modelMenu.getCitiesList();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Read Dictionaries");
                    alert.setHeaderText("Dictionaries are read and now available to use");
//                    alert.showAndWait(); todo - bring back the confirmation
                } else throw new Exception();
            } else throw new Exception();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("OMG!");
            alert.setHeaderText("Couldn't read Dictionaries. try showing them");
            alert.showAndWait();
        }
    }


    //TODO- fix function and all the derived functions

    /**
     * save the results to the Disk
     */
    public void saveResults(ArrayList<QuerySol> results) {
        try {
            FileChooser fileChooser = new FileChooser();
            File file = null;
            try {
                file = new File(PropertiesFile.getProperty("queries.file.path"));
            } catch (Exception e) {
                file = new File(PropertiesFile.getProperty("save.files.path"));
            }
            fileChooser.setInitialDirectory(file);
            fileChooser.setInitialFileName("results");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text", ".txt", ".post"));
            file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
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
     * Sets the stage to be "blocked" fro use before starting indexing
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
        ir_menuView.docs_language.setDisable(false);
        ir_menuView.docs_language.getItems().addAll(LanguagesInfo.getInstance().getLanguagesAsList());
        if (!ir_menuView.docs_language.getItems().isEmpty())
            ir_menuView.docs_language.setPromptText("Please Choose Language");
        ir_menuView.docs_language.setDisable(false);
        loadCorpusPath(PropertiesFile.getProperty("data.set.path"));
        loadTargetPath(PropertiesFile.getProperty("save.files.path"));
    }

    /**
     * Shows the application window
     */
    public void showStage() {
        double start = System.currentTimeMillis();
//        stage.show();
        ir_menuView.browse_queries_btn.fire();//todo - remove fire button
//        update(ir_menuView,"read");
        System.out.println("\n");
        System.out.println((System.currentTimeMillis() - start) / 1000 + " seconds");
    }


    private void crazyBionicAstroFantasticWeightsTest(String directory) {
        try {
            ArrayList<String> queries = new ArrayList<>();
            ArrayList<String> queryNums = new ArrayList<>();
            File file = new File(directory + "\\queries.txt");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line = bufferedReader.readLine();
            while (line != null) {
                if (startsWith(line, "<num> Number: ")) {
                    queryNums.add(trim(substring(line, 13)));
                }
                if (startsWith(line, "<title> ")) {
                    queries.add(trim(substring(line, 7)));
                }
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            double bm25 =  PropertiesFile.getPropertyAsDouble("bm25.weight"),
                    e = PropertiesFile.getPropertyAsDouble("e"),
                    maxBM25 = PropertiesFile.getPropertyAsDouble("maxBM25");
            Treceval_cmd tester = new Treceval_cmd();
            double[] maxVals = new double[7];
//            double[] maxVals = tester.getResultRanked();
//            Files.delete(Paths.get(directory + "\\results.txt"));
            CSVPrinter printer = new CSVPrinter(Files.newBufferedWriter(Paths.get(directory + "\\weights.csv")), CSVFormat.DEFAULT.withHeader("R-Percision", "Percision", "Recall", "Rank","Retrieved","Relevant","Relevant Returned", "BM25", "Wu Palmer", "resnik", "jiang", "lin"));
            printer.flush();
            double startTests = System.currentTimeMillis();
            int iter= 0;
            for (double bm25i = bm25; bm25i <= maxBM25; bm25i += e) {
                for (double wupi = 0; wupi+bm25i <= 1; wupi += e) {
                    for (double resniki = 0; resniki+wupi+bm25i <= 1; resniki += e) {
                        for (double jiangi = 0; jiangi+resniki+wupi+bm25i <= 1 ; jiangi += e) {
                            for (double lini = 1-(jiangi+resniki+wupi+bm25i); lini+jiangi+resniki+wupi+bm25i == 1; lini += e) {
                                bm25i = Math.round(bm25i*1e2) / 1e2;
                                resniki = Math.round(resniki*1e2) / 1e2;
                                jiangi = Math.round(jiangi*1e2) / 1e2;
                                wupi = Math.round(wupi*1e2) / 1e2;
                                lini = Math.round(lini*1e2) / 1e2;
                                tester.simulateSearch2Treceval(queries, queryNums, bm25i, wupi, resniki, jiangi, lini);
                                double[] newVals = tester.getResultRanked();
                                if (newVals[3] > maxVals[3]) {
                                    maxVals = newVals.clone();
                                    printer.printRecord(newVals[0], newVals[1], newVals[2], newVals[3],newVals[4], newVals[5],newVals[6], bm25i, wupi, resniki, jiangi, lini);
                                    printer.flush();
                                }
                                Files.delete(Paths.get(directory + "\\results.txt"));
                                double currentTime = System.currentTimeMillis();
                                iter++;
                                System.out.println((currentTime - startTests) / 1000 + "\tbm25 = " + bm25i + "\twup = " + wupi + "\tresnik = " + resniki + "\tjiang = " + jiangi +"\tlin = " + lini + "\tsum = " + (lini+jiangi+resniki+wupi+bm25i) + "\t\t" + Arrays.toString(newVals));
                            }
                        }
                    }
                }
            }
            System.out.println();
            System.out.println(iter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void testBM25(String directory) {
        try {
            ArrayList<String> queries = new ArrayList<>();
            ArrayList<String> queryNums = new ArrayList<>();
            File file = new File(directory + "\\queries.txt");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line = bufferedReader.readLine();
            while (line != null) {
                if (startsWith(line, "<num> Number: ")) {
                    queryNums.add(trim(substring(line, 13)));
                }
                if (startsWith(line, "<title> ")) {
                    queries.add(trim(substring(line, 7)));
                }
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            double k = PropertiesFile.getPropertyAsDouble("k"), b = PropertiesFile.getPropertyAsDouble("b"), d = PropertiesFile.getPropertyAsDouble("d"), f = PropertiesFile.getPropertyAsDouble("f"), e = PropertiesFile.getPropertyAsDouble("e"), maxk = PropertiesFile.getPropertyAsDouble("maxk");
            Treceval_cmd tester = new Treceval_cmd();
            double[] maxVals1 = tester.getResultRanked();
            double[] maxVals2 = tester.getResultRanked();
            double[] maxVals3 = tester.getResultRanked();
            double[] maxVals4 = tester.getResultRanked();
            Files.delete(Paths.get(directory + "\\results.txt"));
            CSVPrinter printer1 = new CSVPrinter(Files.newBufferedWriter(Paths.get(directory + "\\R-Percision.csv")), CSVFormat.DEFAULT.withHeader("R-Percision", "Percision", "Recall", "Rank", "k", "b", "delta", "idf"));
            CSVPrinter printer2 = new CSVPrinter(Files.newBufferedWriter(Paths.get(directory + "\\Percision.csv")), CSVFormat.DEFAULT.withHeader("R-Percision", "Percision", "Recall", "Rank", "k", "b", "delta", "idf"));
            CSVPrinter printer3 = new CSVPrinter(Files.newBufferedWriter(Paths.get(directory + "\\Recall.csv")), CSVFormat.DEFAULT.withHeader("R-Percision", "Percision", "Recall", "Rank", "k", "b", "delta", "idf"));
            CSVPrinter printer4 = new CSVPrinter(Files.newBufferedWriter(Paths.get(directory + "\\Rank.csv")), CSVFormat.DEFAULT.withHeader("R-Percision", "Percision", "Recall", "Rank", "k", "b", "delta", "idf"));
            printer1.flush();
            printer2.flush();
            printer3.flush();
            printer4.flush();
            double startTests = System.currentTimeMillis();
            for (double kk = k; kk <= maxk; kk += e) {
                for (double bb = b; bb <= 1; bb += e) {
                    for (double dd = d; dd <= 1; dd += 2 * e) {
                        for (double ff = f; ff <= 0.6; ff += e) {
                            tester.simulateSearch2TrecevalBM25(queries, queryNums, kk, bb, dd, ff);
                            double[] newVals = tester.getResultRanked();
                            if (newVals[0] > maxVals1[0]) {
                                maxVals1 = newVals.clone();
                                printer1.printRecord(newVals[0], newVals[1], newVals[2], newVals[3], kk, bb, dd, ff);
                                printer1.flush();
                            }
                            if (newVals[1] > maxVals2[1]) {
                                maxVals2 = newVals.clone();
                                printer2.printRecord(newVals[0], newVals[1], newVals[2], newVals[3], kk, bb, dd, ff);
                                printer2.flush();
                            }
                            if (newVals[2] > maxVals3[2]) {
                                maxVals3 = newVals.clone();
                                printer3.printRecord(newVals[0], newVals[1], newVals[2], newVals[3], kk, bb, dd, ff);
                                printer3.flush();
                            }
                            if (newVals[3] > maxVals4[3]) {
                                maxVals4 = newVals.clone();
                                printer4.printRecord(newVals[0], newVals[1], newVals[2], newVals[3], kk, bb, dd, ff);
                                printer4.flush();
                            }
                            Files.delete(Paths.get(directory + "\\results.txt"));
                            double currentTime = System.currentTimeMillis();
                            System.out.println((currentTime - startTests) / 1000 + "\nk = " + kk + "\tb = " + bb + "\tdelta = " + dd + "\tidf = " + ff + "\n" +
                                    Arrays.toString(maxVals1) + "\n" +
                                    Arrays.toString(maxVals2) + "\n" +
                                    Arrays.toString(maxVals3) + "\n" +
                                    Arrays.toString(maxVals4) + "\n"
                            );
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testSearch() {
        searchQueries();
    }
}
