package com.lin.demo;

import com.lin.demo.prometheus.service.PromService;
import com.lin.demo.vo.ResultVo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class DemoApplicationTests {

	@Resource
	private PromService promService;

	@Test
	void contextLoads() {
	}

	@Test
	void getCpuUsedRate(){
		ResultVo cpuUsedRate = promService.getCpuUsedRate("192.168.2.131:9100");
		System.out.println(cpuUsedRate);
	}

	@Test
	void loginTest(){
		System.out.println("====================");
	}
}
