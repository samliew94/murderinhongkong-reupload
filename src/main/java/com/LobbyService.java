package com;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class LobbyService {
	
	@Autowired
	MyUserRepository userRepository;
	
	@Autowired
	UserRolesRepository userRolesRepository;
	
	@Autowired
	RefRolesRepository refRolesRepository;
	
	@Autowired
	FindByIndexNameSessionRepository sessionRepository;
	
	@Autowired
	MyWebSocketHandler webSocketHandler;
	
	@Autowired
	UserService userService;
	
	public Map getLobbyData() throws Exception {
		
		List<MyUser> users = userRepository.findAllByOrderByUsernameAsc();
		
		List<Map> playerList = new ArrayList<>();
		
		MyUser host = userService.findHost();
		
		for(MyUser u : users) {
			
			Map player = new HashMap<>();
			player.put("username", u.getUsername());
			player.put("isHost", u.isHost());
			player.put("hostUsername", host.getUsername());
			playerList.add(player);
			
		}
		
		Map responseMap = new HashMap<>();
		
		for(MyUser user : users) {
			
			Map map = new HashMap();
			map.put("screen", GameScreenData.LOBBY.getValue());
			map.put("players", playerList);
			map.put("isHost", user.isHost());
			responseMap.put(user.getUsername(), map);
			
		}
		
		return responseMap;
	}

	public void onKick(Map requestBody, Principal principal) throws Exception {
		
		String kickUsername = (String) requestBody.get("username");
		
		MyUser kickUser = userService.findByUsername(kickUsername);
		
		if (kickUser.isHost())
			return;
		
		
		userRepository.delete(kickUser);
		webSocketHandler.disconnect(kickUsername);
		
		Map map = sessionRepository.findByPrincipalName(kickUsername);
		if (map != null && !map.isEmpty()) {
			
			Set<String> sessionIds = map.keySet();
			sessionIds.forEach(x->sessionRepository.deleteById(x));
			
		}
		
	}
	
	
	
	
	
}
