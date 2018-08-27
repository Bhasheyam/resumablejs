package chucked.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import chucked.model.MessageFormat;



public interface DemoRepository extends  MongoRepository< MessageFormat, String >{

}
