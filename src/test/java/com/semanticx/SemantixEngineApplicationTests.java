package com.semanticx;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.semantix.ctrl.SemantixEngineApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SemantixEngineApplication.class)
@WebAppConfiguration
public class SemantixEngineApplicationTests {

	@Test
	public void contextLoads() {
	}

}
