package br.com.nlw.events.repositories;

import org.springframework.data.repository.CrudRepository;

import br.com.nlw.events.models.Event;
import br.com.nlw.events.models.Subscription;
import br.com.nlw.events.models.User;

public interface SubscriptionRepository extends CrudRepository<Subscription, Integer> {
    public Subscription findByEventAndSubscriber(Event event, User user);
}
