package com.cinefms.apitester.springmvc.crawlers.mapping;

import org.springframework.web.bind.annotation.RequestMapping;

public abstract class AbstractControllerMethod {

	@RequestMapping("/api/method/implementation/id")
	public abstract void testMethod(Long id);
	
}
