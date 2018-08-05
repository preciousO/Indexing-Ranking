import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.*;


public class IR {

  private static double THRESHOLD = 0.4;

  boolean isI;
  boolean isQ;
  private BufferedReader documentsReader;
  private BufferedReader queryReader;
  private BufferedReader stopwordsReader;

  private TreeSet<String> stopWords;

  private TreeMap<String, TreeMap<String, Integer>> index;
  private TreeMap<String, TreeMap<Integer, Integer>> queriesIndex;

  private TreeMap<String, TreeMap<String, Double>> indexWeights;
  private TreeMap<String, TreeMap<Integer, Double>> queriesIndexWeights;

  private TreeMap<Integer, String[]> queriesText;

  private TreeMap<Integer, TreeMap<String, Double>> results;
  private TreeMap<Integer, List<Entry<String, Double>>> sortedResults;


  public IR(String args[]) {
    checkOptions(args);
  }













  // R/W Files functions

  /* Options:
  /* -i: Indexing. Should pass documents collection file as parameter.
  /* -q: Querying. Should pass queries file as parameter. */
  public void checkOptions(String args[]) {
    // Check number of parameters
    if(args.length < 2) {
      System.out.println("Proper Usage is: java program -option (filename1 | filename2)");
      System.out.println("\toption is the option (i: indexing, q: querying)");
      System.out.println("\tfilename1 is a file containing documents collection");
      System.out.println("\tfilename2 is a file containing queries");
      System.exit(0);
    }

    // Check the option
    if (!args[0].equals("-i") && !args[0].equals("-q")) {
      System.out.println("Wrong option.\nThe possible options are:");
      System.out.println("\ti: preprocessing + indexing");
      System.out.println("\tq: querying + ranking results");
      System.exit(0);
    }

    // Open file for indexing
    if (args[0].equals("-i")) {
      isI = true;
      documentsReader = openDocument(args[1]);
    }

    // Open file for querying
    if (args[0].equals("-q")) {
      isQ = true;
      queryReader = openDocument(args[1]);

    }
  }



  public BufferedReader openDocument(String path) {
    try {
      BufferedReader br = new BufferedReader(new FileReader(path));
      System.out.println("File named " + path + " opened with success");
      return br;
    } catch (IOException e) {
      System.out.println("Couldn't find file named " + path);
      return null;
    }
  }



  public void loadStopwords() {
    stopwordsReader = openDocument("Stopwords.txt");
    stopWords = new TreeSet<String>();
    String s = "";
    String stopWordDoc = null;

    try {
      stopWordDoc = stopwordsReader.readLine();
    }catch(IOException e) {
      System.out.println("Couldn't read stopword");
    }

    while (stopWordDoc != null) {
      stopWords.add(stopWordDoc);

      // Next stopword
      try {
        stopWordDoc = stopwordsReader.readLine();
      } catch(IOException e) {
        System.out.println("Couldn't read stopword");
      }
    }
  }



  public void saveIndex() {
    File file = new File("index.txt");

    try {
      BufferedWriter bw = new BufferedWriter(new FileWriter(file));

      int i = 0;

      for(String token: index.keySet()) {
        for (String docId: (index.get(token)).keySet()) {
           bw.write(token + "," + docId + "," + (index.get(token)).get(docId));
           bw.newLine();
         }
      }

      bw.flush();
      bw.close();
      System.out.println("Index saved successfully as index.txt");
    } catch (IOException e) {
      System.out.println("Couldn't save index");
    }
  }



