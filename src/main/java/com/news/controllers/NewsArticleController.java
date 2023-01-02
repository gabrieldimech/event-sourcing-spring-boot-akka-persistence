package com.news.controllers;

import akka.actor.typed.ActorSystem;
import com.kafka.KafkaProducer;
import com.news.dto.NewsItemsRequestDto;
import com.news.entities.NewsArticle;
import com.news.messages.NewsArticleMessage;
import com.news.messages.PublishEventIntent;
import com.news.services.NewsArticleService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
public class NewsArticleController {
    @Autowired
    final ActorSystem<NewsArticleMessage> supervisor;
    @Autowired
    final NewsArticleService newsArticleService;
    @Autowired
    final KafkaProducer kafkaProducer;

    @GetMapping("/news/{topic}")
    public ResponseEntity<List<NewsArticle>> getNewsItems(@PathVariable String topic) {
        List<NewsArticle> newsArticles = newsArticleService.getNewsItemsByTopic(topic).toCompletableFuture().join();

        return ResponseEntity.ok(newsArticles);
    }

    @PostMapping("/news/publish")
    public ResponseEntity<Map<String, List<NewsArticle>>> publishNewsItems(@RequestBody NewsItemsRequestDto newsItemsRequestDto) {

        Map<String, List<NewsArticle>> result = new HashMap<>();
        // final ActorSystem<NewsArticleMessage> supervisorRef =
        //   ActorSystem.create(NewsArticleSupervisor.create(), "supervisor");
        //1) for each topic in request we want to instruct the supervisor to either spawn a new actor or use an existing one to spawn a NewsItemsWorker
        //2) each NewsItemsWorker will process a topic by invoking the NewsItemsWorker and this will retrieve the latest RSS new feed items
        //3) for each topic new item , we publish a kafka message on the relevant topic

 /*       ActorRef supervisorRef = actorSystem.actorOf(SPRING_EXTENSION_PROVIDER.get(actorSystem)
            .props("newsArticleSupervisor"), "newsArticleSupervisor");*/
        //final Optional<ActorRef<Void>> childActorRef =actorSystem .getContext().getChild(message.getTopic());

        for (String topic : newsItemsRequestDto.getTopics()) {
            supervisor.tell(new PublishEventIntent(topic));
        }

        return ResponseEntity.ok(result);
    }

}

