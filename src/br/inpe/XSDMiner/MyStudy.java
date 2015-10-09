/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author diego
 */
package br.inpe.XSDMiner;

import br.com.metricminer2.MetricMiner2;
import br.com.metricminer2.RepositoryMining;
import br.com.metricminer2.Study;
import br.com.metricminer2.persistence.csv.CSVFile;
import br.com.metricminer2.scm.GitRepository;
import br.com.metricminer2.scm.commitrange.Commits;

public class MyStudy implements Study {

    /**
     * @param args the command line arguments
     */
    String project =
            //"datacite-schema";
            //"opennms"; // FAIL!!
            //"opennms-mirror";
            "SOCIETIES-Platform";
            //"spring-ws"; // OK
            //"XeroAPI-Schemas"; // OK
            //"xwiki-platform";
            //"zanata-server";
    
    String projectDir = "/home/diego/github/" + project;
    
    String output = "/home/diego/Área de Trabalho/mm_output/" + project + ".csv";
    
    public static void main(String[] args) {
        new MetricMiner2().start(new MyStudy());
    }
    
    @Override
    public void execute() {
        new RepositoryMining()
                .in(GitRepository.singleProject(projectDir))
                .through(Commits.all())
                .process(new MineXSD(), new CSVFile(output))
                .mine();
    }
}