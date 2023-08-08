package com;

enum GameProgressData {
	
	LOBBY("lobby"),
	SETTINGS("settings"),
	SELECT_FORENSIC("selectforensic"),
    SELECT_CLUE_AND_MEAN("selectcluemean"), 
    INVESTIGATION("investigation"),
    GAME_OVER("gg") 
    ;
    
    private final String value;
	
    private GameProgressData(String value) {
    	this.value = value;
    }
    
    public String getValue() { return value; }
    
    public static GameProgressData findEnumByValue(String value) {
        for (GameProgressData enumValue : GameProgressData.values()) {
            if (enumValue.getValue().equals(value)) {
                return enumValue;
            }
        }
        return null; // If no matching enum value is found
    }
    
}