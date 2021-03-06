package com.gcinterceptor.msgpush;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.gcinterceptor.spring.SpringGcInterceptor;
import com.gcinterceptor.spring.SpringGcInterceptorConfiguration;

@SpringBootApplication
@Import(SpringGcInterceptorConfiguration.class)
public class MsgPush {
	private static final boolean USE_GCI = Boolean.parseBoolean(System.getenv("USE_GCI"));
	private static int MSG_SIZE;
	private static int WINDOW_SIZE;
	private static byte[][] buffer;
	private static int msgCount;

	static {
		WINDOW_SIZE = Integer.parseInt(System.getenv("WINDOW_SIZE"));
		MSG_SIZE = Integer.parseInt(System.getenv("MSG_SIZE"));
		if (WINDOW_SIZE>0) {
			buffer = new byte[WINDOW_SIZE][MSG_SIZE];
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(MsgPush.class, args);
	}

	@Bean
	public WebMvcConfigurer adapter() {
		return new WebMvcConfigurer() {
			@Autowired
			SpringGcInterceptor gci;

			@Override
			public void addInterceptors(InterceptorRegistry registry) {
				if (USE_GCI) {
					registry.addInterceptor(gci);
				}
			}
		};
	}

	@RestController
	public static class HelloController {
		@RequestMapping("/*")
		public void index() throws InterruptedException {
			byte[] byteArray = new byte[MSG_SIZE];

			for (int i = 0; i < MSG_SIZE; i++) {
				byteArray[i] = (byte) i;
			}

			if (WINDOW_SIZE > 0) {
				buffer[msgCount++ % WINDOW_SIZE] = byteArray;
			}

		}
	}
}
