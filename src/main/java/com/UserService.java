package com;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@SuppressWarnings({"unchecked", "rawtypes"})
@Service
public class UserService {

	@Autowired
	MyUserRepository userRepository;

	public Map getUsersInLobby() throws Exception {

		List<MyUser> users = userRepository.findAll();

		List<Map> playerList = new ArrayList<>();

		Map data = new HashMap<>();
		data.put("screen", GameScreenData.LOBBY.getValue());
		data.put("players", playerList);

		Map responseMap = new HashMap<>();

		for (MyUser user : users) {

			Map map = new HashMap();
			map.put("isHost", user.isHost());
			responseMap.put(user.getUsername(), map);

		}

		return responseMap;

	}

	public List<MyUser> findAll() {
		// TODO Auto-generated method stub
		return userRepository.findAll();
	}
	
	public Set<MyUser> findAllAsSet(){
		return findAll().stream().collect(Collectors.toSet());
	}

	public boolean isPrincipalHost(Principal principal) throws Exception{
		
		MyUser user = userRepository.findByUsername(principal.getName());
		
		return user.isHost();
		
	}

	public int totalPlayers() {
		// TODO Auto-generated method stub
		return userRepository.findAll().size();
	}

	public MyUser findByUsername(String username) {
		// TODO Auto-generated method stub
		return userRepository.findByUsername(username);
	}

	public MyUser findHost() {
		// TODO Auto-generated method stub
		return userRepository.findByIsHost(true);
	
	}

}
