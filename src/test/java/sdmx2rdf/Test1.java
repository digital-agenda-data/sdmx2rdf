package sdmx2rdf;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import demo.sdmxsource.webservice.main.chapter2.Chapter2ResolveReferences;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/spring-beans-chapter1.xml")
public class Test1 {

	@Autowired
	Chapter2ResolveReferences chapter2ResolveReferences;
	
	@Test
	public void test1() throws UnsupportedEncodingException {

		// TODO: add some unit tests
		assertFalse(chapter2ResolveReferences == null);
		chapter2ResolveReferences.main(null);

	}
}
