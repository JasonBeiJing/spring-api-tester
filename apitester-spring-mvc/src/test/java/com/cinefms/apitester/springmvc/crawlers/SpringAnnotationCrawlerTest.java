package com.cinefms.apitester.springmvc.crawlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.support.GenericWebApplicationContext;

import com.cinefms.apitester.core.ApitesterService;
import com.cinefms.apitester.model.info.ApiCall;
import com.cinefms.apitester.springmvc.crawlers.mapping.AbstractController2;
import com.cinefms.apitester.springmvc.crawlers.mapping.AbstractControllerMethod;
import com.cinefms.apitester.springmvc.crawlers.mapping.Controller3;
import com.cinefms.apitester.springmvc.crawlers.mapping.ControllerImpl1;
import com.cinefms.apitester.springmvc.crawlers.mapping.ControllerImpl2;
import com.cinefms.apitester.springmvc.crawlers.mapping.ControllerImpl3;
import com.cinefms.apitester.springmvc.crawlers.mapping.ControllerMethod;
import com.cinefms.apitester.springmvc.crawlers.mapping.ControllerMethodImpl;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

public class SpringAnnotationCrawlerTest {

	@Test
	public void testAppContextGetterSetter() {
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();
		assertNull(sac.getApplicationContext());
		sac.setApplicationContext(new GenericWebApplicationContext());
		assertNotNull(sac.getApplicationContext());
	}

	
	@Test
	public void testPathCanonicalization() {
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();
		Map<String,String> beforeAndAfter = new HashMap<String, String>();
		beforeAndAfter.put("//a/{a}/xcd", "/a/{a}/xcd");
		beforeAndAfter.put("/b/a//{a}/xcd", "/b/a/{a}/xcd");
		for(Map.Entry<String, String> e : beforeAndAfter.entrySet()) {
			assertEquals(e.getValue(), sac.getPath(e.getKey()));
		}
	}

	@Test
	public void testBasePathCanonicalization() {
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();
		Map<String,String> beforeAndAfter = new HashMap<String, String>();
		beforeAndAfter.put("//a/{a}/xcd", "/a");
		beforeAndAfter.put("/b/a//{a}/xcd", "/b/a");
		beforeAndAfter.put("/b/a//", "/b/a/");
		beforeAndAfter.put("/b/a//x", "/b/a/x");
		for(Map.Entry<String, String> e : beforeAndAfter.entrySet()) {
			assertEquals(e.getValue(), sac.getBasePath(e.getKey()));
		}
	}

	@Test
	public void testAppContextScan() {
		ApplicationContext ac = Mockito.mock(ApplicationContext.class);
		Map<String,Object> a = new HashMap<String, Object>();
		a.put("aaa", new TestController2());
		Mockito.when(ac.getBeansWithAnnotation(Controller.class)).thenReturn(a);
		
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();
		sac.setApplicationContext(ac);
		sac.setService(new ApitesterService());

		List<ApiCall> calls = sac.getApiCalls();
		verify(ac, times(1)).getBeansWithAnnotation(Controller.class);
		assertEquals(1, calls.size());
	}


	@Test
	public void testSimpleClassExtractionExpectEmptyListSuccess() {
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();
		List<Object> controllers = new ArrayList<Object>();
		controllers.add(new TestController1());
		List<ApiCall> calls = sac.scanControllers("A",controllers);
		assertNotNull(calls);
		assertEquals(0, calls.size());
	}

	@Test
	public void testProxiedSimpleClassExtractionExpectEmptyListSuccess() {
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();
		List<Object> controllers = new ArrayList<Object>();
		Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(TestController2.class);
        enhancer.setCallback(NoOp.INSTANCE);
        controllers.add(enhancer.create());
		List<ApiCall> calls = sac.scanControllers("A",controllers);
		assertNotNull(calls);
		assertEquals(1, calls.size());
		assertEquals("com.cinefms.apitester.springmvc.crawlers.TestController2", calls.get(0).getHandlerClass());
		System.err.println(calls.get(0).getHandlerClass());
	}

	@Test
	public void testSimpleClassExtractionExpectSingleCallsSuccess() {
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();
		List<Object> controllers = new ArrayList<Object>();
		controllers.add(new TestController2());
		List<ApiCall> calls = sac.scanControllers("A",controllers);
		assertNotNull(calls);
		assertEquals(1, calls.size());
		assertEquals("/blah", calls.get(0).getFullPath());
		assertEquals("/blah", calls.get(0).getBasePath());
		assertEquals("GET", calls.get(0).getMethod());
	}

