package br.com.nlw.events.repositories;

import org.springframework.data.repository.CrudRepository;

import br.com.nlw.events.models.User;

public interface UserRepository extends CrudRepository<User, Integer> {
    public User findByEmail(String email);
}
