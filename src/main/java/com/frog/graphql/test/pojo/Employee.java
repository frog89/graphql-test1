package com.frog.graphql.test.pojo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Employee {
	private long id;
	private String firstName;
	private String lastName;
	private String jobId;
	
	private List<Job> jobList;
	
	public void setJob(Job job) {
		jobList = new ArrayList<Job>();
		jobList.add(job);
	}
	public Job getJob() {
		if (jobList == null) {
			return null;
		}
		return jobList.get(0);
	}
}
