package com.chiara;

/**
 * Created by Chiara on 09/04/17.
 */

public class TimeIdentifier {
    String part;

    public String getPartOfDay(Integer hour){
        if(hour >= 6 && hour < 12) {
            part = "morning";

        }else if(hour >= 12 && hour < 18){
            part = "afternoon";

        }else if(hour >= 18 && hour < 24){
            part = "evening";

        }else if(hour >= 0 && hour < 6){
            part = "night";
        }else{

        }
        return part;
    }
}
