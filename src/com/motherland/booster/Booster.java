package com.motherland.booster;

import net.luckperms.api.node.Node;

public class Booster {
    private long time;
    private double percent;
    private String type;
    private Node node;

    public Booster(String type, long time, double percent, Node node) {
	this.time = time;
	this.percent = percent;
	this.type = type;
	this.node = node;
    }

    public long getTime() {
	return time;
    }

    public double getPercent() {
	return percent;
    }

    public String getType() {
	return type;
    }

    public Node getNode() {
	return node;
    }

}
