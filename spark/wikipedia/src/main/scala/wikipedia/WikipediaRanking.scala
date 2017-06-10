package wikipedia

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

case class WikipediaArticle(title: String, text: String) {
  /**
    * @return Whether the text of this article mentions `lang` or not
    * @param lang Language to look for (e.g. "Scala")
    */
  def mentionsLanguage(lang: String): Boolean = text.split(' ').contains(lang)
}

object WikipediaRanking {

  val langs = List(
    "JavaScript", "Java", "PHP", "Python", "C#", "C++", "Ruby", "CSS",
    "Objective-C", "Perl", "Scala", "Haskell", "MATLAB", "Clojure", "Groovy")

  val conf: SparkConf = new SparkConf()
    .setAppName("Wikipedia Local Spark Server") // any string is fine
    .setMaster("local[*]") // local server
    .setExecutorEnv("spark.executor.memory", "6g")

  val sc: SparkContext = new SparkContext(conf)
  // Hint: use a combination of `sc.textFile`, `WikipediaData.filePath` and `WikipediaData.parse`
  val wikiRdd: RDD[WikipediaArticle] = sc.textFile(WikipediaData.filePath).map(l => WikipediaData.parse(l))

  /** Returns the number of articles on which the language `lang` occurs.
   *  Hint1: consider using method `aggregate` on RDD[T].
   *  Hint2: consider using method `mentionsLanguage` on `WikipediaArticle`
   */
  def occurrencesOfLang(lang: String, rdd: RDD[WikipediaArticle]): Int =
    rdd.filter(_.mentionsLanguage(lang)).aggregate(0)((acc, _) => acc + 1, _ + _)

  /* (1) Use `occurrencesOfLang` to compute the ranking of the languages
   *     (`val langs`) by determining the number of Wikipedia articles that
   *     mention each language at least once. Don't forget to sort the
   *     languages by their occurrence, in decreasing order!
   *
   *   Note: this operation is long-running. It can potentially run for
   *   several seconds.
   */
  def rankLangs(langs: List[String], rdd: RDD[WikipediaArticle]): List[(String, Int)] =
  langs.par.map(l => (l, occurrencesOfLang(l, rdd))).toList.sortBy(_._2).reverse

  /* Compute an inverted index of the set of articles, mapping each language
   * to the Wikipedia pages in which it occurs.
   * Hint: You might want to use methods 𝚏𝚕𝚊𝚝𝙼𝚊𝚙 and 𝚐𝚛𝚘𝚞𝚙𝙱𝚢𝙺𝚎𝚢 on 𝚁𝙳𝙳 for this part.
   */
  def makeIndex(langs: List[String], rdd: RDD[WikipediaArticle]): RDD[(String, Iterable[WikipediaArticle])] =
     rdd.flatMap(r => langs.flatMap(lang => if(r.mentionsLanguage(lang)) Some((lang, r)) else None)).groupByKey()

  /* (2) Compute the language ranking again, but now using the inverted index. Can you notice
   *     a performance improvement?
   *
   *   Note: this operation is long-running. It can potentially run for
   *   several seconds.
   *   Hint: method 𝚖𝚊𝚙𝚅𝚊𝚕𝚞𝚎𝚜 on 𝙿𝚊𝚒𝚛𝚁𝙳𝙳 could be useful for this part.
   */
  def rankLangsUsingIndex(index: RDD[(String, Iterable[WikipediaArticle])]): List[(String, Int)] =
    index.map(v => (v._1, v._2.count(_.mentionsLanguage(v._1)))).collect().toList.sortBy(_._2).reverse


  /* (3) Use `reduceByKey` so that the computation of the index and the ranking are combined.
   *     Can you notice an improvement in performance compared to measuring *both* the computation of the index
   *     and the computation of the ranking? If so, can you think of a reason?
   *
   *   Note: this operation is long-running. It can potentially run for
   *   several seconds.
   */
  def rankLangsReduceByKey(langs: List[String], rdd: RDD[WikipediaArticle]): List[(String, Int)] = {
    rdd.flatMap(r => langs.map(lang => (lang, if(r.mentionsLanguage(lang)) 1 else 0))).reduceByKey(_ + _).collect().toList.sortBy(_._2).reverse
  }

  def main(args: Array[String]) {

    /* Languages ranked according to (1) */
    val langsRanked: List[(String, Int)] = timed("Part 1: naive ranking", rankLangs(langs, wikiRdd))

    /* An inverted index mapping languages to wikipedia pages on which they appear */
    def index: RDD[(String, Iterable[WikipediaArticle])] = makeIndex(langs, wikiRdd)

    /* Languages ranked according to (2), using the inverted index */
    val langsRanked2: List[(String, Int)] = timed("Part 2: ranking using inverted index", rankLangsUsingIndex(index))

    /* Languages ranked according to (3) */
    val langsRanked3: List[(String, Int)] = timed("Part 3: ranking using reduceByKey", rankLangsReduceByKey(langs, wikiRdd))

    /* Output the speed of each ranking */
    println(timing)
    sc.stop()
  }

  val timing = new StringBuffer
  def timed[T](label: String, code: => T): T = {
    val start = System.currentTimeMillis()
    val result = code
    val stop = System.currentTimeMillis()
    timing.append(s"Processing $label took ${stop - start} ms.\n")
    result
  }
}
