package com.motherland.booster;

import java.time.Instant;
import java.util.Date;

public class Booster {
    private String type;
    private int percent;
    private Date expire;
    private int time;
    private PlayerDataConfig config;

    public Booster(Date e, int percent, int time, String type, PlayerDataConfig config) {
	this.type = type;
	this.percent = percent;
	this.expire = e;
	this.time = time;
	this.config = config;
    }

    public boolean hasExpired() {
	if (expire.before(Date.from(Instant.now().minusSeconds(time * 60)))) {
	    String instant = expire.toInstant().toString();
	    instant = instant.replaceAll("\\.", "%d%").replace(":", "%c%");
	    config.set("boosters." + instant, null);
	    return true;
	}
	return false;
    }

    public int getPercent() {
	return percent;
    }

    public String getType() {
	return type;
    }
}
