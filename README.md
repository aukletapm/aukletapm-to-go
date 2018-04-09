[![Build Status](https://api.travis-ci.org/aukletapm/aukletapm-to-go.svg?branch=develop)](https://travis-ci.org/aukletapm/aukletapm-to-go)
[![Coverage Status](https://codecov.io/gh/aukletapm/aukletapm-to-go/branch/develop/graph/badge.svg)](https://codecov.io/gh/aukletapm/aukletapm-to-go/branch/develop)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.aukletapm.go/go/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aukletapm.go/go)
[![](https://img.shields.io/github/license/aukletapm/aukletapm-to-go.svg)](./LICENSE)


AukletAPM To Go
---
AukletAPM To Go is a free Android App for Java Web Application monitoring and alerting.    
We provide the fluent API to expose an HTTP interface, which is for the AukletAPM To Go and the alerting server to access.



## Download
<a href="https://play.google.com/store/apps/details?id=com.aukletapm.go" target="_blank">Google Play</a>

<a href="http://go.aukletapm.com/downloads/android/aukletapm-to-go.apk" target="_blank">Direct Download</a>

## Installation

#### Maven

```xml
<dependency>
    <groupId>com.aukletapm.go</groupId>
    <artifactId>go-servlet</artifactId>
  <version>1.6.0</version>
</dependency>
```
#### Gradle

```
compile group: 'com.aukletapm.go', name: 'go-servlet', version: '1.6.0'
```

#### Create servlet handle
```java
AukletApmToGoHttpServletHandler aukletApmToGoHttpServletHandler = AukletApmToGoHttpServletHandler.newBuilder()
                .name("My Site")
                .addModule(new OsModule())
                .addModule(new JvmModule())
                .build();
```

#### Spring MVC Integration



```java
@Controller
public class SampleController {

    private AukletApmToGoHttpServletHandler aukletApmToGoHttpServletHandler;

    @PostConstruct
    public void init() {
        aukletApmToGoHttpServletHandler = AukletApmToGoHttpServletHandler.newBuilder()
                .name("My Site")
                .addModule(new OsModule())
                .addModule(new JvmModule())
                .build();
    }

    @CrossOrigin(origins = {"*"})
    @RequestMapping("/aukletapm-to-go")
    public void akuletGoEndpoint(HttpServletRequest request, HttpServletResponse response) {
        aukletApmToGoHttpServletHandler.handle(request, response);
    }

    @RequestMapping("/")
    public String home() {
        return "redirect:/aukletapm-to-go";
    }

}
```

#### Servlet Integration
```java
@WebServlet("/aukletapm-to-go")
public class AukletApmToGoServlet extends HttpServlet {

    private AukletApmToGoHttpServletHandler servletHandler;

    @Override
    public void init() throws ServletException {
        servletHandler = AukletApmToGoHttpServletHandler.newBuilder().enableCors().name("My Site")
                .addModule(new OsModule())
                .addModule(new JvmModule())
                .build();
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        servletHandler.handle(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        servletHandler.handle(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        servletHandler.handle(req, resp);
    }

}
```


For documentation please go to: [http://go.aukletapm.com/documentation/](http://go.aukletapm.com/documentation/)  
