package com;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class GameSettingsService {
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserRoleService userRoleService;
	
	@Autowired
	RefRoleService refRoleService;
	
	private int totalClueMeans = 3;
	private int minClueMeans = 3;
	private int maxClueMeans = 5;
	
	public Map getGameSettingsData() {
		
		List<MyUser> users = userService.findAll();
		
		Map responseMap = new HashMap<>();
		
		boolean isOptionalRoles = refRoleService.areOptionalRolesEnabled();
		
		for (MyUser user : users) {
			
			Map map = new HashMap();
			map.put("screen", GameScreenData.SETTINGS.getValue());
			map.put("isHost", user.isHost());
			map.put("totalClueMeans", totalClueMeans);
			map.put("isOptionalRoles", isOptionalRoles);
			
			responseMap.put(user.getUsername(), map);
			
		}
			
		return responseMap;
	}
	
	public Map getSelectForensicData() {
		
		List<MyUser> users = userService.findAll();
		
		Map responseMap = new HashMap<>();
		
		List<String> usernames = userService.findAll().stream().map(x->x.getUsername()).collect(Collectors.toList());
		
		String forensic = userRoleService.findForensic().getUser().getUsername();
		
		for (MyUser user : users) {
			
			Map map = new HashMap();
			map.put("screen", GameScreenData.SELECT_FORENSIC.getValue());
			map.put("forensic", forensic);
			map.put("isHost", user.isHost());
			map.put("players", usernames);
			
			responseMap.put(user.getUsername(), map);
			
		}
			
		return responseMap;
		
	}
	
	
	public void updateNumClueMeans(Map body, Principal principal) throws Exception {
		
		boolean increment = (boolean) body.get("increment");
		
		if (increment && totalClueMeans < maxClueMeans)
			totalClueMeans += 1;
		else if (!increment && totalClueMeans > minClueMeans)
			totalClueMeans -= 1;
		
	}
	
	public void toggleOptionalRoles() throws Exception {
		
		boolean isEnabled = refRoleService.areOptionalRolesEnabled();
		
		if (!isEnabled && userRoleService.findAll().size() < 6)
			return;
		
		refRoleService.toggleOptionalRoles();
		
	}

	public int getTotalClueAndMeans() {
		// TODO Auto-generated method stub
		return totalClueMeans;
	}

	
	
	
	
}

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class GameSettings{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int gameSettingsId;
	private int minClueMean;
	private int maxClueMean;
	private boolean isAccomplice;
	private boolean isWitness;
	
	
}

interface GameSettingsRepository extends JpaRepository<GameSettings, Integer>{
	
	
	
}