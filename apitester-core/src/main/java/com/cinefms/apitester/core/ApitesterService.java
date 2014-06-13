package com.cinefms.apitester.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.cinefms.apitester.model.ApiCrawler;
import com.cinefms.apitester.model.info.ApiCall;

@Component
public class ApitesterService implements ApplicationContextAware {
	
	private List<ApiCall> calls;
	private ApplicationContext applicationContext;

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	private List<ApiCall> getCallsInternal() {
		if(calls==null) {
			calls = new ArrayList<ApiCall>();
			for(ApiCrawler ac : applicationContext.getBeansOfType(ApiCrawler.class).values()) {
				calls.addAll(ac.getApiCalls());
			}
			Collections.sort(calls,new Comparator<ApiCall>() {

				@Override
				public int compare(ApiCall o1, ApiCall o2) {
					return o1.getFullPath().compareTo(o2.getFullPath());
				}
				
			});
		}
		return calls;
	}
	
	public List<String> getBasePaths(boolean includeDeprecated) {
		List<String> out = new ArrayList<String>();
		for(ApiCall ac : getCalls(includeDeprecated, null, null)) {
			if(!out.contains(ac.getBasePath())) {
				out.add(ac.getBasePath());
			}
		}
		return out;
	}

	public List<ApiCall> getCalls(boolean includeDeprecated, String searchTerm, String[] requestMethods) {
		List<ApiCall> out = new ArrayList<ApiCall>();
		List<String> rms = null;
		if(requestMethods!=null) {
			rms = new ArrayList<String>();
			for(String rm : requestMethods) {
				rms.add(rm);
			}
		}
		for(ApiCall ac : getCallsInternal()) {
			if(!includeDeprecated && ac.isDeprecated()) {
				continue;
			}
			if(searchTerm!=null && !ac.getFullPath().toLowerCase().contains(searchTerm.toLowerCase())) {
				continue;
			}
			if(rms!=null && !rms.contains(ac.getMethod())) {
				continue;
			}
			out.add(ac);
		}
		return out;
	}

}
