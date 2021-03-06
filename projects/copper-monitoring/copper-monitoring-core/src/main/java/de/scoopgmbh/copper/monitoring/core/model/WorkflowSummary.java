/*
 * Copyright 2002-2013 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.scoopgmbh.copper.monitoring.core.model;

import java.io.Serializable;

public class WorkflowSummary implements Serializable {
	private static final long serialVersionUID = 4867510351238162279L;
	
	private String alias;
	private int totalcount;
	private WorkflowStateSummary stateSummery;
	private WorkflowClassMetaData classDescription;
	
	public WorkflowSummary() {
		super();
	}

	public WorkflowSummary(String alias, int totalcount, WorkflowClassMetaData classDescription, WorkflowStateSummary stateSummery) {
		super();
		this.alias = alias;
		this.totalcount = totalcount;
		this.stateSummery = stateSummery;
		this.classDescription = classDescription;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public int getTotalcount() {
		return totalcount;
	}

	public void setTotalcount(int totalcount) {
		this.totalcount = totalcount;
	}

	public WorkflowStateSummary getStateSummary() {
		return stateSummery;
	}

	public void setStateSummary(WorkflowStateSummary stateSummery) {
		this.stateSummery = stateSummery;
	}

	public WorkflowClassMetaData getClassDescription() {
		return classDescription;
	}

	public void setClassDescription(WorkflowClassMetaData classDescription) {
		this.classDescription = classDescription;
	}


	


	
	
	
	
}
