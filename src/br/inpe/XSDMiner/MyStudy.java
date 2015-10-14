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
            //"opennms";
            //"opennms-mirror";
            //"SOCIETIES-Platform";
            "spring-ws";
            //"XeroAPI-Schemas";
            //"xwiki-platform";
            //"zanata-server";
    
    //String projectDir = "/home/diego/github/" + project;
    String projectDir = "C:/github/" + project;
    //String projectDir = "c:/" + project;
    
    //String output = "/home/diego/Área de Trabalho/mm_output/" + project + ".csv";
    String output = "c:/Users/Diego/Desktop/" + project + ".csv";
    
    public static void main(String[] args) {
        new MetricMiner2().start(new MyStudy());
    }
    
    @Override
    public void execute() {
        new RepositoryMining()
                .in(GitRepository.singleProject(projectDir))
                .through(Commits.all())
                .withThreads(4)
                .process(new MineXSD(), new CSVFile(output))
                .mine();
    }
}
