package com.sparta.jwt.config.security;


import com.sparta.jwt.domain.user.entity.User;
import com.sparta.jwt.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		User user = userRepository.findByUserName(userName)
			.orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 유저 이메일입니다."));

		return new UserDetailsImpl(user);
	}
}