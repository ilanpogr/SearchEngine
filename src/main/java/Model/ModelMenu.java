package Model;

import Controller.PropertiesFile;
import Master.Master;
import Parser.Parse;
import ReadFile.ReadFile;
import Stemmer.Stemmer;
import TextContainers.Doc;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ModelMenu {

    private static Master master_of_puppets;

    public int getNumOfTerms() {
        return master_of_puppets.getNumOfTerms();
    }

    public int getNumOfDocs() {
        return master_of_puppets.getNumOfDocs();
    }

    public void start(){
        master_of_puppets = new Master();
    }


}
