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
import br.com.metricminer2.scm.RepositoryFile;
import br.com.metricminer2.scm.SCMRepository;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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
                
                if(commitCount == 0)
                {
                    repo.getScm().checkout(commit.getHash());
                    
                    List<RepositoryFile> files = repo.getScm().files();
                    
                    for(RepositoryFile file : files) {
                        if(!file.fileNameEndsWith("xsd")) continue;

                        File soFile = file.getFile();
                        String shortFName = soFile.getName();
                        modsCount.put(shortFName, 0);

                        XSDParser parser = new XSDParser(soFile, "xs:element", "xs:attribute", "xs:complexType");
                        //XSDParser parser = new XSDParser(soFile, "element", "attribute", "complexType");
                        int qElements = parser.getQuantityOfElements();
                        int qAttributes = parser.getQuantityOfAttributes();
                        int qComplexTypes = parser.getQuantityOfComplexTypes();
                        
                        writer.write(
                            commit.getHash(),
                            String.valueOf(commitCount),
                            shortFName,
                            String.valueOf(0),
                            String.valueOf(qElements),
                            String.valueOf(qAttributes),
                            String.valueOf(qComplexTypes),
                            String.valueOf(0),
                            String.valueOf(0),
                            String.valueOf(0)
                        );
                        modsElements.put(shortFName, qElements);
                        modsAttributes.put(shortFName, qAttributes);
                        modsComplexTypes.put(shortFName, qComplexTypes);
                    }
                    commitCount++;
                    repo.getScm().reset();
                }
                
                else
                {
                    for(Modification m : commit.getModifications())
                    {
                        String fName = m.getFileName();
                        if(!fName.endsWith("xsd")) continue;

                        int oldMods = modsCount.get(fName);
                        modsCount.put(fName, oldMods+1);

                        //File soFile = new File(baseFileLocation + fName);
                        //FileUtils.writeStringToFile(soFile, m.getSourceCode());
                        //XSDParser parser = new XSDParser(soFile.getPath(), "xs:element", "xs:attribute", "xs:complexType");
                        XSDParser parser = new XSDParser(m.getSourceCode().getBytes(), "xs:element", "xs:attribute", "xs:complexType");
                        //XSDParser parser = new XSDParser(soFile.getPath(), "element", "attribute", "complexType");
                        
                        int prevElements = modsElements.get(fName),
                                actualElements = parser.getQuantityOfElements();

                        int prevAttributes = modsAttributes.get(fName),
                                actualAttributes = parser.getQuantityOfAttributes();

                        int prevComplexTypes = modsComplexTypes.get(fName),
                                actualComplexTypes = parser.getQuantityOfComplexTypes();

                        String updateElements, updateAttributes, updateComplexTypes;

                        if(actualElements > prevElements)
                        {
                            updateElements = "-1";
                            modsElements.put(fName, actualElements);
                        }
                        else if(actualElements == prevElements)
                            updateElements = "0";
                        else
                        {
                            updateElements = "1";
                            modsElements.put(fName, actualElements);
                        }

                        if(actualAttributes > prevAttributes)
                        {
                            updateAttributes = "-1";
                            modsAttributes.put(fName, actualAttributes);
                        }
                        else if(actualAttributes == prevAttributes)
                            updateAttributes = "0";
                        else
                        {
                            updateAttributes = "1";
                            modsAttributes.put(fName, actualAttributes);
                        }

                        if(actualComplexTypes > prevComplexTypes)
                        {
                            updateComplexTypes = "-1";
                            modsComplexTypes.put(fName, actualComplexTypes);
                        }
                        else if(actualComplexTypes == prevComplexTypes)
                            updateComplexTypes = "0";
                        else
                        {
                            updateComplexTypes = "1";
                            modsComplexTypes.put(fName, actualComplexTypes);
                        }

                        writer.write(commit.getHash(),
                                String.valueOf(commitCount),
                                fName,
                                String.valueOf(modsCount.get(fName)),
                                String.valueOf(actualElements),
                                String.valueOf(actualAttributes),
                                String.valueOf(actualComplexTypes),
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
