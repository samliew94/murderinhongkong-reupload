package com;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Service
public class GameScreenService {
	
	@Autowired
	GameScreenRepository gameScreenRepository;
	
	@Autowired
	UserService userService;
	
	public void reset() {
		
		gameScreenRepository.deleteAll();
		
		for(MyUser user : userService.findAll()) {
			
			GameScreen build = GameScreen.builder().user(user).screenCode(GameScreenData.LOBBY.getValue()).build();
			
			gameScreenRepository.save(build);
			
		}
		
	}
	
}

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class GameScreen{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int gameScreenId;
	
	@ManyToOne
	@JoinColumn(name = "username")
	MyUser user;
	
	String screenCode;
	
	/**
	 * can the user's screen be refreshed?
	 */
	boolean lockScreen;
	
}

interface GameScreenRepository extends JpaRepository<GameScreen, Integer>{
	
}