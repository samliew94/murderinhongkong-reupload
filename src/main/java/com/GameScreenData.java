package com;

public enum GameScreenData {
	
	LOBBY("lobby"),
	SETTINGS("settings"),
	WAITING_FOR("waitingfor"),
	SELECT_FORENSIC("selectforensic"),
	SELECT_CLUE_MEAN("selectcluemean"),
	SELECT_ANALYSIS("selectanalysis"),
	VIEW_ANALYSIS("viewanalysis"),
	REMOVE_ANALYSIS("removeanalysis"),
	GAME_OVER("gg"),
	;
	
	private final String value;
	
	private GameScreenData(String value) {
		this.value = value;
	}
	
	public String getValue() { return value; }
	
}





