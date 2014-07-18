/**
 * Copyright (c) 2014, Sindice Limited. All Rights Reserved.
 *
 * This file is part of the SIREn project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sindicetech.siren.demo.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Intended to load JSON files to a Solr/SIREn instance
 */
@SuppressWarnings("static-access")
public class Loader {

  private static final String COMMIT_SUCCESFULLY_EXECUTED_MSG = "commit has been succesfully executed";
  private static final int MAX_BATCH_SIZE = 131072;
  private static final String DESCRIPTION = "\nCommand line utility for loading json files into Solr/SIREn";
  private static final String USAGE_BASE = "load ";
  private static final char HELP_OPT = 'h';
  private static final char EXT_OPT = 'e';
  private static final char NO_EXT_CHECK_OPT = 'c';
  private static final char BATCH_OPT = 'b';
  private static final char FILENAME_AS_ID_OPT = 'g';

  private static final Logger logger = LoggerFactory.getLogger(Loader.class);

  private static final Character INPUT_FILE_OPT = 'f';
  private static final Character ID_OPT = 'i';
  private static final Character URL_OPT = 'u';
  private static final Character COMMIT_EACH_OPT = 'o';

  private static final String ID_OPT_LONG = "id-path";
  private static final String URL_OPT_LONG = "solr-url";
  private static final String INPUT_FILE_OPT_LONG = "file";
  private static final String BATCH_OPT_LONG = "batch-size";
  private static final String NO_EXT_CHECK_OPT_LONG = "no-extension-check";
  private static final String EXT_OPT_LONG = "files-extension";
  private static final String COMMIT_EACH_LONG = "commit-after-each-file";
  private static final String FILENAME_AS_ID_LONG = "filename-as-id";

  private static final int DEFAULT_BATCH_SIZE = 1024;
  private static final String DEFAULT_SOLR_URL = "http://localhost:8983/solr/";
  private static final String DEFAULT_ID_FIELD = "id";
  private static final String DEFAULT_JSON_EXTENSION = "json";

  private final SolrServer server;
  private final String idFieldName;
  private final int batchSize;
  private final String jsonFilesExtension;
  private final boolean checkJsonExtension;
  private final boolean commitAfterEachFile;
  private final boolean filenameAsId;

  public Loader(HttpSolrServer solrServer, String idFieldName, int batchSize,
      String jsonFilesExtension, boolean checkJsonExtension, boolean commitAfterEachDocument,
      boolean generateId) {
    this.server = solrServer;
    this.idFieldName = idFieldName;
    this.batchSize = batchSize;
    this.jsonFilesExtension = jsonFilesExtension;
    this.checkJsonExtension = checkJsonExtension;
    this.commitAfterEachFile = commitAfterEachDocument;
    this.filenameAsId = generateId;
  }

  /** checks parameters, instantiate loader and starts load */
  public static void main(String[] args) {
    CommandLineParser cmdLineParser = new BasicParser();
    CommandLine cmd = null;
    Options options = buildOptions();
    try {
      cmd = cmdLineParser.parse(options, args);
    } catch (ParseException e) {
      showHelpExit(options);
    }
    if (cmd.hasOption(HELP_OPT)) {
      showExtendHelpExit(options);
    }

    List<File> filesToProcess = Loader.checkInputFilesAndFolders(cmd
        .getOptionValues(INPUT_FILE_OPT));
    if (filesToProcess.size() == 0) {
      logger.error("no file to process");
      System.exit(-1);
    }
    Loader loader = new Loader(new HttpSolrServer(cmd.getOptionValue(URL_OPT, DEFAULT_SOLR_URL)),
        cmd.getOptionValue(ID_OPT, DEFAULT_ID_FIELD), Loader.retrieveAndChekBatchSize(cmd
            .getOptionValue(BATCH_OPT)), cmd.getOptionValue(EXT_OPT, DEFAULT_JSON_EXTENSION),
        !cmd.hasOption(NO_EXT_CHECK_OPT), cmd.hasOption(COMMIT_EACH_OPT),
        cmd.hasOption(FILENAME_AS_ID_OPT));
    loader.loadFiles(filesToProcess);
  }

