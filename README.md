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

Web Flux support either annotation and Functional Endpoints via a Route DSL definition. For this sample I choose a 
more kotlin idiomatic way for configure my endpoints. For this reason I used the kotlin route dsl, 
a very cool and readable way in order to define webflux routes. The routes definition was liek the code below:
```kotlin
object RouteConfig {

    fun routes() = beans {
        bean {
            val todoRepository = ref<TodoRepository>()

            router {
                POST("/todo/item") {
                    Mono.zip(it.principal(), it.bodyToMono(TodoRepresentation::class.java))
                            .flatMap { todoRepository.insert(Todo(it.t2.id, it.t1.name, it.t2.date, it.t2.todo)) }
                            .flatMap { ServerResponse.created(URI.create("/todo/item/%s".format(it.id))).build() }
                }

                ...
            }
        }
    }

}
```
Very simple and readable!!!

A very important and notable thing hear is about context definition. 
For configure my routes I use the it.valeriovaudi.todolist.web.config.RouteConfig kotlin singleton and use it 
in order to load this configuration I use it on the main app file and add this routes as initializer like in the figure 
![](https://github.com/mrFlick72/bootiful-kotlin-todo-list/blob/master/images/routes_config.png)

On the Test is important to add the same initializer on the test context because the routes of functional endpoints are not 
loaded on the test context even in Java classic bean definition, and for this reason is not possible to use the @WebFluxTest annotation. 
I solved this problem using a TestContextInitializer kotlin class in wich I provide the required initializer to the test context the code is very simple :

```kotlin
class TestContextInitializer : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(applicationContext: GenericApplicationContext) {
        RepositoryConfig.beans().initialize(applicationContext)
        RouteConfig.routes().initialize(applicationContext)
    }
}
```

then we can use it on the real test class using a complete spring boot test context and use a WebTestClient in order to 
test the service call. The class is like below, note the WebTestClient definition with lateinit useful because 
it on the compile time is null but because it will lazy initialized by Spring othervise we should define it as nullable even if it will be never null
```kotlin

@ContextConfiguration(initializers = [TestContextInitializer::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner::class)
class RouteConfigTest { 

    ...
    
    @Autowired
    private lateinit var webClient: WebTestClient


    @Test
    @WithMockUser
    fun `find all the todo in the list`() {
        val actual = TodoTestCaseInitializer.giveAnOrderedTodoRepresentationListByIdFor(
                this.webClient.get().uri("/todo/item").exchange().expectStatus().isOk
                        .returnResult(TodoRepresentation::class.java)
                        .responseBody.collectList().block()
                        .orEmpty())


        val expected = TodoTestCaseInitializer.giveAnOrderedTodoRepresentationListById()

        Assert.assertThat(actual, Is.`is`(expected))
    }
 
 ...
}
```

## Mongo as data store
In the applciatio I used Spring Data Reactive Mongo using the Embedded Mongo server in order to test the my repositories. 
For this use case I did not use the ReactiveMongoRepository auto magical interface of Spring Data. The my decision way because I used an Hexagonal Architectural approach.
Under this point ov view using the instant repository interface I had a dirty domain layer that should be as mutch as possible free from any framework.
For satisfy this requirement I defined a my TodoRepository interface that is then implemented on a specific TodoMongoRepository, the only 
library dependency is on Reactor that is in any case a reactive stream compatible library.

it.valeriovaudi.todolist.core.repository.TodoRepository
```kotlin

interface TodoRepository {

    fun insert(todo: Todo): Mono<Todo>
    fun findOne(todoId: String, userName: String): Mono<Todo>
    fun findAll(userName: String, date: LocalDate): Flux<Todo>
    fun update(userName: String, todoId: String, todo: String) : Mono<Todo>
    fun delete(todoId: String, userName: String) : Mono<Todo>
}
```

it.valeriovaudi.todolist.adapter.repository.TodoMongoRepository
```kotlin

override fun insert(todo: Todo): Mono<Todo> = reactiveMongoTemplate.save(todo)

    override fun findOne(todoId: String, userName: String): Mono<Todo> =
            reactiveMongoTemplate.findOne(Query.query(Criteria("id").`is`(todoId)
                    .and("userName").`is`(userName)),
                    Todo::class.java)

    override fun findAll(userName: String, date: LocalDate): Flux<Todo> = reactiveMongoTemplate.find(Query.query(Criteria("userName").`is`(userName).and("date")
            .gte(LocalDateTime.of(date, LocalTime.MIN)).lte(LocalDateTime.of(date, LocalTime.MAX))), Todo::class.java);

    override fun update(userName: String, todoId: String, todo: String) = findOne(todoId, userName).flatMap { Mono.just(it.copy(id = it.id,userName = it.userName,date = it.date, todo = todo)) }.flatMap { reactiveMongoTemplate.save(it) }

    override fun delete(todoId: String, userName: String) = findOne(todoId, userName).flatMap { reactiveMongoTemplate.remove(it).then(Mono.just(it)) }
```

