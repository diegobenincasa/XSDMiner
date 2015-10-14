/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.inpe.XSDMiner;

import br.com.metricminer2.domain.Commit;
import br.com.metricminer2.domain.Modification;
import br.com.metricminer2.persistence.PersistenceMechanism;
import br.com.metricminer2.scm.CommitVisitor;
import br.com.metricminer2.scm.SCMRepository;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author diego
 */
public class MineXSD implements CommitVisitor{
    
    /**
     *
     * @param repo
     * @param commit
     * @param writer
     */
    static int commitCount = 0;
    static Map<String, Integer> modsCount = new HashMap<String, Integer>();
    static boolean headerPrint = false;
    //static String baseFileLocation = "/home/diego/Área de Trabalho/";
    static String baseFileLocation = "c:/github/";
    //static String baseFileLocation = "c:/";
    
    static Map<String, Integer> modsElements = new HashMap<String, Integer>();
    static Map<String, Integer> modsAttributes = new HashMap<String, Integer>();
    static Map<String, Integer> modsComplexTypes = new HashMap<String, Integer>();

    public MineXSD() {

    }
    
    @Override
    public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {

        try {
                if(headerPrint == false)
                {
                    headerPrint = true;
                    writer.write(
                            "HASH",
                            "REVCOMMIT",
                            "FILENAME",
                            "MODCOUNT",
                            "QTY_ELEMENTS",
                            "QTY_ATTR",
                            "QTY_CTYPES",
                            "MOD_ELEMENTS",
                            "MOD_ATTRIBUTES",
                            "MOD_CTYPES"
                    );
                }
                
                {
                for(Modification m : commit.getModifications())
                {
                    String fName = m.getFileName();
                    if(!fName.endsWith("xsd")) continue;

                    Integer oldMods = modsCount.get(fName);
                    if(oldMods != null)
                    {
                    	modsCount.put(fName, oldMods+1);
                    }
                    else
                    {
                    	modsCount.put(fName, 0);
                    }

                    // CREATE PARSER AND FETCH METRICS                       
                    XSDParser parser = new XSDParser(m.getSourceCode().getBytes(), "xs:element", "xs:attribute", "xs:complexType");
                    int qElements = parser.getQuantityOfElements();
                    int qAttributes = parser.getQuantityOfAttributes();
                    int qComplexTypes = parser.getQuantityOfComplexTypes();

                    // INITIALIZE COMPARISON VARIABLES (CV)
                    Integer recentElements = modsElements.get(fName),
                    	recentAttributes = modsAttributes.get(fName),
                    	recentComplexTypes = modsComplexTypes.get(fName);
                    String updateElements, updateAttributes, updateComplexTypes;
                    
                    // CHECK IF THE CURRENT COMMIT HAS THE FIRST VISITED MOD IN CURRENT FILE AND SET CV'S TO 0 IN THIS SCENARIO;
                    // IF NOT, COMPARE IT TO MOST RECENT MODIFICATION
                    
                    // 1) DO IT WITH XS:ELEMENT
                    if(recentElements == null)
                    {
                        updateElements = "2"; // 2 MEANS HEAD -- DO NOT TAKE PART IN COMPARISONS
                    }
                    else
                    {
                    	if(qElements > recentElements)
                        {
                            updateElements = "-1";
                        }
                        else if(qElements == recentElements)
                            updateElements = "0";
                        else
                        {
                            updateElements = "1";
                        }
                    }
                    
                    // 2) DO IT WITH XS:ATTRIBUTE
                    if(recentAttributes == null)
                    {
                        updateAttributes = "2"; // 2 MEANS HEAD -- DO NOT TAKE PART IN COMPARISONS
                    }
                    else
                    {
                    	if(qAttributes > recentAttributes)
                        {
                            updateAttributes = "-1";
                        }
                        else if(qAttributes == recentAttributes)
                            updateAttributes = "0";
                        else
                        {
                            updateAttributes = "1";
                        }
                    }
                    
                    // 3) DO IT WITH XS:COMPLEXTYPE
                    if(recentComplexTypes == null)
                    {
                        updateComplexTypes = "2"; // 2 MEANS HEAD -- DO NOT TAKE PART IN COMPARISONS
                    }
                    else
                    {
                    	if(qComplexTypes > recentComplexTypes)
                        {
                            updateComplexTypes = "-1";
                        }
                        else if(qComplexTypes == recentComplexTypes)
                            updateComplexTypes = "0";
                        else
                        {
                            updateComplexTypes = "1";
                        }
                    }
                    // WRITE CURRENT METRICS TO HASHMAPS
                    modsElements.put(fName, qElements);
                    modsAttributes.put(fName, qAttributes);
                    modsComplexTypes.put(fName, qComplexTypes);

                    writer.write(commit.getHash(),
                            String.valueOf(commitCount),
                            fName,
                            String.valueOf(modsCount.get(fName)),
                            String.valueOf(qElements),
                            String.valueOf(qAttributes),
                            String.valueOf(qComplexTypes),
                            updateElements,
                            updateAttributes,
                            updateComplexTypes
                    );
                } commitCount++;
            }
        } catch (IOException ex) {
            Logger.getLogger(MineXSD.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            repo.getScm().reset();
        }
    }

    public String name() {
        return "developers";
    }
    
    
}