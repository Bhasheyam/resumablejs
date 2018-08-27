package chucked;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;



import chucked.model.MessageFormat;

@Configuration 
public class KafkaConfiguration 
{

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
	public ProducerFactory producerFactory()
	{
		Map<String, Object> config = new HashMap<>();
	    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"127.0.0.1:9092");
	    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG ,StringSerializer.class );
	    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG , JsonSerializer.class );
		return new DefaultKafkaProducerFactory(config);
		
	}
	
	@SuppressWarnings("unchecked")
	@Bean
	public KafkaTemplate<String, MessageFormat> KafkaTemplate()
	{
		return new KafkaTemplate<>(producerFactory());
	}
}
