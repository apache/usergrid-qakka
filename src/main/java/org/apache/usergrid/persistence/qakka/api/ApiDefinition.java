package org.apache.usergrid.persistence.qakka.api;


import io.swagger.annotations.*;

@SwaggerDefinition(
        info = @Info(
                description = "Queue Management and Messages",
                version = "v1.0",
                title = "Apache Usergrid - Qakka API",
                contact = @Contact(
                        name = "Apache Usergrid project",
                        email = "dev@usergrid.apache.org",
                        url = "http://usergrid.apache.org"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "http://www.apache.org/licenses/LICENSE-2.0"
                )
        ),
        consumes = {"application/json", "application/xml"},
        produces = {"application/json", "application/xml"},
        schemes = {SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS},
        tags = {
                @Tag(name = "Private", description = "Tag used to denote operations as private")
        },
        externalDocs = @ExternalDocs(value = "README", url = "https://github.com/apache/usergrid-qakka")
)
/**
 * Created by Dave Johnson (snoopdave@apache.org) on 11/30/16.
 */
public interface ApiDefinition {}
