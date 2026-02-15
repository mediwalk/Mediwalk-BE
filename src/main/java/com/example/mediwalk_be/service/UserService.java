package com.example.mediwalk_be.service;

import com.example.mediwalk_be.entity.User;
import com.example.mediwalk_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;

	public Optional<User> findById(Long id) {
		return userRepository.findById(id);
	}

	public User getById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("User not found: id=" + id));
	}

	public Optional<User> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	public List<User> findAll() {
		return userRepository.findAll();
	}

	@Transactional
	public User save(User user) {
		return userRepository.save(user);
	}

	@Transactional
	public void deleteById(Long id) {
		userRepository.deleteById(id);
	}
}
