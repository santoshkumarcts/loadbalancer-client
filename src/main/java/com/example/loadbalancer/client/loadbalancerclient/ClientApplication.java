package com.example.loadbalancer.client.loadbalancerclient;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class ClientApplication {


	public static void main(String[] args) throws URISyntaxException {

		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(ClientApplication.class)
				.web(WebApplicationType.NONE)
				.run(args);

		WebClient loadBalancedClient = ctx.getBean(WebClient.Builder.class).build();

			URI uri = new URI("http://test-service/test");
			System.out.println("URI ======>>>>> " + uri);
			String response =
					loadBalancedClient.get().uri(uriBuilder -> uriBuilder.scheme("http").host("test-service").path("/test")
							.queryParam("reqParam", "{reqParam}")
							.queryParam("param","{param}")
							.build("{”parent-a1\":{“attrib1”:true,“attrib2\":“value1”,“attrib3\":3,“parent-a2”:{“attrib4\":17,“attrib5”:“abc117\"}}}","{“parent-b1”:{“attrib11\":true,“attrib12”:“value1\",“attrib13”:3,“parent-b2\":{“attrib14”:17,“attrib15\":“abc117”}}}"))
							.retrieve()
							.toEntity(String.class)
							.block().getBody();
			System.out.println(response);
	}
}

@Configuration
class DemoServerInstanceConfiguration {
	@Bean
	ServiceInstanceListSupplier serviceInstanceListSupplier() {
		return new DemoInstanceSupplier("test-service");
	}
}

@Configuration
@LoadBalancerClient(name = "test-service", configuration = DemoServerInstanceConfiguration.class)
class WebClientConfig {
	@LoadBalanced
	@Bean
	WebClient.Builder webClientBuilder() {
		return WebClient.builder();
	}
}

class DemoInstanceSupplier implements ServiceInstanceListSupplier {
	private final String serviceId;

	public DemoInstanceSupplier(String serviceId) {
		this.serviceId = serviceId;
	}

	@Override
	public String getServiceId() {
		return serviceId;
	}

	@Override
	public Flux<List<ServiceInstance>> get() {
		return Flux.just(Arrays
				.asList(new DefaultServiceInstance(serviceId + "1", serviceId, "::1", 8080, false),
						new DefaultServiceInstance(serviceId + "1", serviceId, "::1", 8081, false)));
	}

}
