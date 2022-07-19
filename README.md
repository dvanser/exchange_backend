#Exchange back end
Exchange back end was bootstrapped with [Spring Initializr](https://start.spring.io/).  
Was generated: Gradle Project with Kotlin and Spring Boot 2.0.6  
Dependencies: Web, Security, JPA, H2

##Getting started
This chapter provides steps to 
run back end on your local environment.
###Prerequisites
Installed:
* Intellij Idea (Ultimate Edition)
* Java Developer Kit 8
* [Postman](https://www.getpostman.com/)
###Run the project
1. Find file ExchangeApplication.kt:
`src -> main -> kotlin -> com.exchange -> ExchangeApplication.kt`
1. Right click -> Run/Debug 'ExchangeApplication'
1. Predefined data will be loaded on app startup from the file `src/main/resources/data.sql`. 
See more [here](https://docs.spring.io/spring-boot/docs/current/reference/html/howto-database-initialization.html#howto-initialize-a-database-using-spring-jdbc)


###Database
1. Open Database tab from sidebar. Follow the [instructions](https://www.jetbrains.com/help/idea/connecting-to-a-database.html)
1. Create new Data Source H2
1. Set configuration. Copy configuration properties from `src -> main -> resources -> application.properties`.
Click on `Test Connection`

###Setup test
http://localhost:8080/users