	@Test
	public void testSimpleClassExtractionExpectManyCallsSuccess() {
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();
		List<Object> controllers = new ArrayList<Object>();
		controllers.add(new TestController3());
		List<ApiCall> calls = sac.scanControllers("A",controllers);
		assertNotNull(calls);
		assertEquals(6, calls.size());
		assertEquals("/a/x", calls.get(0).getFullPath());
		assertEquals("/a/y", calls.get(1).getFullPath());
		assertEquals("/a/z", calls.get(2).getFullPath());
		assertEquals("/b/x", calls.get(3).getFullPath());
		assertEquals("/b/y", calls.get(4).getFullPath());
		assertEquals("/b/z", calls.get(5).getFullPath());
	}
	
	@Test
	public void testPathParametersExpectTwoSuccess() {
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();
		List<Object> controllers = new ArrayList<Object>();
		controllers.add(new TestController4());
		List<ApiCall> calls = sac.scanControllers("A",controllers);
		assertNotNull(calls);
		assertEquals(1, calls.size());
		assertEquals("/x", calls.get(0).getBasePath());
		assertEquals("/x/{id}/sub/{value}/xxx/{a}", calls.get(0).getFullPath());
		assertEquals(3, calls.get(0).getPathParameters().size());
		// first param
		assertEquals(String.class.getCanonicalName(), calls.get(0).getPathParameters().get(0).getParameterType().getClassName());

		// second param
		assertEquals(String.class.getCanonicalName(), calls.get(0).getPathParameters().get(1).getParameterType().getClassName());
		assertEquals(null, calls.get(0).getPathParameters().get(1).getDefaultValue());
		assertEquals("value", calls.get(0).getPathParameters().get(1).getParameterName());

		// thrid param
		assertEquals("int", calls.get(0).getPathParameters().get(2).getParameterType().getClassName());
		assertEquals(null, calls.get(0).getPathParameters().get(2).getDefaultValue());
		assertEquals("a", calls.get(0).getPathParameters().get(2).getParameterName());
	}
	
	@Test
	public void testClassLevelAnnotationExpectTwoSuccess() {
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();
		List<Object> controllers = new ArrayList<Object>();
		controllers.add(new TestController5());
		controllers.add(new TestController6());
		List<ApiCall> calls = sac.scanControllers("A",controllers);
		assertNotNull(calls);
		assertEquals(2, calls.size());
		assertEquals("/aaa/x/{id}", calls.get(0).getFullPath());
		assertEquals("/aaa/x", calls.get(0).getBasePath());
		assertEquals("/aaa/{aaaId}/x/{id}", calls.get(1).getFullPath());
		assertEquals("/aaa", calls.get(1).getBasePath());
	}
	
	@Test
	public void testBasepath() {
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();
		String x = sac.getBasePath("/asdasd/asdasd/das/{adsasd}/asdasd");
		assertEquals("/asdasd/asdasd/das", x);
	}
	
	@Test
	public void testPrefixConfigAnnotationExpectTwoSuccess() {
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();

		// servlet context
		ServletContext sc = mock(ServletContext.class);
		when(sc.getContextPath()).thenReturn("/scpath");
		sac.setServletContext(sc);

		List<Object> controllers = new ArrayList<Object>();
		controllers.add(new TestController5());
		controllers.add(new TestController6());
		List<ApiCall> calls = sac.scanControllers("A",controllers);
		assertNotNull(calls);
		assertEquals(2, calls.size());
		assertEquals("/scpath/aaa/x/{id}", calls.get(0).getFullPath());
		assertEquals("/scpath/aaa/x", calls.get(0).getBasePath());
		assertEquals("/scpath/aaa/{aaaId}/x/{id}", calls.get(1).getFullPath());
		assertEquals("/scpath/aaa", calls.get(1).getBasePath());
	}
	
	
	@Test
	public void testRequestParameters() {
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();
		List<Object> controllers = new ArrayList<Object>();
		controllers.add(new TestController7());
		List<ApiCall> calls = sac.scanControllers("A",controllers);
		assertEquals(1, calls.size());
		assertEquals(5, calls.get(0).getRequestParameters().size());
		//
		assertEquals("id", calls.get(0).getRequestParameters().get(0).getParameterName());
		assertEquals(String.class.getCanonicalName(), calls.get(0).getRequestParameters().get(0).getParameterType().getClassName());
		assertEquals(true, calls.get(0).getRequestParameters().get(0).isMandatory());
		assertEquals(true, calls.get(0).getRequestParameters().get(0).isDeprecated());
		assertEquals("dd-mm-yyyy", calls.get(0).getRequestParameters().get(0).getFormat());
		assertEquals(null,calls.get(0).getRequestParameters().get(0).getDefaultValue());
		//
		assertEquals("id2", calls.get(0).getRequestParameters().get(1).getParameterName());
		assertEquals(String.class.getCanonicalName(), calls.get(0).getRequestParameters().get(1).getParameterType().getClassName());
		assertEquals(false, calls.get(0).getRequestParameters().get(1).isMandatory());
		assertEquals(false, calls.get(0).getRequestParameters().get(1).isDeprecated());
		assertEquals("0.9", calls.get(0).getRequestParameters().get(1).getSince());
		assertNull(calls.get(0).getRequestParameters().get(1).getFormat());
		assertEquals(null,calls.get(0).getRequestParameters().get(1).getDefaultValue());

		assertEquals("ID", calls.get(0).getRequestParameters().get(3).getParameterName());
		assertEquals("someDefault", calls.get(0).getRequestParameters().get(3).getDefaultValue());
	}
	
	