  public void loadIndex() {
    File file = new File("index.txt");
    index = new TreeMap<String, TreeMap<String, Integer>>();
    indexWeights = new TreeMap<String, TreeMap<String, Double>>();

    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
      String line;

      while((line = br.readLine()) != null) {
        String[] args = line.split(",");

        String token = args[0];
        String docId = args[1];
        int df = Integer.parseInt(args[2]);

        if (!index.containsKey(token)) {
          index.put(token, new TreeMap<String, Integer>());
        }

        index.get(token).put(docId, df);
      }

      br.close();

      weightIndex();

      System.out.println("Index loaded successfully");

    } catch (IOException e) {
      System.out.println("Couldn't load index");
    }

  }


  public void saveResults() {
    File file = new File("results.txt");

    try {
      BufferedWriter bw = new BufferedWriter(new FileWriter(file));

      for(Integer queryId: sortedResults.keySet()) {
        int i = 1;
        for (Entry<String, Double> entry: sortedResults.get(queryId)) {
          if (entry.getValue() >= THRESHOLD) {
            bw.write(queryId + " Q0 " + entry.getKey() + "\t" + i++ + "\t" + entry.getValue() + "\tmyRun");
            bw.newLine();
          }
         }
      }

      bw.flush();
      bw.close();
      System.out.println("Results saved successfully as index.txt");

    } catch (IOException e) {
      System.out.println("Couldn't save results");
    }

  }



  // Clean a string
  public String[] cleanString(String queryText) {
    String[] queriesArray;

    // Remove punctuation
    queryText = queryText.replaceAll("[^a-zA-Z0-9]", " ");

    // Remove numbers but possible relevant ones
    queryText = queryText.replaceAll("(?<=\\D)(?=\\d)"," ");
    queryText = queryText.replaceAll("(?<=\\d)(?=\\D)"," ");
    queryText = queryText.replaceAll("^[0-9]\\s", " ");
    queryText = queryText.replaceAll("\\s[0-9]$", " ");
    queryText = queryText.replaceAll("\\s[0-9]\\s", " ");
    queryText = queryText.replaceAll("[0-9]{5,}", " ");

    // Reduce whitespaces
    queryText = queryText.replaceAll("\\s+", " ");

    // Remove whitespaces at the beginning
    queryText = queryText.replaceAll("^\\s+", "");

    // Remove whitespaces at the end
    queryText = queryText.replaceAll("\\s+$", "");

    //Stemming
    if (!queryText.equals("")) {
      queriesArray = queryText.split(" ");
      /*Stemmer stemmer = new Stemmer();

      int i = 0;

      for (String query : queriesArray) {
        stemmer.add(query.toCharArray(), query.length());
        stemmer.stem();
        query = stemmer.toString();
        queriesArray[i++] = query;
      }*/

      return queriesArray;
    } else {
      return null;
    }
  }












  // SCORING FUNCTIONS

  public double idf(int df) {
    return Math.log10(index.size() / df);
  }

  public double tf(int f) {
    return Math.log10(1 + f);
  }

  // Compute the weight of a token
  public double weight(int f, int df) {
    return tf(f) * idf(df);
  }




  public void weightIndex() {
    System.out.println("Weighting index...");

    for (String token: index.keySet()) {
      int df = index.get(token).size();
      indexWeights.put(token, new TreeMap<String, Double>());

      for (String docId: index.get(token).keySet()) {
        int f = index.get(token).get(docId);
        double weight = weight(f, df);

        indexWeights.get(token).put(docId, weight);
      }
    }
  }

  public void weightQueriesIndex() {
    System.out.println("Weighting queries...");

    for (String token: queriesIndex.keySet()) {
      int f = queriesIndex.get(token).size();
      queriesIndexWeights.put(token, new TreeMap<Integer, Double>());

      for (Integer queryId: queriesIndex.get(token).keySet()) {
        int df = queriesIndex.get(token).get(queryId);
        double weight = weight(f, df);

        queriesIndexWeights.get(token).put(queryId, weight);
      }
    }
  }
















  // INDEXING + PREPROCESSING

  // Take a collection of documents as input and execute preprocessing on each document
  public void preprocessing() {
    System.out.println("Start preprocessing...");

    index = new TreeMap<String, TreeMap<String, Integer>>();
    indexWeights = new TreeMap<String, TreeMap<String, Double>>();

    String document = null;
    String docId = "";
    String tokens = "";
    String[] stemmedTokens;

    Pattern pattern = Pattern.compile("^(\\d+)\\s*(.+)$");

    loadStopwords();

    try {
      document = documentsReader.readLine();
    } catch (IOException e) {
      System.out.println("Couldn't read document");
    }

    while (document != null) {
      Matcher matcher = pattern.matcher(document);
      if (matcher.matches()) {
        // Get document id and content
        docId = matcher.group(1);
        tokens = matcher.group(2).toLowerCase();

        stemmedTokens = cleanString(tokens);

        if (stemmedTokens != null)
          indexing(docId, stemmedTokens);
      }

      // Next document
      try {
        document = documentsReader.readLine();
      } catch (IOException e) {
        System.out.println("Couldn't read document");
      }

    }

    saveIndex();

    System.out.println("Preprocessing finished successfully !");
  }



  // Take a document as input an indexes all the terms it contains
  public void indexing(String docId, String[] tokens) {
    int pos = 0;

    for (String token: tokens) {
      if (!stopWords.contains(token)) {
        if (!index.containsKey(token)) {
          index.put(token, new TreeMap<String, Integer>());
        }

        TreeMap<String, Integer> documentsHash = index.get(token);

        if (!documentsHash.containsKey(docId)) {
          documentsHash.put(docId, 1);
        } else {
          documentsHash.put(docId, (int)(documentsHash.get(docId)) + 1);
        }
      }
    }
  }


















  // QUERYING

  // Process querying and return ranked results
  public void querying() {
    System.out.println("Start querying...");

    queriesIndex = new TreeMap<String, TreeMap<Integer, Integer>>();
    queriesText = new TreeMap<Integer, String[]>();
    queriesIndexWeights = new TreeMap<String, TreeMap<Integer, Double>>();
    results = new TreeMap<Integer, TreeMap<String, Double>>();
    sortedResults = new TreeMap<Integer, List<Entry<String, Double>>>();

    loadIndex();
    parseQueries();
    weightQueriesIndex();

    for (Integer queryId: queriesText.keySet()) {
      getQueryResults(queryId);
    }

    rankResults();

    saveResults();

    System.out.println("Querying finished successfully !");

    // TODO:
    // Parse query file
    // Clean each query and get a String[] as a result for each querying
    // Call getQueryResults function for each query;
  }


  public void parseQueries()
  {
    System.out.println("Start parsing queries...");

    loadStopwords();

    String document = null;
    int queryNumber = -1;
    String queryText = "";

    String[] stemmedQuery = null;

    Pattern pattern_query = Pattern.compile("<title.*?>(.*?)<\\/title>");
    Pattern pattern_num = Pattern.compile("<num.*?>\\s*Number\\s*:\\s*MB0*(\\d+)\\s*<\\/num>");


    try {
      document = queryReader.readLine();
    } catch (IOException e) {
      System.out.println("Couldn't read document");
    }


    while (document != null) {
      Matcher matcher_query = pattern_query.matcher(document);
      Matcher matcher_num = pattern_num.matcher(document);

      if (matcher_query.matches()) {

        queryText = matcher_query.group(1).toLowerCase();

        stemmedQuery = cleanString(queryText);

      } else if (matcher_num.matches()) {

        queryNumber = Integer.valueOf(matcher_num.group(1));

      } else {
        queryNumber = -1;
        queryText = "";
      }

      if (queryNumber > -1 && !queryText.equals("") && stemmedQuery != null) {
        queriesText.put(queryNumber, stemmedQuery);
        indexingQueries(queryNumber, stemmedQuery);
      }

      // Next document
      try {
        document = queryReader.readLine();
      } catch (IOException e) {
        System.out.println("Couldn't read document");
      }

    }

    System.out.println("Queries parsed successfully !");
  }



  // Take a document as input an indexes all the terms it contains
  public void indexingQueries(Integer queryId, String[] tokens) {
    int pos = 0;

    for (String token: tokens) {
      if (!stopWords.contains(token)) {
        if (!queriesIndex.containsKey(token)) {
          queriesIndex.put(token, new TreeMap<Integer, Integer>());
        }

        TreeMap<Integer, Integer> queriesHash = queriesIndex.get(token);

        if (!queriesHash.containsKey(queryId)) {
          queriesHash.put(queryId, 1);
        } else {
          queriesHash.put(queryId, (int)(queriesHash.get(queryId)) + 1);
        }
      }
    }
  }



  // Get all the documents matching a query and compute their similarity
  public void getQueryResults(Integer queryId) {

    //ValueComparator comparator = new ValueComparator();
    TreeMap<String, Double> myTreeMap = new TreeMap<String, Double>();
    //comparator.setMap(myTreeMap);
    //myTreeMap = new TreeMap<String, Double>(comparator);
    results.put(queryId, myTreeMap);

    String[] query = queriesText.get(queryId);

    double sumQ2 = 0.0;
    HashMap<String, Double> sumD2 = new HashMap<String, Double>();
    HashMap<String, Double> sumDQ = new HashMap<String, Double>();
    double q = 0.0;
    double d = 0.0;

    TreeSet<String> matchingDocuments = new TreeSet<String>();

    // Retrieve all matching documents
    for (String token: query) {
      if (queriesIndex.containsKey(token)) {
        q = queriesIndexWeights.get(token).get(queryId);
        sumQ2 += q * q;

        if (index.containsKey(token)) {

          for (String docId: index.get(token).keySet()) {
            matchingDocuments.add(docId);

            d = (double) indexWeights.get(token).get(docId);

            double tmpD2 = 0.0;
            double tmpDQ = 0.0;

            if (sumD2.containsKey(docId)) {
              tmpD2 = sumD2.get(docId);
            }

            if (sumDQ.containsKey(docId)){
              tmpDQ = sumDQ.get(docId);
            }

            sumD2.put(docId, tmpD2 + d * d);
            sumDQ.put(docId, tmpDQ + d * q);
          }
        }
      }
    }

    for (String docId: matchingDocuments) {
      double similarity = sumDQ.get(docId) / ( Math.sqrt(sumQ2) * Math.sqrt(sumD2.get(docId)) );
      results.get(queryId).put(docId, similarity);
    }

  }


  public void rankResults() {
    for (Integer queryId: results.keySet()) {

      Set<Entry<String, Double>> entrySet = results.get(queryId).entrySet();

      List<Entry<String, Double>> sortedList = new ArrayList<Entry<String, Double>>(entrySet);

      Collections.sort(sortedList, new Comparator<Entry<String,Double>>() {
        @Override
        public int compare(Entry<String, Double> ele1, Entry<String, Double> ele2) {
          return ele2.getValue().compareTo(ele1.getValue());
        }
      });

      sortedResults.put(queryId, sortedList);
    }
  }














  public static void main(String args[]) {
    IR ir = new IR(args);

    if (ir.isI) {
      ir.preprocessing();
    }

    if (ir.isQ) {
      // TODO
      ir.querying();
    }
  }
}
