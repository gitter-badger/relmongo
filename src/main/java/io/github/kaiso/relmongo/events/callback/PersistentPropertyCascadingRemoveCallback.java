/**
*   Copyright 2018 Kais OMRI [kais.omri.int@gmail.com] and authors.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/

package io.github.kaiso.relmongo.events.callback;

import io.github.kaiso.relmongo.annotation.CascadeType;
import io.github.kaiso.relmongo.annotation.OneToMany;
import io.github.kaiso.relmongo.annotation.OneToOne;
import io.github.kaiso.relmongo.mongo.DatabaseOperations;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
/**
 * 
 * @author Kais OMRI
 *
 */
public class PersistentPropertyCascadingRemoveCallback implements FieldCallback {

    private Document source;
    private MongoOperations mongoOperations;
    private ObjectId id;
    private Object entity;

    public PersistentPropertyCascadingRemoveCallback(Document source, MongoOperations mongoOperations, Class<?> clazz) {
        super();
        this.source = source;
        this.mongoOperations = mongoOperations;
        this.id = this.source.getObjectId("_id");
        if (this.id != null) {
            Collection<?> findByIds = DatabaseOperations.findByIds(mongoOperations, clazz, id);
            this.entity = findByIds != null && !findByIds.isEmpty() ? findByIds.iterator().next() : null;
        }
    }

    public void doWith(Field field) throws IllegalAccessException {
        ReflectionUtils.makeAccessible(field);
        if (field.isAnnotationPresent(OneToMany.class)) {
            doCascade(field, field.getAnnotation(OneToMany.class).cascade());
        } else if (field.isAnnotationPresent(OneToOne.class)) {
            doCascade(field, field.getAnnotation(OneToOne.class).cascade());
        }

    }

    private void doCascade(Field field, CascadeType cascadeType) throws IllegalAccessException {
        Object child = field.get(entity);
        if (child != null) {
            if (Collection.class.isAssignableFrom(child.getClass())) {
                cascadeCollection(cascadeType, (Collection<?>) child);

            } else {
                cascadeItem(cascadeType, child);

            }
        }

    }

    private void cascadeItem(CascadeType cascadeType, Object child) {
        if (Arrays.asList(CascadeType.REMOVE, CascadeType.ALL).contains(cascadeType)) {
            mongoOperations.remove(child);
        }
    }

    private void cascadeCollection(CascadeType cascadeType, Collection<?> child) {
        if (Arrays.asList(CascadeType.REMOVE, CascadeType.ALL).contains(cascadeType)) {
            child.parallelStream().forEach(mongoOperations::remove);
        }
    }

    public void doProcessing() {
        if (id == null || entity == null) {
            return;
        }
        ReflectionUtils.doWithFields(entity.getClass(), this);
    }

}
