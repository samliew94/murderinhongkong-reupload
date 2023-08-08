package com;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@SuppressWarnings({"unchecked", "rawtypes"})
@Service
public class GameOverService {
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserRoleService userRoleService;
	
	@Autowired
	ClueMeanService clueMeanService;
	
	@Autowired
	RefRoleService refRoleService;
	
	public Map getGameOverData() {
		
		Map responseMap = new HashMap<>();
		
		UserRoles murderer = userRoleService.findMurderer();
		
		UserClueMean userClueMean = clueMeanService.findByUser(murderer.getUser());

		List<UserClueMeanOption> clueMeanOptions = clueMeanService.findAllUserClueMeanOptionsByUser(murderer.getUser());

		boolean areOptionalRolesEnabled = refRoleService.areOptionalRolesEnabled();
		
		Map optionalRolesMap = null;
		
		if (areOptionalRolesEnabled) {
			
			optionalRolesMap = new LinkedHashMap<>();
			
			String accomplice = userRoleService.findByRole(GameRoles.ACCOMPLICE).getUser().getUsername();
			String witness = userRoleService.findByRole(GameRoles.WITNESS).getUser().getUsername();
			
			optionalRolesMap.put("accomplice", accomplice);
			optionalRolesMap.put("witness", witness);
			
		}
		
		for(MyUser user : userService.findAll()) {
			
			Map map = new HashMap<>();
			
			map.put("isHost", user.isHost());
			map.put("screen", GameScreenData.GAME_OVER.getValue());
			map.put("murderer", userClueMean.getUser().getUsername());
			map.put("clue", userClueMean.getClue());
			map.put("mean", userClueMean.getMean());
			map.put("clues", clueMeanOptions.stream().map(UserClueMeanOption::getClue).collect(Collectors.toList()));
			map.put("means", clueMeanOptions.stream().map(UserClueMeanOption::getMean).collect(Collectors.toList()));

			if (optionalRolesMap != null)
				map.putAll(optionalRolesMap);
			
			responseMap.put(user.getUsername(), map);
			
		}
		
		return responseMap;
	}
	
}
