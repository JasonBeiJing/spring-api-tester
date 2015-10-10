package com.cinefms.apitester.springmvc.crawlers.mapping;

import org.springframework.web.bind.annotation.RequestMapping;

public class ControllerMethodImpl extends AbstractControllerMethod implements ControllerMethod{

	@Override
	public void testMethod(String a) {
		
	}
	
	@Override
	@RequestMapping("/api/method/implementation/ab")
	public void testMethod(String a, String b) {
		
	}

	@Override
	public void testMethod(Long id) {
		
	}
}
