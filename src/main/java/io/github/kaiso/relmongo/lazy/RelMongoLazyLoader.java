package io.github.kaiso.relmongo.lazy;

import io.github.kaiso.relmongo.mongo.DatabaseOperations;

import org.bson.types.ObjectId;
import org.springframework.cglib.proxy.LazyLoader;
import org.springframework.data.mongodb.core.MongoOperations;

import java.util.Collection;
import java.util.List;

public class RelMongoLazyLoader implements LazyLoader {

    private List<Object> ids;
    private MongoOperations mongoOperations;
    private Class<?> targetClass;
    private Class<?> fieldType;

    public RelMongoLazyLoader(List<Object> ids, MongoOperations mongoOperations, Class<?> targetClass, Class<?> fieldType) {
        super();
        this.ids = ids;
        this.mongoOperations = mongoOperations;
        this.targetClass = targetClass;
        this.fieldType = fieldType;
    }

    @Override
    public Object loadObject() throws Exception {
        if (!ids.isEmpty()) {
            if (Collection.class.isAssignableFrom(fieldType)) {
                return DatabaseOperations.findByIds(mongoOperations, targetClass, ids.toArray(new ObjectId[ids.size()]));
            } else {
                return DatabaseOperations.findByPropertyValue(mongoOperations, targetClass, "_id", ids.get(0));
            }
        }
        return null;
    }

}