  private static int retrieveAndChekBatchSize(String opt) {

    if (opt != null) {
      try {
        int batchSize = Integer.parseInt(opt);
        if (batchSize <= MAX_BATCH_SIZE && batchSize > 0) {
          return batchSize;
        } else {
          logger.error("batch size should be positive integer <= {}, {} ignored", MAX_BATCH_SIZE,
              opt);
        }
      } catch (NumberFormatException e) {
        logger
            .error("batch size should be positive integer <= {}, {} ignored", MAX_BATCH_SIZE, opt);
      }
    }
    return DEFAULT_BATCH_SIZE;
  }
  private static List<File> checkInputFilesAndFolders(String[] optionValues) {
    List<File> filesToProcess = new ArrayList<File>();
    for (String param : optionValues) {
      File fileOrDirectory = new File(param);
      if ((fileOrDirectory.isFile() || fileOrDirectory.isDirectory()) && fileOrDirectory.exists()
          && fileOrDirectory.canRead()) {
        filesToProcess.add(fileOrDirectory);
      } else {
        logger.error("not existing or not readable file is skipped: {}", param);
      }
    }
    return filesToProcess;
  }

  /**
   * loads files to Solr
   * 
   * @param filesToProcess
   *          - list of json files or/and folder with json files to process
   */
  public void loadFiles(List<File> filesToProcess) {
    long start = System.currentTimeMillis();
    int total = 0;
    FilenameFilter jsonFilesNamesFilter = new JSONfilesFilter();
    for (File fileOrDirectory : filesToProcess) {
      if (fileOrDirectory.isFile()) {
        total += loadFile(fileOrDirectory);
      } else {
        String[] dirList = fileOrDirectory.list(jsonFilesNamesFilter);
        for (String fileName : dirList) {
          File file = new File(fileOrDirectory, fileName);
          // ignores nested directory
          if (file.isFile() && file.canRead()) {
            total += loadFile(file);
          }

        }
      }
    }
    // final commit
    try {
      server.commit();
      logger.info(COMMIT_SUCCESFULLY_EXECUTED_MSG);
    } catch (SolrServerException e) {
      logger.error("error executing commit in Solr, giving up", e);
      throw new IllegalStateException("error executing commit in Solr, giving up", e);
    } catch (IOException e) {
      logger.error("error executing commit in Solr, giving up", e);
      throw new IllegalStateException("error executing commit in Solr, giving up", e);
    }
    logger.info("Total: {} object loaded in {} second", total,
        (System.currentTimeMillis() - start) / 1000);
  }

