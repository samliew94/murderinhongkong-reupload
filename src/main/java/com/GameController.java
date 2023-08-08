package com;

import java.security.Principal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@SuppressWarnings({"rawtypes"})
@RestController
@RequestMapping("game")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class GameController {

	@Autowired
	@Lazy
	MyWebSocketHandler webSocketHandler;

	@Autowired
	LobbyService lobbyService;
	
	@Autowired
	GameSettingsService gameSettingsService;
	
	@Autowired
	UserService userService;

	@Autowired
	GameLogService gameLogService;
	
	@Autowired
	GameProgressService gameProgressService;
	
	@Autowired
	ClueMeanService clueMeanService;
	
	@Autowired
	GameScreenService gameScreenService;

	@Autowired
	InvestigationService investigationService;
	
	@Autowired
	SceneTileService sceneTileService;
	
	@Autowired
	UserRoleService userRoleService;
	
	@Autowired
	GameOverService gameOverService;
	
	/**
	 * publishes message to all users ;
	 */
	public void update() throws Exception {

		Map data = null;
		
		GameProgressData gpd = gameProgressService.get();
		
		if (gpd == GameProgressData.LOBBY)
			data = lobbyService.getLobbyData();
		else if (gpd == GameProgressData.SETTINGS)
			data = gameSettingsService.getGameSettingsData();
		else if (gpd == GameProgressData.SELECT_FORENSIC)
			data = gameSettingsService.getSelectForensicData();
		else if (gpd == GameProgressData.SELECT_CLUE_AND_MEAN)
			data = clueMeanService.getCluesAndMeansData();
		else if (gpd == GameProgressData.INVESTIGATION)
			data = investigationService.getInvestigationData();
		else if (gpd == GameProgressData.GAME_OVER)
			data = gameOverService.getGameOverData();
		else
			return;
		
		webSocketHandler.broadcast(data);
			
		
	}
	
	@PostMapping("tosettings")
	public void toSettings(Principal principal) throws Exception {
		
		if (!userService.isPrincipalHost(principal))
			return;
		
		userRoleService.reset();
		clueMeanService.reset();
		sceneTileService.reset();
		
		gameProgressService.update(GameProgressData.SETTINGS);
		
		update();
	}
	
	@PostMapping("updatenumcluemeans")
	public void updateNumClueMeans(@RequestBody Map body, Principal principal) throws Exception {
		
		if (!userService.isPrincipalHost(principal))
			return;
		
		gameSettingsService.updateNumClueMeans(body, principal);
		
		update();
	}
	
	@PostMapping("toggleoptionalroles")
	public void toggleOptionalRoles(Principal principal) throws Exception {
		
		if (!userService.isPrincipalHost(principal))
			return;
		
		gameSettingsService.toggleOptionalRoles();
		
		update();
	}
	
	@PostMapping("toselectforensic")
	public void toSelectForensic(Principal principal) throws Exception {
		
		System.err.println("game/toselectforensic");
		
		if (!userService.isPrincipalHost(principal))
			return;
		
		gameProgressService.update(GameProgressData.SELECT_FORENSIC);
		
		update();
	}
	
	@PostMapping("onselectedforensic")
	public void onSelectedForensic(@RequestBody Map body, Principal principal) throws Exception {
		
		System.err.println("game/onselectedforensic");
		
		if (!userService.isPrincipalHost(principal))
			return;
		
		userRoleService.onSelectedForensic(body);
		
		update();
	}

	@PostMapping("toselectclueandmean")
	public void toSelectClueAndMean(Principal principal) throws Exception {
		
		System.err.println("game/toselectclueandmean");
		
		if (!userService.isPrincipalHost(principal))
			return;
		
		clueMeanService.reset();
		
		gameProgressService.update(GameProgressData.SELECT_CLUE_AND_MEAN);
		
		update();
	}
	
	@PostMapping("onselectedclueandmean")
	public synchronized void onSelectedClueAndMean(@RequestBody Map body, Principal principal) throws Exception {
		
		System.err.println("game/onselectedclueandmean");
		
		clueMeanService.onSelectedClueAndMean(body, principal);
		
		update();
	}
	
	@PostMapping("onselectedanalysis")
	public void onSelectedAnalysis(@RequestBody Map body, Principal principal) throws Exception {
		
		sceneTileService.onSelectedAnalysis(body, principal);
		
		update();
	}
	
	@PostMapping("onnextanalysis")
	public void onNextAnalysis(Principal principal) throws Exception {
		
		sceneTileService.onNextAnalysis(principal);
		
		update();
	}
	
	@PostMapping("onremovedanalysis")
	public void onRemovedAnalysis(@RequestBody Map body, Principal principal) throws Exception {
		
		sceneTileService.onRemovedAnalysis(body, principal);
		
		update();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// foo bars
	@PostMapping("tolobby")
	public void toLobby(HttpServletRequest request, Principal principal) throws Exception {

		System.err.println("game/tolobby");

		if (!userService.isPrincipalHost(principal))
			return;
		
		userRoleService.hardReset();
		clueMeanService.hardReset();
		
		gameProgressService.update(GameProgressData.LOBBY);
		
		update();

	}
	
	
	
	@PostMapping("kick")
	public void kick(@RequestBody Map requestMap, Principal principal) throws Exception {
		
		System.err.println("game/kick");
		
		if (!userService.isPrincipalHost(principal))
			return;
		
		lobbyService.onKick(requestMap, principal);
		
		update();
		
	}
}
