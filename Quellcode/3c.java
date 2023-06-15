import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

public class Aufgabe3cSpark {

  public static void main(String[] args) throws Exception {

      //Dateipfad des Inputs angeben (in dem Fall ist der Inhalt der Input)
      Path file = Paths.get("./src/main/resources/Text.txt");

      //Konfigurationsklasse fuer Spark anlegen
      //In dieser werden Attribute wie Name der App oder auch RAM gesetzt
      SparkConf sparkConf = new SparkConf().setAppName("Aufgabe3c").set("spark.master", "local");
      sparkConf.set("spark.testing.memory", "2453012480");   

      //Anlegen des Spark-Kontextes, welcher auf der Konfiguration von oben beruht
      JavaSparkContext ctx = new JavaSparkContext(sparkConf);

      //Java Resilient Distributed Dataset, welches eine verteilte Sammlung von Elementen darstellt
      //In diesem Fall ist es eine Sammlung, die den Inhalt der oben angegebenen Datei zeilenweise beinhaltet
      JavaRDD<String> lines = ctx.textFile(file.toString(), 1);

      //JavaPairRDD ist eine spezielle JavaRDD-Klasse, welche eine Sammlung an Key-Value-Paaren darstellt
      //In diesem Fall werden die 2-grams als Values abgespeichert, als Key wird der Text1 bzw. Text2 benutzt
      //Mit .mapToPair() wird fuer jede Zeile in der eingelesenen Datei eine neue Sammlung erstellt, die als Key die Zeile enthaelt
      JavaPairRDD<String, List> twoGrams = lines.mapToPair(s -> {
        String l = s.toLowerCase();
        List<String> list2grams = new ArrayList<String>();
        for(int i = 0; i < l.length() - 1; i++) {
          list2grams.add(l.substring(i, i+2))
        }
        return new Tuple2<>(s,list2grams);
      });

      //Mit .mapValues() wird eine Transformation auf jedem Key-Value-Paar durchgefuehrt
      //Der Key bleibt gleich, die Werte veraendern sich
      //Zurueck bleibt ein neues JavaPairRDD
      JavaPairRDD<String, HashSet> unique2Grams = twoGrams.mapValues(l -> {
        HashSet<String> result = new HashSet<String>();
        for(Object o : l) {
          result.add(o.toString());
        }
        return result;
      });

      //Hier wird final ein letztes PairRDD erstellt, indem erneut die Paare transformiert werden
      //Der Key bleibt gleich, der Value wird auf die Anzahl der unique 2-grams gesetzt (Groesse des Sets)
      JavaPairRDD<String, Integer> final = unique2Grams.mapValues(set -> {
        return set.size();
      });

      //Eine Liste mit finalen Tupeln (Key-Value-Paaren) wird von dem RDD eingesammelt, damit diese ausgegeben werden koennen
      List<Tuple2<String, Integer>> output = final.collect();
      for (Tuple2<?, ?> tuple : output) {
          System.out.println(tuple._1() + ": " + tuple._2());
      } 

      //Mit .stop() werden alle Verbindungen zum Spark-Cluster geschlossen, sowie alle ressourcen freigegeben
      //Der Kontext kann danach nicht erneut verwendet werden
      ctx.stop();

      //Mit .close() werden auch alle Verbindungen geschlossen, sowie alle Ressourcen freigegeben
      //Diese Methode ist Java-spezifisch
      ctx.close();
  } 

}