  private int loadFile(File file) {

    InputStream in = null;
    int counter = 0;
    try {
      in = new FileInputStream(file);
      JsonFileParser parser = new JsonFileParser(in);
      if (this.filenameAsId) {
        counter = loadOneDocFromFile(parser, file.getName());
      } else {
        counter = loadMultiFromFile(parser);
      }

      if (commitAfterEachFile) {
        server.commit();
        logger.info(COMMIT_SUCCESFULLY_EXECUTED_MSG);
      }
      logger.debug("{} objects loaded from file {}", counter, file.getName());
    } catch (JsonParseException e) {
      logger.error("error in file {} skipping", file.getName(), e);
    } catch (IOException e) {
      logger.error("error reading file {} skipping", file.getName(), e);
    } catch (SolrServerException e) {
      logger.error("error sending documents to Solr, giving up", e);
      throw new IllegalStateException("error sending documents to Solr, giving up", e);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          logger.error("error closing file {}", file.getName());
        }
      }
    }
    return counter;
  }

  private int loadMultiFromFile(JsonFileParser parser) throws JsonProcessingException, IOException,
      SolrServerException {
    int counter = 0;
    List<SolrInputDocument> documents = new ArrayList<SolrInputDocument>();
    while (parser.hasNext()) {
      documents.add(parseObject(parser.next()));
      counter++;
      if (counter % batchSize == 0) {
        server.add(documents);
        logger.debug(" {} objects loaded to Solr", counter);
        documents.clear();
      }
    }
    if (documents.size() > 0) {
      server.add(documents);
      logger.debug(" {} objects loaded to Solr", counter);
    }
    return counter;
  }

  private int loadOneDocFromFile(JsonFileParser parser, String filename) throws JsonProcessingException,
      IOException, SolrServerException {
    String id = null;
    if (filename.lastIndexOf('.') >0){
      id = filename.substring(0,filename.lastIndexOf('.'));
    } else {
      id = filename;
    }
    if (parser.hasNext()) {
      server.add(buildDocument(id, parser.next()));
      if (parser.hasNext()) {
        throw new IllegalArgumentException(
            "filename selected as ID and more then one object in the file: " + filename);
      }
      return 1;
    } else {
      return 0;
    }
  }

  private SolrInputDocument parseObject(JsonNode node) {
    return buildDocument(node.path(this.idFieldName).asText(), node);
  }

  private SolrInputDocument buildDocument(String id, JsonNode node) {
    final SolrInputDocument doc = new SolrInputDocument();
    doc.addField("id", id);
    doc.addField("json", node);
    return doc;
  }

  private static void showExtendHelpExit(Options options) {
    System.out.println("Loader" + DESCRIPTION);
    HelpFormatter hf = new HelpFormatter();
    hf.setWidth(100);
    hf.printHelp(USAGE_BASE + "<opts>", options);
    System.out.println("\nNote: only file or directory names option is mandatory\n" + "Examples:\n"
        + "    " + USAGE_BASE + " -" + INPUT_FILE_OPT + " file.json  export-folder\n" + "    "
        + USAGE_BASE + " -" + BATCH_OPT + " " + 128 + " -" + ID_OPT + " ChargeDeviceId" + " -"
        + INPUT_FILE_OPT + " file.json\n" + "    " + USAGE_BASE + " -" + NO_EXT_CHECK_OPT
        + " export-directory\n");

    System.exit(0);
  }
  private static void showHelpExit(Options options) {
    System.out.println("Loader" + DESCRIPTION + "\nsimple usage: " + USAGE_BASE + " -"
        + INPUT_FILE_OPT + " <file|directory> [<file|directory>] ..."
        + "\nPlease use -h option for extended help.\n");
    System.exit(-1);
  }

  private static Options buildOptions() {
    Options options = new Options();
    // input file name and help are exclusive
    OptionGroup fileHelpGroup = new OptionGroup();
    fileHelpGroup.setRequired(true);
    fileHelpGroup.addOption(OptionBuilder.hasArgs(1).hasOptionalArgs(20)
        .withArgName("file or/and folder name[s]")
        .withDescription("JSON file[s] or/and director(y|ies) with JSON files (max 20)")
        .withLongOpt(INPUT_FILE_OPT_LONG).isRequired().create(INPUT_FILE_OPT));

    fileHelpGroup
        .addOption(OptionBuilder.withDescription("prints help and exits").create(HELP_OPT));
    options.addOptionGroup(fileHelpGroup);
    options.addOption(OptionBuilder.hasArgs(1).withArgName("Solr URL")
        .withDescription("Solr URL (default=" + DEFAULT_SOLR_URL + ")").withLongOpt(URL_OPT_LONG)
        .isRequired(false).create(URL_OPT));

    options.addOption(OptionBuilder
        .hasArgs(1)
        .withArgName("batch size")
        .withDescription(
            "number of documents sent to Solr in one request, max " + MAX_BATCH_SIZE + " (default="
                + DEFAULT_BATCH_SIZE + ")").withLongOpt(BATCH_OPT_LONG).isRequired(false)
        .create(BATCH_OPT));
    options.addOption(OptionBuilder
        .withDescription("load all files in directories, not only files with JSON file extension")
        .withLongOpt(NO_EXT_CHECK_OPT_LONG).isRequired(false).create(NO_EXT_CHECK_OPT));
    options.addOption(OptionBuilder
        .withDescription("JSON file extension (default=" + DEFAULT_JSON_EXTENSION + ")")
        .withLongOpt(EXT_OPT_LONG).isRequired(false).create(EXT_OPT));
    options.addOption(OptionBuilder.withDescription("commit after each file, (default=false)")
        .withLongOpt(COMMIT_EACH_LONG).isRequired(false).create(COMMIT_EACH_OPT));

    OptionGroup idGroup = new OptionGroup();
    idGroup.addOption(OptionBuilder.withDescription("use filename as id, only one object can be in a file (default=false)")
        .withLongOpt(FILENAME_AS_ID_LONG).isRequired(false).create(FILENAME_AS_ID_OPT));
    idGroup.addOption(OptionBuilder.hasArgs(1).withArgName("id field name")
        .withDescription("JSON path of id field used for index (default=" + DEFAULT_ID_FIELD + ")")
        .withLongOpt(ID_OPT_LONG).isRequired(false).create(ID_OPT));
    options.addOptionGroup(idGroup);
    return options;
  }

  private class JSONfilesFilter implements FilenameFilter {

    @Override
    public boolean accept(File dir, String name) {
      if (name.startsWith(".")) {
        return false;
      }
      if (checkJsonExtension && !name.endsWith(jsonFilesExtension)) {
        return false;
      }
      return true;
    }

  }
}