	@Test
	public void testReturnTypeExpectSimpleString() {
		ApplicationContext acc = mock(ApplicationContext.class);
		
		Map<String,Object> aMap = new HashMap<String, Object>();
		Object a = new TestController8();
		aMap.put("a",a);

		when(acc.getBeansWithAnnotation(Controller.class)).thenReturn(aMap);
		
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();
		sac.setService(new ApitesterService());
		sac.setApplicationContext(acc);

		List<ApiCall> calls = sac.getApiCalls();

		assertEquals(1, calls.size());
		//
		assertEquals("java.lang.String", calls.get(0).getReturnType().getReturnClass().getClassName());
		assertEquals(false, calls.get(0).getReturnType().isCollection());

	}
	
	@Test
	public void testReturnTypeExpectCollection() {
		ApplicationContext acc = mock(ApplicationContext.class);
		
		Map<String,Object> aMap = new HashMap<String, Object>();
		Object a = new TestController9();
		aMap.put("a",a);
		
		when(acc.getBeansWithAnnotation(Controller.class)).thenReturn(aMap);
		
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();
		sac.setApplicationContext(acc);
		sac.setService(new ApitesterService());
		
		List<ApiCall> calls = sac.getApiCalls();
		
		assertEquals(1, calls.size());
		//
		assertEquals("java.lang.String", calls.get(0).getReturnType().getReturnClass().getClassName());
		assertEquals(true, calls.get(0).getReturnType().isCollection());
		
	}
	
	@Test
	public void testReturnTypeExpectVoid() {
		ApplicationContext acc = mock(ApplicationContext.class);
		
		Map<String,Object> aMap = new HashMap<String, Object>();
		Object a = new TestController10();
		aMap.put("a",a);
		
		when(acc.getBeansWithAnnotation(Controller.class)).thenReturn(aMap);
		
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();
		sac.setService(new ApitesterService());
		sac.setApplicationContext(acc);
		
		List<ApiCall> calls = sac.getApiCalls();
		
		assertEquals(1, calls.size());
		//
		assertEquals("void", calls.get(0).getReturnType().getReturnClass().getClassName());
		assertEquals(false, calls.get(0).getReturnType().isCollection());
		
	}
	
	
	@Test
	public void testReturnTypeExpectMap() {
		ApplicationContext acc = mock(ApplicationContext.class);
		
		Map<String,Object> aMap = new HashMap<String, Object>();
		Object a = new TestController11();
		aMap.put("a",a);
		
		when(acc.getBeansWithAnnotation(Controller.class)).thenReturn(aMap);
		
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();
		sac.setApplicationContext(acc);
		sac.setService(new ApitesterService());
		
		List<ApiCall> calls = sac.getApiCalls();
		
		assertEquals(1, calls.size());
		//
		assertEquals("java.util.Map", calls.get(0).getReturnType().getReturnClass().getClassName());
		assertEquals(false, calls.get(0).getReturnType().isCollection());
		
	}
	
	
	@Test
	public void testRequestBodyExpectTrueFalse() {
		ApplicationContext acc = mock(ApplicationContext.class);
		
		Map<String,Object> aMap = new HashMap<String, Object>();
		Object a = new TestController11();
		aMap.put("a",a);
		
		when(acc.getBeansWithAnnotation(Controller.class)).thenReturn(aMap);
		
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();
		sac.setApplicationContext(acc);
		sac.setService(new ApitesterService());

		List<ApiCall> calls = sac.getApiCalls();
		
		assertEquals(1, calls.size());
		//
		assertEquals("com.cinefms.apitester.model.info.ApiObject", calls.get(0).getRequestBodyParameters().get(0).getParameterType().getClassName());
		assertEquals("com.cinefms.apitester.model.info.ApiObject", calls.get(0).getRequestBodyParameters().get(1).getParameterType().getClassName());
		
	}
	
