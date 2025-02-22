package br.com.nlw.events.services;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.nlw.dto.SubscriptionRankingByUser;
import br.com.nlw.dto.SubscriptionRankingItem;
import br.com.nlw.dto.SubscriptionResponse;
import br.com.nlw.events.exception.EventNotFoundException;
import br.com.nlw.events.exception.SubscriptionConflictException;
import br.com.nlw.events.exception.UserIndicatorNotFoundException;
import br.com.nlw.events.models.Event;
import br.com.nlw.events.models.Subscription;
import br.com.nlw.events.models.User;
import br.com.nlw.events.repositories.EventRepository;
import br.com.nlw.events.repositories.SubscriptionRepository;
import br.com.nlw.events.repositories.UserRepository;

@Service
public class SubscriptionService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    public SubscriptionResponse createNewSubscription(String eventName, User user, Integer userId) {
        // Recupera o evento pelo nome formatado
        Event event = eventRepository.findByPrettyName(eventName);
        // Se o evento não existir, uma exceção é lançada
        if (event == null) {
            throw new EventNotFoundException("Evento " + eventName + " não existe");
        }

        // Verifica se o usuário existe na base pelo e-mail cadastrado
        User userRec = userRepository.findByEmail(user.getEmail());
        if (userRec == null) {
            userRec = userRepository.save(user);
        }
        
        // Por padrão o ID está definido como null. Caso contrário, a API o busca no banco
        User indicator = null;
        if (userId != null) {
            indicator = userRepository.findById(userId).orElse(null); 
            if (indicator == null) {
                throw new UserIndicatorNotFoundException("Usuário " + userId + " indicador não existe");
            }
        }

        Subscription subs = new Subscription();
        subs.setEvent(event);
        subs.setSubscriber(userRec);
        subs.setIndication(indicator);

        Subscription tmpSubscriber = subscriptionRepository.findByEventAndSubscriber(event, userRec);
        if (tmpSubscriber != null) {
            throw new SubscriptionConflictException("O usuário " + userRec.getName() + " já está inscrito no evento " + event.getTitle());
        }
        
        Subscription res = subscriptionRepository.save(subs);
        
        return new SubscriptionResponse(res.getSubscriptionNumber(), "http://codecraft.com/subscription/" + res.getEvent().getPrettyName() + "/" + res.getSubscriber().getUser_id());
    }

    public List<SubscriptionRankingItem> getCompleteRanking(String prettyName) {
        Event event = eventRepository.findByPrettyName(prettyName);
        if (event == null) {
            throw new EventNotFoundException("O ranking do evento" + prettyName + " não existe");
        }
        return subscriptionRepository.generateRanking(event.getEventId());
    }

    public SubscriptionRankingByUser getRankingByUser(String prettyName, Integer userId) {
        List<SubscriptionRankingItem> ranking = getCompleteRanking(prettyName);
        SubscriptionRankingItem item = ranking.stream().filter(i -> i.userId().equals(userId)).findFirst().orElse(null);
        if (item == null) {
            throw new UserIndicatorNotFoundException("Não existe inscrição com indicação do usuário " + userId);
        }
        
        Integer position = IntStream.range(0, ranking.size())
                           .filter(pos -> ranking.get(pos).userId().equals(userId)) 
                           .findFirst().getAsInt();

        return new SubscriptionRankingByUser(item, position + 1);
    } 
}
