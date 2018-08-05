import java.io.*;
import java.util.regex.*;
import java.util.HashMap;

public class IR {

  boolean isI;
  boolean isQ;
  private BufferedReader documentsReader;
  private BufferedReader queryReader;

  private HashMap index;


  public IR(String args[]) {
    checkOptions(args);
  }



  // INIT PROGRAM

  /* Options:
  /* -i: Indexing. Should pass documents collection file as parameter.
  /* -q: Querying. Should pass queries file as parameter. */
  public void checkOptions(String args[]) {
    if(args.length < 2) {
      System.out.println("Proper Usage is: java program -option (filename1 | filename2)");
      System.out.println("\toption is the option (i: indexing, q: querying)");
      System.out.println("\tfilename1 is a file containing documents collection");
      System.out.println("\tfilename2 is a file containing queries");
      System.exit(0);
    }

    if (!args[0].equals("-i") && !args[0].equals("-q")) {
      System.out.println("Wrong option.\nThe possible options are:");
      System.out.println("\ti: preprocessing + indexing");
      System.out.println("\tq: querying + ranking results");
      System.exit(0);
    }

    if (args[0].equals("-i")) {
      isI = true;
      documentsReader = openDocument(args[1]);
    }
    if (args[0].equals("-q")) {
      isQ = true;
      queryReader = openDocument(args[1]);
    }
  }

  public BufferedReader openDocument(String path) {
    try {
      BufferedReader br = new BufferedReader(new FileReader(path));
      System.out.println("File opened with success");
      return br;
    } catch (IOException e) {
      System.out.println("Couldn't find file named " + path);
      return null;
    }
  }









  // INDEXING + PREPROCESSING

  // Take a collection of documents as input and execute preprocessing on each document
  public void preprocessing() {
    System.out.println("Start preprocessing");

    String document = null;
    String docId = "";
    String tokens = "";

    Pattern pattern = Pattern.compile("(\\d*)\\s*(.*)");

    try {
      document = documentsReader.readLine();
    } catch (IOException e) {
      System.out.println("Couldn't read document");
    }

    while (document != null) {
      Matcher matcher = pattern.matcher(document);
      System.out.println(document);
      if (matcher.matches()) {
        docId = matcher.group(0);
        tokens = matcher.group(1);
        System.out.println(docId);
      }
      // TODO
      // 1. Remove punctuation, numbers and stopwords
      // 2. Stem remaining tokens
      // 3. Index tokens
      indexing(tokens);

      try {
        document = documentsReader.readLine();
      } catch (IOException e) {
        System.out.println("Couldn't read document");
      }
    }

  }

  // Take a document as input an indexes all the terms it contains
  public void indexing(String document) {
    // TODO
  }






  // QUERYING














  public static void main(String args[]) {
    IR ir = new IR(args);

    if (ir.isI) {
      ir.preprocessing();
    }

    if (ir.isQ) {

    }
  }
}
