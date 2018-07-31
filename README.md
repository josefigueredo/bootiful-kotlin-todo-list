# Bootiful-Kotlin-Todo-List
 
It is a simple todo list, implemented on Spring boot 2.x entirely written in Kotlin. The application avails on 
Reactive Mongo as datastore and ReactJS on front end layer

## Technologies

- Spring Boot 2.x
- Spring Data Reactive MongoDB 
- Webflux
- kotlin as main language
- React JS as front end framework

## Intent
The intent of the project is provide a complete sample of how use kotlin with a spring boot project. 
The project show how use the newest kotlin dsl bean and route definition, in this case notable is how configure the dsl 
in the main and test context. The dsl was provided via context initializer and therefore in the test is not 
loaded if not explicit, the project show how install those bean definition.

# The application
As said before the application is a simple to do list. The application is protected by the simple default login page 
that Spring provides, the default user credentials are userName: user, password: secret, for the mongo storage can be used 
the provided docker-compose under the docker folder. The entry point of the application is http://localhost:8080/index.html.
The main server used is teh default Netty provided with the reactive-web starter. The application is implemented in a no blocking io manner, 
using webflux on web layer, reactive mongo as data store and Reactor Project as glue.

# The secuirty  

Being a "POC" the security concern are very basic as the official Spring Documentation say we have configure by ourself the SecurityWebFilterChain. 
The snippet of code is very simple it is notable the usage of a password encoder and the relative UserDetailsService implementation for a reactive system that is 
an implementation of the ReactiveUserDetailsService interface.
The code is available on the it.valeriovaudi.todolist.web.config.SecurityConfig kotlin class.

# The reactive web layer
The web layer is implemented with the newest reactive stack based in this sample on Netty, but weill be 
enough include the tomcat-starter in order to switch on tomcat as web server since that Spring Boot 2.x include 
as embedded Tomcat a version of Tomcat that support the Servlet 3.1+ version that support the No Blocking IO.

Web Flux support either annotation and Functional Endpoints via a Route DSL definition. For this sample I choose of use a 
more kotlin idiomatic way for configure my endpoints and for this reason I used the kotlin route dsl, 
a very cool and readable way in order to define webflux routes.

A very important and notable thing is about context definition. 
For configure my routes I use the it.valeriovaudi.todolist.web.config.RouteConfig kotlin singleton the code was like below
```kotlin
object RouteConfig {

    fun routes() = beans {
        bean {
            val todoRepository = ref<TodoRepository>()

            router {
                POST("/todo/item") {
                    ...
                }

                GET("/todo/item/{id}") {
                   ...
                }

                ...
            }
        }
    }

}
```
In order to load this configuration I use it on the main app file and add this routes as initializer like in the figure 
![GitHub Logo](https://github.com/mrFlick72/bootiful-kotlin-todo-list/images/routes_config.png)
