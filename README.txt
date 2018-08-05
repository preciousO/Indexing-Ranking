*****************************************
Name : Precious Oladotun-Amoo		*
Student No: 7397658		     	*
Name : 	Jordhan Madec			*
Student No:	300041696		*
					*
				        *
Assignment 1 -    CSI 4107		*
Microblog information retrieval system	*
*****************************************

TASK LIST AND TASK ASSIGNMENT

We both worked on the following list of tasks together:

1. Pre Processing
2. Indexing
3. Loading of Stop Words and Stop Word Removal
4. Parsing Queries
5. Geting Query Results
6. Retrieval and Ranking Results
7. Evaluation
8. Results file


FUNCTIONALITY

The program is designed to satisfy all the functional requirements
as specified in the assignment description.

The main java file is IR.java although there are libraries which the IR class
is dependent on. One of such libraries is the Porter Stemmer. The code for the
porter stemmer is in Stemmer.java

The IR.java file contains code for :

Preprocessing : This deals with tokenization , punctuation , number and stopword removal.
After the program carries out preprocessing the output known as the index terms will be all the words left after

Indexing : This part of the code deals with building an inverted index, with an entry for each word in the vocabulary.
The algorithm for this will be discussed in the algorithm section

Retrieval and Ranking Results: This part of the code deals with obtaining the limited set of documents that contain at
least one of the query words. Subsequently we compute the similarity scores between a query and each document using the
cosine method

Results File : This part of the code generates the results.txt file

Evaluation : This is done using the tree_eval script


EXECUTION INSTRUCTIONS

Please note a JDK (Java Development Kit) is required to run this program. If you don't have one, please download the JDK before continuing

To run the program :

1. Download the zip file submitted.

2. Using a Terminal / Command Prompt / IDE that includes a terminal , change directories to the location of IR.java
   Changing directories can be done using the "cd" command followed by the desired path.

3. Type "javac IR.java" and hit enter to compile the program

4. For indexing , type : "java IR -i <name of document you wish to index>" and hit enter
   For querying , type : "java IR -q <name of document you wish to query>" and hit enter

5. For indexing , the index.txt file contains the results.
   For querying , the results.txt file contains the results.


ALGORITHMS

Below are the algorithms/ data structures used for this program :


TreeMap Data Structure :  The map is sorted according to the natural ordering of its keys, or by a Comparator provided at
map creation time, depending on which constructor is used.

This implementation provides guaranteed log(n) time cost for the containsKey, get, put and remove operations. Algorithms
are adaptations of those in Cormen, Leiserson, and Rivest's Introduction to Algorithms.

This structure was used for storing indexes , results , queriesIndex , queriesweights and IndexWeights


TreeSet Data Structure : A NavigableSet implementation based on a TreeMap. The elements are ordered using their natural
ordering, or by a Comparator provided at set creation time, depending on which constructor is used.

This implementation provides guaranteed log(n) time cost for the basic operations (add, remove and contains).

This structure was used for storing the Stop words


Regarding algorithms and optimization , we took log(n) or n time for almost every task.

The only exception was the Weighting of the queries which was done in O(n^2) time

Vocabulary Size and Sample Tokens

The size of the vocabulary was 81007

The 100 sample tokens as requested can be found in indexSample.txt

The other results are in results.txt


DISCUSSION

Based on our assessment of the results, running time of the program and using the Mean Average Precision we believe
our program performs as expected by meeting all the functional requirements and doing so efficiently. The MAP was 22% ,
the precision for the first 10 documents was 31%. Based on these numbers the algorithm is efficient. As there is always room for
improvement we believe a better Stemmer as well as more precise regex expressions for finding punctuations e.t.c would slightly
improve our results. However ,all things considered , our results are very good.

Initially, we decided to use Porter Stemming algorithm to stem the tokens. However, after removing it, we realized that both
precision and recall raised. Thus, we decided to remove stemming from our algorithm.
