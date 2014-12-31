# Sahagin

Make your Selenium Script more readable and maintainable!

Sahagin provides highly readable HTML test script viewer and test result report.

Sahagin is now beta version, and only supports JUnit4 and Selenium WebDriver Java binding in the current latest version.
If you want support for other languages or test frameworks, please request us by creating new [issue](https://github.com/SahaginOrg/sahagin-java/issues).

[![Build Status](https://travis-ci.org/SahaginOrg/sahagin-java.svg?branch=master)](https://travis-ci.org/SahaginOrg/sahagin-java)

# Getting started

## 1. Set up Java library

### If you use Maven
Add dependency to your pom.xml file.

```xml
  <dependencies>
    <dependency>
      <groupId>org.sahagin</groupId>  
       <artifactId>sahagin</artifactId>  
       <version>0.2.5</version> 
    </dependency>
  </dependencies>
```

and add test execution JVM argument.

```xml
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-surefire-plugin</artifactId>
      <version>2.18(or any other version)</version>
      <configuration>
        <argLine>
          -javaagent:${settings.localRepository}/org/sahagin/sahagin/0.2.5/sahagin-0.2.5.jar
        </argLine>
      </configuration>
    </plugin>
  </plugins>
```

### If you use Gradle
Add dependency to your build.gradle file.

```groovy
dependencies {
    compile 'org.sahagin:sahagin:0.2.5'
}
```

and add test execution JVM argument.

```groovy
test {
    doFirst {
        // search sahagin jar file in the local cache
        def sahaginJar = project.configurations.testCompile.find {
            it.name.startsWith('sahagin-0.2.5') 
        }
        jvmArgs '-javaagent:' + sahaginJar
    }
}

```

### If you use jar file directory
Download sahagin-0.2.5.zip from [here](https://github.com/SahaginOrg/sahagin-java/releases/tag/0.2.5) and add all jar files in the zip file to the Java class path.

## 2. Add annotations
Add @Page annotations to your page object class declarations, and add @TestDoc annotations to your page object methods or any other methods.

You don't need to add annotations to all your classes and methods.

```java
import org.sahagin.runlib.external.Page;
import org.sahagin.runlib.external.TestDoc;

@Page("Data input page")
public class DataInputPage {
    
    @TestDoc("Set user name {user}")
    public void setUserName(String user) {
        ....
    }
    
    @TestDoc("user name")
    public String getUserName() {
        ....
    }

}
```

## 3. Set WebDriver instance before each test method is called
Set a WebDriver instance used to take screen captures.

```java
import org.sahagin.runlib.external.adapter.webdriver.WebDriverAdapter;

public class SampleTest {

  @Before
  public void setUp() {
      driver = new FirefoxDriver(); // or any other driver you want to use
      WebDriverAdapter.setAdapter(driver);
      ...
  }

}
```

## 4. Create sahagin.yml
Create a file "sahagin.yml" on the Java project root directory.

Then change the "testDir" value to the root directory your test files are located.

```yaml
# Sahagin configuration file.

java:
  # Root directory of test Java files
  # (absolute path or relative path from this YAML file).
  # All methods and classes annotated by @Page or @TestDoc 
  # must be located under this directory.
  testDir: src/test/java
```

## 5. Run test and generate report
Run your JUnit tests.

Then you will find the report sahagin-rerpot/index.html on the Java project root directory.

## 6. Jenkins plug-in

Coming soon.