	@Test
	public void testDescriptionInFileExpectSuccess() {
		ApplicationContext acc = mock(ApplicationContext.class);
		
		Map<String,Object> aMap = new HashMap<String, Object>();
		Object a = new TestController12();
		aMap.put("a",a);
		
		when(acc.getBeansWithAnnotation(Controller.class)).thenReturn(aMap);
		
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();
		sac.setApplicationContext(acc);
		sac.setService(new ApitesterService());
		
		List<ApiCall> calls = sac.getApiCalls();
		
		assertEquals(5, calls.size());
		//
		System.err.println("-------------------------------");
		for(ApiCall ac : calls) {
			System.err.println(ac.getDescription());
		}
		System.err.println("-------------------------------");
		
		assertEquals("DESCRIPTION_A", calls.get(0).getDescription());
		assertEquals("DESCRIPTION_B", calls.get(1).getDescription());
		assertEquals("returns the A", calls.get(2).getDescription());
		assertEquals("1.22", calls.get(3).getDeprecatedSince());
		assertEquals("0.9", calls.get(3).getSince());
		assertEquals(true, calls.get(3).isDeprecated());

		assertEquals(true, calls.get(4).isDeprecated());
		
	}
	
	@Test
	public void testGenericControllerRequestMapping_implmenation(){
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();
		RequestMapping actual = sac.getClassAnnotation(ControllerImpl1.class, RequestMapping.class);

		RequestMapping expect = ControllerImpl1.class.getAnnotation(RequestMapping.class);
	
		Assert.assertEquals(expect.value()[0], actual.value()[0]);
	}
	
	@Test
	public void testGenericControllerRequestMapping_abstract(){
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();
		RequestMapping actual = sac.getClassAnnotation(ControllerImpl2.class, RequestMapping.class);

		RequestMapping expect = AbstractController2.class.getAnnotation(RequestMapping.class);
	
		Assert.assertEquals(expect.value()[0], actual.value()[0]);
	}
	
	@Test
	public void testGenericControllerRequestMapping_interface(){
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();
		RequestMapping actual = sac.getClassAnnotation(ControllerImpl3.class, RequestMapping.class);

		RequestMapping expect = Controller3.class.getAnnotation(RequestMapping.class);
		Assert.assertEquals(expect.value()[0], actual.value()[0]);
	}
	
	@Test
	public void testGenericMethodRequestMapping_implmenation() throws NoSuchMethodException, SecurityException{
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();
		
		RequestMapping actual = sac.getMethodAnnotation(ControllerMethodImpl.class.getMethod("testMethod", String.class, String.class), RequestMapping.class);

		RequestMapping expect = ControllerMethodImpl.class.getMethod("testMethod", String.class, String.class).getAnnotation(RequestMapping.class);
	
		Assert.assertEquals(expect.value()[0], actual.value()[0]);
	}
	
	@Test
	public void testGenericMethodRequestMapping_abstract() throws NoSuchMethodException, SecurityException{
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();
		
		RequestMapping actual = sac.getMethodAnnotation(ControllerMethodImpl.class.getMethod("testMethod", Long.class), RequestMapping.class);

		RequestMapping expect = AbstractControllerMethod.class.getMethod("testMethod", Long.class).getAnnotation(RequestMapping.class);
	
		Assert.assertEquals(expect.value()[0], actual.value()[0]);
	}
	
	@Test
	public void testGenericMethodRequestMapping_interface() throws NoSuchMethodException, SecurityException{
		SpringAnnotationCrawler sac = new SpringAnnotationCrawler();
		
		RequestMapping actual = sac.getMethodAnnotation(ControllerMethodImpl.class.getMethod("testMethod", String.class), RequestMapping.class);

		RequestMapping expect = ControllerMethod.class.getMethod("testMethod", String.class).getAnnotation(RequestMapping.class);
	
		Assert.assertEquals(expect.value()[0], actual.value()[0]);
	}
}
