package com;

import java.security.Principal;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Handles userClueMean and userClueMeanOption repositories
 * 
 *
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@Service
public class ClueMeanService {
	
	@Autowired
	UserService userService;
	
	@Autowired
	GameSettingsService gameSettingsService;
	
	@Autowired
	UserRoleService userRoleService;
	
	@Autowired
	GameProgressService gameProgressService;
	
	@Autowired
	UserClueMeanRepository userClueMeanRepository;
	
	@Autowired
	UserClueMeanOptionsRepository userClueMeanOptionsRepository;
	
	@Autowired
	@Lazy
	InvestigationService investigationService;
	
	@Autowired
	TimerService timerService;
	
	Random random = new SecureRandom();
	
	public void reset() {
		
		userClueMeanRepository.deleteAll();
		userClueMeanOptionsRepository.deleteAll();
		
		for(MyUser user : userService.findAll()) {
			
			if (userRoleService.userIsForensic(user))
				continue;
			
			userClueMeanRepository.save(UserClueMean.builder().user(user).clue(-1).mean(-1).build());
			
		}
		
	}
	
	/**
	 * when redirecting back to lobby
	 */
	public void hardReset() {
		userClueMeanRepository.deleteAll();
		userClueMeanOptionsRepository.deleteAll();
	}

	public Map getCluesAndMeansData() throws Exception {
		
		if (userClueMeanOptionsRepository.findAll().size() == 0)
			randomCluesAndMeans();
		
		Map responseMap = new HashMap<>();
		
		Set<MyUser> pendingUsers = playersNotYetSelectedBothClueAndMean();
		
		for (MyUser user : userService.findAll()) {
			
			Map map = new HashMap<>();

			if (!pendingUsers.contains(user)) {
				
				map.put("screen", GameScreenData.WAITING_FOR.getValue());
				map.put("waitingFor", pendingUsers.stream().map(x->x.getUsername()).collect(Collectors.toList()));
				map.put("isHost", user.isHost());
				
			} else {
				
				UserClueMean userClueMean = userClueMeanRepository.findByUser(user);
				
				map.put("selectedClue", userClueMean.getClue()); 
				map.put("selectedMean", userClueMean.getMean());
				map.put("screen", GameScreenData.SELECT_CLUE_MEAN.getValue());
				map.put("isHost", user.isHost());
				
				List<UserClueMeanOption> userClueMeanOptions = userClueMeanOptionsRepository.findAllByUser(user);
				
				map.put("clues", userClueMeanOptions.stream().map(x->x.getClue()).collect(Collectors.toList()));
				map.put("means", userClueMeanOptions.stream().map(x->x.getMean()).collect(Collectors.toList()));
				
			}
			
			responseMap.put(user.getUsername(), map);
			
		}
		
		return responseMap;
	}

	/**
	 * each player gets dealt a number of clue and means. <br/> 
	 * There can be no duplicates
	 */
	private void randomCluesAndMeans() {
		
		int totalClueAndMeans = gameSettingsService.getTotalClueAndMeans();
		
		for (MyUser user : userService.findAll()) {
			
			for (int i = 0; i < totalClueAndMeans; i++) {
				
				int clue = -1;
				int mean = -1;
				
				while(clue == -1 || mean == -1 || isClueTaken(clue) || isMeanTaken(mean)) {
					
					clue = random.nextInt(199);
					mean = random.nextInt(88);
					
				}
				
				UserClueMeanOption userClueMean  = UserClueMeanOption.builder().user(user).clue(clue).mean(mean).build();
				userClueMeanOptionsRepository.save(userClueMean);
				
			}
			
			
		}
	}
	
//	public void onSelectedClueOrMean(Map body, Principal principal) throws Exception {
//		
//		MyUser user = userService.findByUsername(principal.getName());
//		UserClueMean userClueMean = userClueMeanRepository.findByUser(user);
//		
//		int clue = (int) body.get("clue");
//		int mean = (int) body.get("mean");
//		
//		if (clue != -1)
//			userClueMean.setClue(clue);	
//		
//		if (mean != -1)
//			userClueMean.setMean(mean);
//		
//		userClueMeanRepository.save(userClueMean);
//		
//	}
	
	public void onSelectedClueAndMean(Map body, Principal principal) throws Exception {

		MyUser user = userService.findByUsername(principal.getName());
		
		UserClueMean userClueMean = userClueMeanRepository.findByUser(user);
		userClueMean.setClue((int) body.get("clue"));
		userClueMean.setMean((int) body.get("mean"));
		userClueMean.setConfirmed(true);
		
		userClueMeanRepository.save(userClueMean);
		
		List<UserClueMean> all = userClueMeanRepository.findAllByIsConfirmed(true);
		
		// -1 because forensic is the only one who don't need to select clue/mean
		if (all.size() == userService.findAll().size()-1) {
			gameProgressService.update(GameProgressData.INVESTIGATION);
			investigationService.assignRoles();
			timerService.start();
		}
			
	}
	
	private Set<MyUser> playersNotYetSelectedBothClueAndMean() throws Exception{
		
		Set<MyUser> all = userService.findAllAsSet();
		
		all.remove(userRoleService.findForensic().getUser());

		Set<MyUser> confirms = userClueMeanRepository.findAllByIsConfirmed(true).stream().map(x->x.getUser()).collect(Collectors.toSet());
		
		all.removeAll(confirms);
		
		return all;
	}
	
	private boolean isClueTaken(int clue) {
		
		return userClueMeanOptionsRepository.findByClue(clue) != null;
		
	}
	
	private boolean isMeanTaken(int mean) {
		
		return userClueMeanOptionsRepository.findByMean(mean) != null;
		
	}
	
	public UserClueMean findByUser(MyUser user) {
		return userClueMeanRepository.findByUser(user);
	}

	public List<UserClueMeanOption> findAllUserClueMeanOptionsByUser(MyUser user){
		
		List<UserClueMeanOption> all = userClueMeanOptionsRepository.findAllByUserOrderByUserClueMeanIdAsc(user);
		
		return all;
	}
}

