import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;
import parser.SubscriptionParser;
import subscription.Subscription;
import subscription.SingleSubscription;
import feed.Feed;
import feed.Article;
import parser.RssParser;
import namedEntity.heuristic.Heuristic;
import namedEntity.heuristic.QuickHeuristic;
import java.util.*;
import java.util.stream.Collectors;

public class SparkFeedFetcher {
    public static void main(String[] args) {
        // 1. Crear SparkSession
        SparkSession spark = SparkSession.builder()
                .appName("SparkFeedFetcher")
                .master("local[*]")
                .getOrCreate();

        // 1b. Crear JavaSparkContext
        JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());

        // 2. Leer subscriptions.json y obtener lista de URLs RSS
        Subscription subscriptions = SubscriptionParser.parse("config/subscriptions.json");
        List<String> rssUrls = new ArrayList<>();
        for (SingleSubscription sub : subscriptions.getSubscriptionsList()) {
            if (sub.getUrlType().equalsIgnoreCase("rss")) {
                for (int i = 0; i < sub.getUrlParamsSize(); i++) {
                    rssUrls.add(sub.getFeedToRequest(i));
                }
            }
        }

        // 3. Crear un RDD de URLs usando JavaSparkContext
        JavaRDD<String> urlsRDD = jsc.parallelize(rssUrls, rssUrls.size());

        // 4. Descargar y parsear feeds en paralelo
        JavaRDD<Article> articlesRDD = urlsRDD.flatMap(url -> {
            List<Article> articles = new ArrayList<>();
            try {
                // Descargar el feed
                String xml = new httpRequest.httpRequester().getFeedRss(url);
                if (xml != null) {
                    // Parsear el feed
                    Feed feed = new RssParser().parse(xml, url);
                    articles.addAll(feed.getArticleList());
                }
            } catch (Exception e) {
                // Manejar errores de descarga/parseo
            }
            return articles.iterator();
        });

        // 5. Extraer entidades nombradas de cada artículo en paralelo
        JavaRDD<String> entitiesRDD = articlesRDD.flatMap(article -> {
            Heuristic heuristic = new QuickHeuristic();
            article.computeNamedEntities(heuristic);
            List<String> entities = article.getNamedEntityList().stream()
                    .map(ne -> ne.getName())
                    .collect(Collectors.toList());
            return entities.iterator();
        });

        // 6. Contar entidades nombradas
        JavaPairRDD<String, Integer> entityCounts = entitiesRDD.mapToPair(entity -> new Tuple2<>(entity, 1))
                .reduceByKey(Integer::sum);

        // 7. Mostrar resultados
        List<Tuple2<String, Integer>> output = entityCounts.collect();
        for (Tuple2<String, Integer> tuple : output) {
            System.out.println(tuple._1() + ": " + tuple._2());
        }

        spark.stop();
    }
} 