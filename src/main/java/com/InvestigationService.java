package com;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@SuppressWarnings({"rawtypes", "unchecked", "serial"})
@Service
public class InvestigationService {
	
	Random random = new SecureRandom();
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserRoleService userRoleService;
	
	@Autowired
	ClueMeanService clueMeanService;
	
	@Autowired
	SceneTileService sceneTileService;
	
	@Autowired
	TimerService timerService;
	
	@Autowired
	RefRoleService refRoleService;
	
	public Map getInvestigationData() {
		
		Map responseMap = new HashMap<>();
		
		for(MyUser user : userService.findAll()) {
			
			Map map = new HashMap();
			map.put("isHost", user.isHost());
			map.put("timeRemaining", timerService.getTimeRemaining());
			map.put("round", sceneTileService.getRound());			
			
			GameRoles gameRole = userRoleService.findGameRoleByUser(user);
			map.put("roleCode", gameRole.getValue());
			
			if(gameRole == GameRoles.FORENSIC)
				map.putAll(getForensicData());
			else if (gameRole == GameRoles.MURDERER)
				map.putAll(getMurdererData());
			else if (gameRole == GameRoles.INVESTIGATOR)
				map.putAll(getInvestigatorData());
			else if (gameRole == GameRoles.ACCOMPLICE)
				map.putAll(getAccompliceData());
			else if (gameRole == GameRoles.WITNESS)
				map.putAll(getWitnessData());

			responseMap.put(user.getUsername(), map);


		}

		return responseMap;
	}
	
	/**
	 */
	private Map getForensicData() {
		
		Map map = new HashMap<>();
		
		map.putAll(getMurdererData());
		
		if (sceneTileService.isFullAnalysisGivenForTheRound()) {
			map.put("screen", GameScreenData.VIEW_ANALYSIS.getValue());
			map.put("analysis", sceneTileService.getAvailableAnalysis());
			map.put("suspects", getSuspects());
		}
		else {
			
			if (sceneTileService.forensicNeedsToRemoveAnalysis()) {
				
				map.put("screen", GameScreenData.REMOVE_ANALYSIS.getValue());
				map.put("analysis", sceneTileService.getAvailableAnalysis());
				
			} else {
				
				map.put("screen", GameScreenData.SELECT_ANALYSIS.getValue());
				map.put("analysis", sceneTileService.getPendingAnalysis());
				
			}
			
		}
		
		return map;
	}
	
	private Map getMurdererData() {
		
		Map map = new HashMap<>();
		
		UserRoles murderer = userRoleService.findMurderer();
		MyUser murdererUser = murderer.getUser();
		String murdererUsername = murdererUser.getUsername();
		
		UserClueMean userClueMeanMurderer = clueMeanService.findByUser(murdererUser);
		List<UserClueMeanOption> clueMeanOptions = clueMeanService.findAllUserClueMeanOptionsByUser(murdererUser);

		map.put("screen", GameScreenData.VIEW_ANALYSIS.getValue());
		map.put("murderer", murdererUsername);
		map.put("clue", userClueMeanMurderer.getClue());
		map.put("mean", userClueMeanMurderer.getMean());
		map.put("clues", clueMeanOptions.stream().map(x->x.getClue()).collect(Collectors.toList()));
		map.put("means", clueMeanOptions.stream().map(x->x.getMean()).collect(Collectors.toList()));
		map.put("analysis", sceneTileService.getAvailableAnalysis());
		map.put("suspects", getSuspects());

		return map;
		
	}
	
	private Map getInvestigatorData() {
		// TODO Auto-generated method stub
		Map map = new HashMap<>();
		
		map.put("screen", GameScreenData.VIEW_ANALYSIS.getValue());
		map.put("analysis", sceneTileService.getAvailableAnalysis());
		map.put("suspects", getSuspects());;
		
		return map;
	}
	
	private Map getAccompliceData() {
		// TODO Auto-generated method stub
		Map map = new HashMap<>();
		
		map.put("screen", GameScreenData.VIEW_ANALYSIS.getValue());
		map.put("analysis", sceneTileService.getAvailableAnalysis());
		map.put("suspects", getSuspects());;
		map.putAll(getMurdererData());;
		
		return map;
	}
	
	private Map getWitnessData() {
		// TODO Auto-generated method stub
		Map map = new HashMap<>();
		
		map.put("screen", GameScreenData.VIEW_ANALYSIS.getValue());
		map.put("analysis", sceneTileService.getAvailableAnalysis());
		map.put("suspects", getSuspects());;
		
		List<String> usernames = userRoleService.findAllByGameRoles(GameRoles.MURDERER, GameRoles.ACCOMPLICE)
				.stream().map(x->x.getUser().getUsername()).collect(Collectors.toList());
		
		Collections.shuffle(usernames);
		
		map.put("murdererOrAccomplice", usernames);
		
		return map;
	}
	
	private List<Map> getSuspects() {
		
		List<UserRoles> userRoles = userRoleService.findAllExceptForensic();
		
		List<Map> list = new ArrayList<>();
		
		for(UserRoles userRole : userRoles) {
			
			List<UserClueMeanOption> options = clueMeanService.findAllUserClueMeanOptionsByUser(userRole.getUser());
			
			Map map = new HashMap<>();
			
			map.put("username", userRole.getUser().getUsername());
			map.put("clues", options.stream().map(x->x.getClue()).collect(Collectors.toList()));
			map.put("means", options.stream().map(x->x.getMean()).collect(Collectors.toList()));
			
			list.add(map);
			
		}
		
		return list;
	}

	public void assignRoles() {
		
		randomMurderer();
		randomOptionalRoles();
		assignInvestigators();
		
	}
	
	/**
	 * create murderer if not found
	 */
	private void randomMurderer() {
		
		List<UserRoles> all = userRoleService.findAll();
		all.remove(userRoleService.findForensic());
		
		int murdererId = random.nextInt(all.size());
		UserRoles murderer = all.get(murdererId);
		userRoleService.updateUserRole(murderer, GameRoles.MURDERER);
		
		System.err.println(murderer.getUser().getUsername() + " is assigned the murderer");
		
	}
	
	private void randomOptionalRoles() {
		
		if (!refRoleService.areOptionalRolesEnabled())
			return;
		
		List<UserRoles> userRoles = userRoleService.findAllWithoutRoles();
		
		int randomAccomplice = random.nextInt(userRoles.size());
		
		UserRoles accomplice = userRoles.get(randomAccomplice);
		userRoleService.updateUserRole(accomplice, GameRoles.ACCOMPLICE);
		userRoles.remove(accomplice);
		
		int randomWitness = random.nextInt(userRoles.size());
		UserRoles witness = userRoles.get(randomWitness);
		userRoleService.updateUserRole(witness, GameRoles.WITNESS);
		
	}
	

	private void assignInvestigators() {
		// TODO Auto-generated method stub
		List<UserRoles> userRoles = userRoleService.findAllWithoutRoles();
		userRoles.forEach(x->{
			userRoleService.updateUserRole(x, GameRoles.INVESTIGATOR);
		});
	}
	
}

















