package com.cinefms.apitester.springmvc.crawlers.mapping;

import org.springframework.web.bind.annotation.RequestMapping;

public interface ControllerMethod {

	@RequestMapping("/api/method/interface/a")
	public void testMethod(String a);
	
	@RequestMapping("/api/method/interface/a")
	public void testMethod(String a, String b);
}
