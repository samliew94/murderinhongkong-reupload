package com;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

@Service
@Transactional
public class GameProgressService {

	@Autowired
	GameProgressRepository gameProgressRepository;
	
	@PostConstruct
	void init() {
		
		reset();
		
	}

	private void reset() {
		GameProgress gameProgress = new GameProgress();
		gameProgress.setCode(GameProgressData.LOBBY.getValue());
		gameProgressRepository.save(gameProgress);
	}
	
	public boolean update(GameProgressData gpd) {
		
		GameProgress gameProgress = gameProgressRepository.findAll().get(0);
		
		gameProgress.setCode(gpd.getValue());
		
		System.err.println("game progress updated to " + gameProgress.getCode());
		
		gameProgressRepository.save(gameProgress);
		
		return true;
		
	}
	
	public GameProgressData get() {
		
		GameProgress gameProgress = gameProgressRepository.findAll().get(0);
		
		String code = gameProgress.getCode();
		
		return GameProgressData.findEnumByValue(code);
		
	}
	
}