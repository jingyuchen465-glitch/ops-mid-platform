package com.bear.mcp.single.core.groovy;

import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GroovySecurityCustomizerTest {

    @Test
    void validateShouldBlockFileConstructor() {
        assertThrows(SecurityException.class,
                () -> GroovySecurityCustomizer.validate("def userDir = new File('C:/Users')"));
    }

    @Test
    void secureCustomizerShouldBlockFileConstructorDuringCompilation() {
        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.addCompilationCustomizers(GroovySecurityCustomizer.create());

        assertThrows(Exception.class,
                () -> new GroovyShell(configuration).parse("def userDir = new File('C:/Users')"));
    }
}
